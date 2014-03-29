package net.notifly.core.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.R;
import net.notifly.core.entity.DistanceMatrix;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.MainActivity_;
import net.notifly.core.gui.activity.main.RetreiveDistanceMatrixTask;
import net.notifly.core.sql.NotesDAO;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by Barak on 21/03/2014.
 */
public class BackgroundService extends Service
{
  // constant
  public static final int NOTIFY_INTERVAL = 1; // In minutes
  public static final int TIME_LOCATION_SAFETY_FACTOR = 15; // In minutes
  public static final int TIME_ONLY_SAFETY_FACTOR = 15; // In minutes
  public static final int TIME_ONLY_REMINDER_INTERVAL = 5; // In minutes
  public static final int GENERAL_REMINDER_INTERVAL = 30; // In minutes
  public static final int LOCATION_ONLY_DISTANCE_FACTOR = 300; // In meters
  public static final int LOCATION_ONLY_REMINDER_INTERVAL = 5; // In minutes
  public static final int[] TIME_LOCATION_REMINDER_TIMINGS = new int[]{0, 5, 13, TIME_LOCATION_SAFETY_FACTOR};

  public static boolean ALIVE = false;

  private NotificationManager notificationManager;

  // run on another Thread to avoid crash
  private Handler handler = new Handler();
  // timer handling
  private Timer timer = new Timer();

  // Unique Identification Number for the Notification.
  // We use it on Notification start, and to cancel it.
  private int NOTIFICATION = R.string.local_service_started;

  private Map<Note, DistanceMatrix> etaMap = new HashMap<Note, DistanceMatrix>();
  private Map<Note, Integer> reminderMap = new HashMap<Note, Integer>();

  /**
   * Class for clients to access.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with
   * IPC.
   */
  public class LocalBinder extends Binder
  {
    BackgroundService getService() {
      return BackgroundService.this;
    }
  }

  @Override
  public void onCreate() {
    Log.d("BackgroundService", "Creating shit");
    ALIVE = true;

    // init joda time
    ResourceZoneInfoProvider.init(this);

    notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d("BackgroundService", "Received start id " + startId + ": " + intent);

    // schedule task
    timer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL * 60 * 1000);

    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  private class TimeDisplayTimerTask extends TimerTask
  {
    @Override
    public void run() {
      // run on another thread
      handler.post(new Runnable()
      {

        @Override
        public void run()
        {
          LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
          Location currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
          LocalDateTime now = LocalDateTime.now();
          NotesDAO notesDAO = new NotesDAO(BackgroundService.this);
          List<Note> notes = notesDAO.getAllNotes();
          notesDAO.close();

          List<Note> notesToNotify = new ArrayList<Note>();
          etaMap.clear();
          for (Note note : notes)
          {
            // It's a time based note
            boolean timeBasedNote = note.getTime() != null;
            // It's a location based note
            boolean locationBasedNote = note.getLocation() != null;
            if (timeBasedNote && locationBasedNote)
            {
              handleLocationBasedNote(now, currentLocation, note, notesToNotify, false);
            }
            // time based only
            else if (timeBasedNote)
            {
              // Do not notify me about note past time
              if (!now.isAfter(note.getTime()))
              {
                remindNoteByNoteTime(now, notesToNotify, note, TIME_ONLY_REMINDER_INTERVAL);
              }
            }
            // location based only
            else if (locationBasedNote)
            {
              handleLocationBasedNote(now, currentLocation, note, notesToNotify, true);
            }
            else // neither time nor location
            {
              remindNote(notesToNotify, note, GENERAL_REMINDER_INTERVAL);
            }
          }

          if (notesToNotify.size() > 0)
            showNotification(getShortContent(notesToNotify), getExtendedContent(notesToNotify));
        }

        private void remindNoteByNoteTime(LocalDateTime now, List<Note> notesToNotify, Note note,
                                          int interval)
        {
          if (now.plusMinutes(TIME_ONLY_SAFETY_FACTOR).isAfter(note.getTime()))
          {
            remindNote(notesToNotify, note, interval);
          }
        }

        private void remindNote(List<Note> notesToNotify, Note note, int interval)
        {
          createEntryIfNeeded(note);

          if (reminderMap.get(note) % interval == 0)
          {
            notesToNotify.add(note);
          }
          increaseRemindCounter(note);
        }

        private void increaseRemindCounter(Note note)
        {
          reminderMap.put(note, reminderMap.get(note) + 1);
        }

        private void handleLocationBasedNote(LocalDateTime now, Location currentLocation, Note note,
                                             List<Note> notesToNotify, boolean isLocationOnly)
        {
          String org = net.notifly.core.entity.Location.from(currentLocation).toString();
          String dest = note.getLocation().toString();
          try
          {
            // TODO: Get the mode of transportation from note
            DistanceMatrix distanceMatrix = new RetreiveDistanceMatrixTask().execute(org, dest,
              "driving").get();

            // TODO: check what can we do when Google fails to give distance matrix
            if (distanceMatrix == null) return;

            if (isLocationOnly)
            {
              if (distanceMatrix.getDistance() < LOCATION_ONLY_DISTANCE_FACTOR)
              {
                createEntryIfNeeded(note);

                if (reminderMap.get(note) % LOCATION_ONLY_REMINDER_INTERVAL == 0)
                {
                  notesToNotify.add(note);
                  etaMap.put(note, distanceMatrix);
                }

                increaseRemindCounter(note);
              }
            }
            else // time and location based note
            {
              // Check if we'll not make it on time
              if (now.plusSeconds((int) distanceMatrix.getDuration()).
                  plusMinutes(TIME_LOCATION_SAFETY_FACTOR).isAfter(note.getTime()))
              {
                createEntryIfNeeded(note);

                for (int timing : TIME_LOCATION_REMINDER_TIMINGS)
                {
                  if (reminderMap.get(note) == timing)
                  {
                    notesToNotify.add(note);
                    etaMap.put(note, distanceMatrix);
                  }
                }

                increaseRemindCounter(note);
              }
            }
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
          catch (ExecutionException e)
          {
            e.printStackTrace();
          }
        }

        private void createEntryIfNeeded(Note note)
        {
          if (!reminderMap.containsKey(note))
          {
            reminderMap.put(note, 0);
          }
        }

        private String getShortContent(List<Note> notesToNotify)
        {
          return "You have " + notesToNotify.size() + " incoming task" +
            ((notesToNotify.size() > 0) ? "s!" : "!");
        }

        private String getExtendedContent(List<Note> notesToNotify)
        {
          String msg = "Incoming tasks: \n";
          for (int i = 0; i < notesToNotify.size(); i++)
          {
            Note note = notesToNotify.get(i);
            msg += (i + 1) + ". " + note.getTitle();
            // It's a time based note
            boolean timeBasedNote = note.getTime() != null;
            // It's a location based note
            boolean locationBasedNote = note.getLocation() != null;

            if (timeBasedNote && locationBasedNote)
            {
              msg += " ,ETA: " + etaMap.get(note).getDurationText();
            }
            else if (timeBasedNote)
            {
              msg += " , due time: " + note.getTime().toString("HH:mm");
            }
            else if (locationBasedNote)
            {
              msg += " , in " + etaMap.get(note).getDistanceText() + " within " +
                etaMap.get(note).getDurationText();
            }

            msg += "\n";
          }
          return msg;
        }
      });
    }
  }

  @Override
  public void onDestroy() {
    Log.d("BackgroundService", "Destroying shit");
    ALIVE = false;

    // Cancel the persistent notification.
    notificationManager.cancel(NOTIFICATION);

    // Tell the user we stopped.
    Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();

    // Display a notification about us stopping.  We put an icon in the status bar.
    showNotification(getText(R.string.local_service_stopped).toString(), "");
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  // This is the object that receives interactions from clients.  See
  // RemoteService for a more complete example.
  private final IBinder mBinder = new LocalBinder();

  /**
   * Show a notification while this service is running.
   */
  private void showNotification(String text, String message) {
    // The PendingIntent to launch our activity if the user selects this notification
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
      new Intent(this, MainActivity_.class), 0);

    // Set the icon, scrolling text and timestamp
    Notification notification;

    String title = "Notifly";
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

      notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
      notification.setLatestEventInfo(this, title , text, contentIntent);
      notification.flags |= Notification.FLAG_AUTO_CANCEL;
    } else {
      notification = new NotificationCompat.Builder(this).setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_launcher).setTicker(text).setWhen(System.currentTimeMillis())
        .setAutoCancel(true).setContentTitle(title)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
        .setContentText(text).build();
    }

    // Setting vibration, sound and led light
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
    notification.ledARGB = Color.CYAN;
    notification.ledOnMS = 500;
    notification.ledOffMS = 500;
    notification.flags |= Notification.FLAG_SHOW_LIGHTS;

    // Send the notification.
    notificationManager.notify(NOTIFICATION, notification);
  }
}
