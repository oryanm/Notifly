package net.notifly.core.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.R;
import net.notifly.core.entity.DistanceMatrix;
import net.notifly.core.entity.Note;
import net.notifly.core.gui.activity.main.MainActivity_;
import net.notifly.core.sql.NotesDAO;
import net.notifly.core.util.SerializableSparseArray;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by Barak on 21/03/2014.
 */
@EService
public class BackgroundService extends Service {
    // Constants
    public static final int TIMER_INTERVAL = 30; // In seconds
    private static final String REMINDER_FILE_NAME = "reminder";
    private static final String TAG = "BackgroundService";

    public static boolean ALIVE = false;

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
    private SerializableSparseArray<LocalDateTime> reminderDateTimeMap = new SerializableSparseArray<LocalDateTime>();

    private boolean isServiceWithIntent = true;
    private boolean isMapChanged = false;

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating service");
        ALIVE = true;

        // init joda time
        ResourceZoneInfoProvider.init(this);

        readReminderMapFromFile();
        Log.d(TAG, "Finished creating service");
    }

    private void readReminderMapFromFile() {
        try {
            File file = getFileStreamPath(REMINDER_FILE_NAME);
            if (file.exists()) {
                if (file.length() == 0) return;
                Log.d(TAG, "read map from file");
                FileInputStream fileInputStream = openFileInput(REMINDER_FILE_NAME);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                Object readObject = objectInputStream.readObject();
                reminderDateTimeMap = (SerializableSparseArray<LocalDateTime>) readObject;
                Log.d(TAG, "reminder map size is " + reminderDateTimeMap.size());
                objectInputStream.close();
                fileInputStream.close();
            } else {
                file.createNewFile();
            }

        } catch (java.io.IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void writeReminderMapToFile() {
        try {
            if (isMapChanged) {
                Log.d(TAG, "write map to file");
                FileOutputStream fileOutputStream = openFileOutput(REMINDER_FILE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(reminderDateTimeMap);
                Log.d(TAG, "reminder map size is " + reminderDateTimeMap.size());
                objectOutputStream.close();
                fileOutputStream.close();
                isMapChanged = false;
            }

        } catch (java.io.IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);

        isServiceWithIntent = intent != null;

        // schedule task
        timer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, TIMER_INTERVAL * 1000);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Location currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    LocalDateTime now = LocalDateTime.now();
                    NotesDAO notesDAO = new NotesDAO(BackgroundService.this);
                    List<Note> notes = notesDAO.getAllNotes();
                    notesDAO.close();

                    // First check if any of the notes were deleted
                    checkForDeletedNotes(notes);
                    // Clear the ETA map
                    etaMap.clear();

                    List<Note> notesToNotify = new ArrayList<Note>();
                    for (Note note : notes) {
                        // It's a time based note
                        boolean timeBasedNote = note.getTime() != null;

                        if (timeBasedNote && note.hasLocation()) {
                            handleLocationBasedNote(now, currentLocation, note, notesToNotify, false);
                        }
                        // time based only
                        else if (timeBasedNote) {
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

                    writeReminderMapToFile();

                    if (notesToNotify.size() > 0)
                        showNotification(getShortContent(notesToNotify), getExtendedContent(notesToNotify));
                }

                private void checkForDeletedNotes(List<Note> notes) {
                    Set<Integer> noteIds = new HashSet<Integer>();
                    for (Note note : notes) {
                        noteIds.add(note.getId());
                    }

                    for (int i = 0; i < reminderDateTimeMap.size(); i++) {
                        // If the list of notes doesn't contain the note id which is located in the map then remove it
                        if (!noteIds.contains(reminderDateTimeMap.keyAt(i))) {
                            reminderDateTimeMap.remove(reminderDateTimeMap.keyAt(i));
                            isMapChanged = true;
                        }
                    }
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
                            Log.d(TAG, "noteSafetyTime is after now - next notification will be at " +
                                    scheduleReminder(currentNote, noteSafetyTime));
                        } else {
                            // We're in the warning range
                            if (!isServiceWithIntent) notesToNotify.add(currentNote);
                            Log.d(TAG, "noteSafetyTime isn't after now - next notification will be at "
                                    + scheduleReminder(currentNote, now.plusMinutes(interval)));
                        }
                    }
                }

                private void remindGeneralNote(LocalDateTime now, List<Note> notesToNotify,
                                               Note currentNote) {
                    int interval = getPreferenceValue(getString(R.string.general_reminder_interval_preference_key));
                    if (!remindByInterval(now, notesToNotify, currentNote, interval)) {
                        Log.d(TAG, "next notification will be at " +
                        scheduleReminder(currentNote, now.plusMinutes(interval)));
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
                            Log.d(TAG, "now is after note time  - next notification will be at " +
                                    scheduleReminder(currentNote, now.plusMinutes(interval)));
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

                private void handleLocationBasedNote(LocalDateTime now, Location currentLocation, Note currentNote,
                                                     List<Note> notesToNotify, boolean isLocationOnly) {
                    String org = net.notifly.core.entity.Location.from(currentLocation).toString();
                    String dest = currentNote.getLocation().toString();
                    try {
                        // TODO: Get the mode of transportation from currentNote
                        DistanceMatrix distanceMatrix = new RetreiveDistanceMatrixTask().execute(org, dest,
                                "driving").get();

                        // TODO: check what can we do when Google fails to return distance matrix
                        if (distanceMatrix == null) return;

                        if (isLocationOnly) {
                            int safetyFactor = getPreferenceValue(getString
                                    (R.string.location_only_safety_factor_preference_key));
                            int interval = getPreferenceValue(getString
                                    (R.string.location_only_reminder_interval_preference_key));

                            if (distanceMatrix.getDistance() < safetyFactor) {
                                if (!remindByInterval(now, notesToNotify, currentNote, interval)){
                                    notesToNotify.add(currentNote);
                                    Log.d(TAG, "next notification will be at " +
                                            scheduleReminder(currentNote, now.plusMinutes(interval)));
                                }
                                etaMap.put(currentNote, distanceMatrix);
                            }
                        } else // time and location based currentNote
                        {
                            // Do not notify me about note past time
                            if (now.isAfter(currentNote.getTime())) return;

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
                                int interval = Minutes.minutesBetween(now, departTime).getMinutes()/2;
                                if (!remindByInterval(now, notesToNotify, currentNote, interval))
                                {
                                    notesToNotify.add(currentNote);
                                    Log.d(TAG, "next notification will be at " +
                                            scheduleReminder(currentNote, now.plusMinutes(interval)));
                                }

                                etaMap.put(currentNote, distanceMatrix);
                            }
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                    } catch (ExecutionException e) {
                        Log.e(TAG, e.getMessage(), e);
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
            });
        }
    }

    private int getPreferenceValue(String key) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(BackgroundService.this).
                getString(key, "0"));
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

            notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
            notification.setLatestEventInfo(this, title, text, contentIntent);
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
