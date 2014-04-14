package net.notifly.core.util;

import android.content.Context;
import android.util.Log;

import net.notifly.core.service.BackgroundService;

import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Noam on 13/04/2014.
 */
public class FileUtils
{
    public static final String REMINDER_FILE_NAME = "reminder";

    public static SerializableSparseArray<LocalDateTime> readReminderMapFromFile(String tag, Context context) {
        SerializableSparseArray<LocalDateTime> reminderDateTimeMap =
                new SerializableSparseArray<LocalDateTime>();
        try {
            File file = context.getFileStreamPath(REMINDER_FILE_NAME);
            if (file.exists() && file.length() > 0) {
                Log.d(tag, "read map from file");
                FileInputStream fileInputStream = context.openFileInput(REMINDER_FILE_NAME);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                reminderDateTimeMap = (SerializableSparseArray<LocalDateTime>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
                Log.d(tag, "reminder map size is " + reminderDateTimeMap.size());
            }


        } catch (java.io.IOException e) {
            Log.e(tag, e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            Log.e(tag, e.getMessage(), e);
        }

        return reminderDateTimeMap;
    }

    public static void writeReminderMapToFile(String tag, Context context, boolean isMapChanged,
                                        SerializableSparseArray<LocalDateTime> reminderDateTimeMap) {
        try {
            if (isMapChanged) {
                Log.d(tag, "write map to file");
                FileOutputStream fileOutputStream = context.
                        openFileOutput(FileUtils.REMINDER_FILE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(reminderDateTimeMap);
                Log.d(tag, "reminder map size is " + reminderDateTimeMap.size());
                objectOutputStream.close();
                fileOutputStream.close();
            }

        } catch (java.io.IOException e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public static void deleteNoteFromReminderFile(String tag, Context context, int noteId) {
        Log.d(tag, "deleting note from reminder file");
        // Remove the note from the reminder file and save it, this will ensure the deletion of
        // the note from the file even if the service restarted
        SerializableSparseArray<LocalDateTime> reminderDateTimeMap = readReminderMapFromFile(tag, context);
        reminderDateTimeMap.remove(noteId);
        FileUtils.writeReminderMapToFile(tag, context, true, reminderDateTimeMap);

        Log.d(tag, "push note to modified notes stack");
        // Update the service about the deletion so it won't be rewritten again to the file
        BackgroundService.modifiedNotes.push(noteId);
    }
}
