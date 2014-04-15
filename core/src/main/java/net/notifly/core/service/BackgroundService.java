package net.notifly.core.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.Notifly;
import net.notifly.core.R;
import net.notifly.core.entity.DistanceMatrix;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.MainActivity_;
import net.notifly.core.util.FileUtils;
import net.notifly.core.util.LocationHandler;
import net.notifly.core.util.SerializableSparseArray;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Barak on 21/03/2014.
 */
@EService
public class BackgroundService extends Service {
    @App
    Notifly notifly;

    // Constants
    public static final int TIMER_INTERVAL = 30; // In seconds
    private static final String TAG = "BackgroundService";

    // static members
    public static boolean ALIVE = false;
    public static Stack<Integer> modifiedNotes = new Stack<Integer>();

    @SystemService
    NotificationManager notificationManager;

    @SystemService
    LocationManager locationManager;

    // run on another Thread to avoid crash
    private Handler handler = new Handler();
    // timer handling
    private Timer timer = new Timer();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    private Map<Note, DistanceMatrix> etaMap = new HashMap<Note, DistanceMatrix>();
    private SerializableSparseArray<LocalDateTime> reminderDateTimeMap;

    private boolean isMapChanged = false;

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating service");
        ALIVE = true;

        // init joda time
        ResourceZoneInfoProvider.init(this);

        reminderDateTimeMap = FileUtils.readReminderMapFromFile(TAG, this);
        Log.d(TAG, "Finished creating service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);

        // schedule task
        timer.scheduleAtFixedRate(new NotiflyTimerTask(), 0, TIMER_INTERVAL * 1000);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private class NotiflyTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Location currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    LocalDateTime now = LocalDateTime.now();

                    // Clear the ETA map
                    etaMap.clear();

                    List<Note> notesToNotify = new ArrayList<Note>();
                    for (Note note : notifly.getNotes()) {
                        if (note.hasTime() && note.hasLocation()) {
                            handleLocationBasedNote(now, currentLocation, note, notesToNotify, false);
                        }
                        // time based only
                        else if (note.hasTime()) {
                            remindTimedNote(now, notesToNotify, note);
                        }
                        // location based only
                        else if (note.hasLocation()) {
                            handleLocationBasedNote(now, currentLocation, note, notesToNotify, true);
                        } else // neither time nor location
                        {
                            remindGeneralNote(now, notesToNotify, note);
                        }
                    }

                    // Write current reminder map to reminder map file
                    writeReminderMapToFile();

                    if (notesToNotify.size() > 0)
                        showNotification(getShortContent(notesToNotify), getExtendedContent(notesToNotify));
                }
            });
        }
    }

    private void writeReminderMapToFile() {
        // Remove the modified notes from reminder map
        while (!modifiedNotes.isEmpty()) reminderDateTimeMap.remove(modifiedNotes.pop());

        FileUtils.writeReminderMapToFile(TAG, BackgroundService.this, isMapChanged, reminderDateTimeMap);
        isMapChanged = false;
    }

    private void remindTimedNote(LocalDateTime now, List<Note> notesToNotify, Note currentNote) {
        // Do not notify me about note past time
        if (now.isAfter(currentNote.getTime())) return;

        int safetyFactor = getPreferenceValue(getString(R.string.time_only_safety_factor_preference_key));
        int interval = getPreferenceValue(getString(R.string.time_only_reminder_interval_preference_key));

        // If an entry was set, do remind me by the predetermined interval
        if (!remindByInterval(now, notesToNotify, currentNote, interval)) {
            // Else calculate the safety time
            LocalDateTime noteSafetyTime = currentNote.getTime().minusMinutes(safetyFactor);
            // we are not in the warning range
            if (noteSafetyTime.isAfter(now)) {
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, noteSafetyTime));
            } else {
                // We're in the warning range
                notesToNotify.add(currentNote);
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
            }
        }
    }

    private void remindGeneralNote(LocalDateTime now, List<Note> notesToNotify,
                                   Note currentNote) {
        int interval = getPreferenceValue(getString(R.string.general_reminder_interval_preference_key));
        if (!remindByInterval(now, notesToNotify, currentNote, interval)) {
            writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
        }
    }

    // Returns a boolean value which indicates if an entry for the note exists
    private boolean remindByInterval(LocalDateTime now, List<Note> notesToNotify, Note currentNote,
                                     int interval) {
        // If the current note has been enlisted to the reminder file
        boolean entryExist = reminderDateTimeMap.get(currentNote.getId()) != null;
        if (entryExist) {
            if (now.isAfter(reminderDateTimeMap.get(currentNote.getId()))) {
                notesToNotify.add(currentNote);
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
            }
        }

        return entryExist;
    }

    private LocalDateTime scheduleReminder(Note currentNote, LocalDateTime atTime) {
        isMapChanged = true;
        atTime = atTime.withSecondOfMinute(0).withMillisOfSecond(0);
        reminderDateTimeMap.put(currentNote.getId(), atTime);
        return atTime;
    }

    private int getPreferenceValue(String key) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(BackgroundService.this).
                getString(key, "0"));
    }

    private void handleLocationBasedNote(LocalDateTime now, Location currentLocation, Note currentNote,
                                         List<Note> notesToNotify, boolean isLocationOnly) {
        String org = LocationHandler.getLatitudeLongitudeString(currentLocation);
        String dest = LocationHandler.getLatitudeLongitudeString(currentNote.getLocation());
        try {
            // TODO: Get the mode of transportation from currentNote
            DistanceMatrix distanceMatrix = new RetreiveDistanceMatrixTask().execute(org, dest, "driving").get();

            // TODO: check what can we do when Google fails to return distance matrix
            if (distanceMatrix == null) return;

            if (isLocationOnly) {
                remindLocationNote(now, currentNote, notesToNotify, distanceMatrix);
            } else // time and location based currentNote
            {
                // Do not notify me about note past time
                if (now.isAfter(currentNote.getTime())) return;
                remindTimeLocationNote(now, currentNote, notesToNotify, distanceMatrix);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void remindLocationNote(LocalDateTime now, Note currentNote, List<Note> notesToNotify, DistanceMatrix distanceMatrix) {
        int safetyFactor = getPreferenceValue(getString
                (R.string.location_only_safety_factor_preference_key));
        int interval = getPreferenceValue(getString
                (R.string.location_only_reminder_interval_preference_key));

        if (distanceMatrix.getDistance() <= safetyFactor) {
            if (!remindByInterval(now, notesToNotify, currentNote, interval)){
                notesToNotify.add(currentNote);
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
            }
            etaMap.put(currentNote, distanceMatrix);
        }
    }

    private void remindTimeLocationNote(LocalDateTime now, Note currentNote, List<Note> notesToNotify, DistanceMatrix distanceMatrix) {
        LocalDateTime departTime = currentNote.getTime().minusSeconds((int) distanceMatrix.getDuration());
        if (now.isAfter(departTime))
        {
            // TODO: decide what to do when we are past depart time
            return;
        }

        int safetyFactor = getPreferenceValue(getString
                (R.string.time_location_safety_factor_preference_key));

        // Check if we'll not make it on time (note's time minus ETA is in warning range)
        LocalDateTime noteSafetyTime = departTime.minusMinutes(safetyFactor);
        if (now.isAfter(noteSafetyTime)) {
            // Calculate interval
            int interval = (int) Math.ceil(Minutes.minutesBetween(now, departTime).getMinutes() / (float) 2);
            if (!remindByInterval(now, notesToNotify, currentNote, interval))
            {
                notesToNotify.add(currentNote);
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
            }

            etaMap.put(currentNote, distanceMatrix);
        }
    }

    private String getShortContent(List<Note> notesToNotify) {
        return "You have " + notesToNotify.size() + " incoming task" +
                ((notesToNotify.size() > 0) ? "s!" : "!");
    }

    private String getExtendedContent(List<Note> notesToNotify) {
        String msg = "Incoming tasks: \n";
        for (int i = 0; i < notesToNotify.size(); i++) {
            Note note = notesToNotify.get(i);
            msg += (i + 1) + ". " + note.getTitle();
            // It's a time based note
            boolean timeBasedNote = note.getTime() != null;
            // It's a location based note
            boolean locationBasedNote = note.getLocation() != null;

            if (timeBasedNote && locationBasedNote) {
                msg += " ,ETA: " + etaMap.get(note).getDurationText();
            } else if (timeBasedNote) {
                msg += " , due time: " + note.getTime().toString("HH:mm");
            } else if (locationBasedNote) {
                msg += " , in " + etaMap.get(note).getDistanceText() + " within " +
                        etaMap.get(note).getDurationText();
            }

            msg += "\n";
        }
        return msg;
    }

    private void writeNotificationToLog(Note currNote, LocalDateTime atTime)
    {
        Log.d(TAG, currNote.getTitle() + " next notification will be at " + atTime);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying service");
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
        return null;
    }

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
        notification = new NotificationCompat.Builder(this).setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher).setTicker(text).setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(text).build();

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
