package net.notifly.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.MainActivity;
import net.notifly.core.sql.NotesDAO;

import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Barak on 21/03/2014.
 */
public class BackgroundService extends Service
{
  // constant
  public static final long NOTIFY_INTERVAL = 3333; // In minutes

  public static boolean ALIVE = false;

  private NotificationManager notificationManager;
  private LocationHandler locationHandler;

  // run on another Thread to avoid crash
  private Handler handler = new Handler();
  // timer handling
  private Timer timer = null;

  // Unique Identification Number for the Notification.
  // We use it on Notification start, and to cancel it.
  private int NOTIFICATION = R.string.local_service_started;

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

    // cancel if already existed
    if(timer != null) {
      timer.cancel();
    }

    // recreate new
    timer = new Timer();
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
          NotesDAO notesDAO = new NotesDAO(BackgroundService.this);
          List<Note> notes = notesDAO.getAllNotes();

//          for (Note note : notes)
//          {
//
//          }

          notesDAO.close();
          showNotification("You have an incoming task! " + getDateTime());
        }

      });
    }

    private String getDateTime()
    {
      return LocalDateTime.now().toString("dd/MM/yyyy HH:mm:ss");
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
    showNotification(getText(R.string.local_service_stopped).toString());
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
  private void showNotification(String text) {
    // The PendingIntent to launch our activity if the user selects this notification
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
      new Intent(this, MainActivity.class), 0);

    // Set the icon, scrolling text and timestamp
    Notification notification;

    String title = "Notifly";
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

      notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
      notification.setLatestEventInfo(this, title , text, contentIntent);
      notification.flags |= Notification.FLAG_AUTO_CANCEL;
      notificationManager.notify(NOTIFICATION, notification);
    } else {
      notification = new NotificationCompat.Builder(this).setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_launcher).setTicker(text).setWhen(System.currentTimeMillis())
        .setAutoCancel(true).setContentTitle(title )
        .setContentText(text).build();

      notificationManager.notify(NOTIFICATION, notification);
    }

    // Send the notification.
    notificationManager.notify(NOTIFICATION, notification);
  }
}
