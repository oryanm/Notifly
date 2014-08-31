package net.notifly.core.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import net.danlew.android.joda.ResourceZoneInfoProvider;
import net.notifly.core.Notifly;
import net.notifly.core.R;
import net.notifly.core.SvmPredict;
import net.notifly.core.SvmTrain;
import net.notifly.core.entity.DistanceMatrix;
import net.notifly.core.entity.Note;
import net.notifly.core.entity.SVMVector;
import net.notifly.core.gui.activity.main.MainActivity_;
import net.notifly.core.util.FileUtils;
import net.notifly.core.util.LocationHandler;
import net.notifly.core.util.SerializableSparseArray;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Barak on 21/03/2014.
 */
@EService
public class BackgroundService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    // Constants
    public static final int TIMER_INTERVAL = 30; // In seconds
    private static final String TAG = "BackgroundService";

    // static members
    public static boolean ALIVE = false;
    public static Stack<Integer> modifiedNotes = new Stack<Integer>();
    public static Set<Integer> dismissedNotes = new HashSet<Integer>();
    private Set<Integer> notesPastDepartTime = new HashSet<Integer>();

    @App
    Notifly notifly;

    @SystemService
    NotificationManager notificationManager;

    // run on another Thread to avoid crash
    private Handler handler = new Handler();
    // timer handling
    private Timer timer = new Timer();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    public static int NOTIFICATION_ID = R.string.local_service_started;

    private Map<Note, DistanceMatrix> etaMap = new HashMap<Note, DistanceMatrix>();
    private SerializableSparseArray<LocalDateTime> reminderDateTimeMap;
    private Map<Note, SVMVector> noteToSvmVectorMap = new HashMap<Note, SVMVector>();

    private boolean isMapChanged = false;

    private LocationClient locationClient;

    private List<Note> notesToNotify = new ArrayList<Note>();

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating service");
        ALIVE = true;

        // init joda time
        ResourceZoneInfoProvider.init(this);

        setUpLocationClient();

        reminderDateTimeMap = FileUtils.readReminderMapFromFile(TAG, this);
        dismissedNotes = FileUtils.readDismissedFromFile(TAG, this);

        Log.d(TAG, "Finished creating service");
    }

    private void setUpLocationClient() {
        if (locationClient == null) locationClient = new LocationClient(this, this, this);
        if (!locationClient.isConnected() || !locationClient.isConnecting())
            locationClient.connect();
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
                    Location currentLocation = locationClient.isConnected() ? locationClient.getLastLocation() : null;
                    LocalDateTime now = LocalDateTime.now();

                    // Clear all
                    etaMap.clear();
                    notesToNotify.clear();

                    for (Note note : notifly.getNotes()) {
                        // We don't want to process a modified or a dismissed note
                        if (modifiedNotes.contains(note.getId()) || dismissedNotes.contains(note.getId()))
                            continue;

                        if (note.hasTime() && note.hasLocation()) {
                            handleLocationBasedNote(now, currentLocation, note, false);
                        }
                        // time based only
                        else if (note.hasTime()) {
                            remindTimedNote(now, note);
                        }
                        // location based only
                        else if (note.hasLocation()) {
                            handleLocationBasedNote(now, currentLocation, note, true);
                        } else // neither time nor location
                        {
                            remindGeneralNote(now, note);
                        }
                    }

                    // Write current reminder map to reminder map file
                    writeReminderMapToFile();

                    if (notesToNotify.size() > 0)
                        showNotification(getShortContent(), getExtendedContent());
                }
            });
        }
    }

    private void writeReminderMapToFile() {
        // Remove the modified notes from reminder map
        while (!modifiedNotes.isEmpty())
        {
            Integer noteId = modifiedNotes.pop();
            reminderDateTimeMap.remove(noteId);
            notesPastDepartTime.remove(noteId);
        }

        FileUtils.writeReminderMapToFile(TAG, BackgroundService.this, isMapChanged, reminderDateTimeMap);
        isMapChanged = false;
    }

    private void remindTimedNote(LocalDateTime now, Note currentNote) {
        // Do not notify me about note past time
        if (now.isAfter(currentNote.getTime())) return;

        int safetyFactor = getPreferenceValue(getString(R.string.time_only_safety_factor_preference_key));
        int interval = getPreferenceValue(getString(R.string.time_only_reminder_interval_preference_key));

        // If an entry was set, do remind me by the predetermined interval
        if (!remindByInterval(now, currentNote, interval)) {
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

    private void remindGeneralNote(LocalDateTime now, Note currentNote) {
        int interval = getPreferenceValue(getString(R.string.general_reminder_interval_preference_key));
        if (!remindByInterval(now, currentNote, interval)) {
            writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
        }
    }

    // Returns a boolean value which indicates if an entry for the note exists
    private boolean remindByInterval(LocalDateTime now, Note currentNote, int interval) {
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
                                         boolean isLocationOnly) {
        if (currentLocation == null) return;

        String org = LocationHandler.getLatitudeLongitudeString(currentLocation);
        String dest = LocationHandler.getLatitudeLongitudeString(currentNote.getLocation());
        try {
            DistanceMatrix distanceMatrix = LocationHandler.
                    getDistanceMatrixUsingTask(org, dest, currentNote.getTravelMode().toString());

            // TODO: check what can we do when Google fails to return distance matrix
            if (distanceMatrix == null) return;

            etaMap.put(currentNote, distanceMatrix);

            if (isLocationOnly) {
                remindLocationNote(now, currentNote, distanceMatrix);
            } else // time and location based currentNote
            {
                // Do not notify me about note past time
                if (now.isAfter(currentNote.getTime()))
                {
                    // Meaning we have arrived to the destination
                    if (distanceMatrix.getDistance() < 100)
                    {
                        noteToSvmVectorMap.get(currentNote).setLate
                                (Minutes.minutesBetween(now, currentNote.getTime()).getMinutes());

                        SvmTrain.GetInstance(this).
                                svmSaveVector(noteToSvmVectorMap.get(currentNote).toObjectArray());
                    }
                    notesPastDepartTime.remove(currentNote.getId());
                    return;
                }
                remindTimeLocationNote(now, currentNote, distanceMatrix);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void remindLocationNote(LocalDateTime now, Note currentNote, DistanceMatrix distanceMatrix) {
        int safetyFactor = getPreferenceValue(getString
                (R.string.location_only_safety_factor_preference_key));
        int interval = getPreferenceValue(getString
                (R.string.location_only_reminder_interval_preference_key));

        if (distanceMatrix.getDistance() <= safetyFactor) {
            if (!remindByInterval(now, currentNote, interval)) {
                notesToNotify.add(currentNote);
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
            }
        }
    }

    private void remindTimeLocationNote(LocalDateTime now, Note currentNote,
                                        DistanceMatrix distanceMatrix) throws IOException {
        LocalDateTime departTime =
                currentNote.getTime().minusSeconds((int) distanceMatrix.getDuration());

        if (now.isAfter(departTime)) {
            // TODO: save the time as start time for learning (end time when arrived)
            notesPastDepartTime.add(currentNote.getId());

            int interval = 2;
            if (!remindByInterval(now, currentNote, interval)) {
                notesToNotify.add(currentNote);
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
            }
            return;
        }

        if (!noteToSvmVectorMap.containsKey(currentNote))
        {
            noteToSvmVectorMap.put(currentNote, new SVMVector(0, distanceMatrix.getDuration()/60.0,
                    distanceMatrix.getDistance()/1000.0,
                    currentNote.getTravelMode().ordinal(), currentNote.getTime().hourOfDay().get(),
                    currentNote.getTime().minuteOfHour().get(),
                    currentNote.getTime().dayOfWeek().get(), 0));
        }

        int time = SvmPredict.getInstance(this).Calc(noteToSvmVectorMap.get(currentNote).toObjectArray());
        int safetyFactor = time == 0 ? getPreferenceValue(getString
                (R.string.time_location_safety_factor_preference_key)) : time;

        // Check if we'll not make it on time (note's time minus ETA is in warning range)
        LocalDateTime noteSafetyTime = departTime.minusMinutes(safetyFactor);
        if (now.isAfter(noteSafetyTime)) {
            // Calculate interval
            int interval = (int) Math.ceil(Minutes.minutesBetween(now, departTime).getMinutes() / (float) 2);
            if (interval == 0) return;
            if (!remindByInterval(now, currentNote, interval)) {
                noteToSvmVectorMap.get(currentNote).setEstimation(Minutes.minutesBetween(now, currentNote.getTime()).getMinutes());
                notesToNotify.add(currentNote);
                writeNotificationToLog(currentNote, scheduleReminder(currentNote, now.plusMinutes(interval)));
            }
        }
    }

    private String getShortContent() {
        return "You have " + notesToNotify.size() + " incoming task" +
                ((notesToNotify.size() > 1) ? "s!" : "!");
    }

    private String getExtendedContent() {
        String msg = "Incoming tasks: \n";
        for (int i = 0; i < notesToNotify.size(); i++) {
            Note note = notesToNotify.get(i);
            msg += (i + 1) + ". " + note.getTitle();
            // It's a time based note
            boolean timeBasedNote = note.getTime() != null;
            // It's a location based note
            boolean locationBasedNote = note.getLocation() != null;

            if (timeBasedNote && locationBasedNote) {
                if (notesPastDepartTime.contains(note.getId())){
                    msg += "You'll be late ";
                }
                msg += " ,ETA: " + etaMap.get(note).getDurationText();
            }
            if (timeBasedNote) {
                msg += " , due time: " + note.getTime().toString("HH:mm");
            } else if (locationBasedNote) {
                msg += " , in " + etaMap.get(note).getDistanceText() + " within " +
                        etaMap.get(note).getDurationText();
            }

            msg += "\n";
        }
        return msg;
    }

    private void writeNotificationToLog(Note currNote, LocalDateTime atTime) {
        Log.d(TAG, currNote.getTitle() + " next notification will be at " + atTime);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying service");
        ALIVE = false;
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

        // Building the notification
        String title = "Notifly";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentIntent(contentIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setTicker(text).setWhen(System.currentTimeMillis()).setAutoCancel(true)
                .setContentTitle(title).setSmallIcon(R.drawable.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(text);

        // If there's only one note to notify on, display dismiss option and travel mode icon
        if (notesToNotify.size() == 1)
        {
            // Init the broadcast receiver intent
            Intent broadcastIntent = new Intent(this, NoteBroadcastReceiver_.class);

            broadcastIntent.putExtra(Note.class.getName(), notesToNotify.iterator().next());
            broadcastIntent.setAction(NoteBroadcastReceiver.DISMISS_ACTION);

            // The PendingIntent to dismiss notes
            PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);

            builder.addAction(R.drawable.abc_ic_clear, "Dismiss", dismissPendingIntent);

            Note note = notesToNotify.iterator().next();
            if (note.hasLocation()) {
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().
                        getIdentifier("ic_" + note.getTravelMode() + "_selected", "drawable", getPackageName())));
            }
        }

        Notification notification = builder.build();

        // Setting vibration, sound and led light
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        notification.ledARGB = Color.CYAN;
        notification.ledOnMS = 500;
        notification.ledOffMS = 500;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;

        // Send the notification.
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
