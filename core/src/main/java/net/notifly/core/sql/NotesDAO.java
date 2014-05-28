package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.notifly.core.entity.Note;
import net.notifly.core.util.TravelMode;

import java.util.ArrayList;
import java.util.List;

import static net.notifly.core.sql.NotiflySQLiteHelper.DATETIME_PATTERN;

public class NotesDAO extends AbstractDAO {
    public static final String TABLE_NAME = "note";

    public static final String CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME + "(" +
            COLUMNS.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMNS.TITLE + " TEXT NOT NULL, " +
            COLUMNS.DESCRIPTION + " TEXT, " +
            COLUMNS.TIME + " DATETIME, " +
            COLUMNS.LOCATION + " INT, " +
            COLUMNS.TRAVEL_MODE + " TEXT, " +
            " FOREIGN KEY( " + COLUMNS.LOCATION + " ) REFERENCES " +
            LocationDAO.TABLE_NAME + "( " + LocationDAO.COLUMNS.ID + " ))";

    public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    enum COLUMNS {
        /* todo: might need to change to _ID due to content provider, BaseColumns._ID?. */
        ID, TITLE, DESCRIPTION, LOCATION, TIME, TRAVEL_MODE
    }

    public NotesDAO(Context context) {
        super(context);
    }

    public long addOrUpdateNote(Note note) {
        if (note.getId() != -1) {
            return updateNote(note);
        } else {
            return addNote(note);
        }
    }

    public long addNote(Note note) {
        long id = -1;
        database.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMNS.TITLE.name(), note.getTitle());
            values.put(COLUMNS.DESCRIPTION.name(), note.getDescription());
            if (note.hasLocation()) {
                values.put(COLUMNS.LOCATION.name(),
                        new LocationDAO(database).addLocationIfNotExists(note.getLocation()));
            }

            if (note.hasTime()) {
                values.put(COLUMNS.TIME.name(), note.getTime().toString(DATETIME_PATTERN));

                if (note.repeats()) {
                    new RepetitionDAO(database).addRepetition(note.getRepetition());
                }
            }

            values.put(COLUMNS.TRAVEL_MODE.name(), note.getTravelMode().toString());

            id = database.insert(TABLE_NAME, null, values);

            new TagsDAO(database).updateTags(note);

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        return id;
    }

    public long updateNote(Note note) {
        database.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMNS.TITLE.name(), note.getTitle());
            values.put(COLUMNS.DESCRIPTION.name(), note.getDescription());
            values.put(COLUMNS.LOCATION.name(), note.getLocation() != null ?
                    new LocationDAO(database).addLocationIfNotExists(note.getLocation()) : null);
            values.put(COLUMNS.TIME.name(), note.getTime() != null ?
                    note.getTime().toString(DATETIME_PATTERN) : null);

            if (note.repeats()) {
                new RepetitionDAO(database).addOrUpdate(note.getRepetition());
            } else {
                new RepetitionDAO(database).deleteRepetition(note.getId());
            }

            values.put(COLUMNS.TRAVEL_MODE.name(), note.getTravelMode().toString());

            database.update(TABLE_NAME, values,
                    String.format("%s = ? ", COLUMNS.ID.name()),
                    new String[]{String.valueOf(note.getId())});

            new TagsDAO(database).updateTags(note);

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        return note.getId();
    }


    public void deleteNote(Note note) {
        if (note.repeats()) {
            new RepetitionDAO(database).deleteRepetition(note.getRepetition());
        }

        database.delete(TABLE_NAME, String.format("%s = ? ", COLUMNS.ID.name()),
                new String[]{String.valueOf(note.getId())});
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<Note>();
        Cursor cursor = query(database, QueryBuilder
                .select(COLUMNS.ID.name(), COLUMNS.TITLE.name(), COLUMNS.DESCRIPTION.name(),
                        COLUMNS.LOCATION.name(), COLUMNS.TIME.name(), COLUMNS.TRAVEL_MODE.name())
                .from(TABLE_NAME));

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(COLUMNS.ID.ordinal()));
                note.setTitle(cursor.getString(COLUMNS.TITLE.ordinal()));
                String time = cursor.getString(COLUMNS.TIME.ordinal());
                if (time != null) note.setTime(parseTime(time));
                note.setDescription(cursor.getString(COLUMNS.DESCRIPTION.ordinal()));
                int locId = cursor.getInt(COLUMNS.LOCATION.ordinal());
                if (locId != 0) note.setLocation(new LocationDAO(database).getLocation(locId));
                note.setRepetition(new RepetitionDAO(database).getRepetition(note));
                note.setTravelMode(TravelMode.getMode(cursor.getString(COLUMNS.TRAVEL_MODE.ordinal())));
                note.setTags(new TagsDAO(database).getTags(note));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return notes;
    }
}
