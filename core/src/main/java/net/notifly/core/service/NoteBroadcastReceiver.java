package net.notifly.core.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.notifly.core.entity.Note;
import net.notifly.core.util.FileUtils;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;

@EReceiver
public class NoteBroadcastReceiver extends BroadcastReceiver {
    public static final String DISMISS_ACTION = "android.note.action.dismiss";

    @SystemService
    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DISMISS_ACTION))
        {
            Note note = intent.getParcelableExtra(Note.class.getName());
            BackgroundService.dismissedNotes.add(note.getId());
            FileUtils.writeDismissedNotesToFile(NoteBroadcastReceiver.class.getName(), context);
            notificationManager.cancel(BackgroundService.NOTIFICATION_ID);
        }
    }
}
