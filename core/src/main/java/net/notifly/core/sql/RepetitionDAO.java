package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.notifly.core.entity.Note;
import net.notifly.core.entity.Repetition;

import static net.notifly.core.sql.NotiflySQLiteHelper.DATE_PATTERN;

public class RepetitionDAO extends AbstractDAO {
    public static final String TABLE_NAME = "repetition";

    public static final String CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME + "(" +
            COLUMNS.NOTE_ID + " INTEGER PRIMARY KEY, " +
            COLUMNS.TYPE + " INT NOT NULL, " +
            COLUMNS.INTERVAL + " INT NOT NULL, " +
            COLUMNS.START + " DATE NOT NULL, " +
            COLUMNS.END + " DATE, " +
            " FOREIGN KEY( " + COLUMNS.NOTE_ID + " ) REFERENCES " +
            NotesDAO.TABLE_NAME + "( " + NotesDAO.COLUMNS.ID + " ) " + ")";

    public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    enum COLUMNS {
        NOTE_ID, TYPE, INTERVAL, START, END
    }

    public RepetitionDAO(Context context) {
        super(context);
    }

    public RepetitionDAO(SQLiteDatabase database) {
        super(database);
    }

    public void addOrUpdate(Repetition repetition) {
        Repetition oldLocation = getRepetition(repetition.getNote());

        if (oldLocation == null) {
            addRepetition(repetition);
        } else {
            updateRepetition(repetition);
        }
    }

    public void addRepetition(Repetition repetition) {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.NOTE_ID.name(), repetition.getNote().getId());
        values.put(COLUMNS.TYPE.name(), repetition.getType().ordinal());
        values.put(COLUMNS.INTERVAL.name(), repetition.getInterval());
        values.put(COLUMNS.START.name(), repetition.getStart().toString(DATE_PATTERN));
        values.put(COLUMNS.END.name(), repetition.hasEnd() ? repetition.getEnd().toString(DATE_PATTERN) : null);

        database.insert(TABLE_NAME, null, values);
    }

    public void updateRepetition(Repetition repetition) {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.TYPE.name(), repetition.getType().ordinal());
        values.put(COLUMNS.INTERVAL.name(), repetition.getInterval());
        values.put(COLUMNS.START.name(), repetition.getStart().toString(DATE_PATTERN));
        values.put(COLUMNS.END.name(), repetition.hasEnd() ? repetition.getEnd().toString(DATE_PATTERN) : null);

        database.update(TABLE_NAME, values,
                String.format("%s = ? ", COLUMNS.NOTE_ID.name()),
                new String[]{String.valueOf(repetition.getNote().getId())});

        repetition.getNote().getId();
    }

    public void deleteRepetition(Repetition repetition) {
        deleteRepetition(repetition.getNote().getId());
    }

    public void deleteRepetition(int id) {
        database.delete(TABLE_NAME, String.format("%s = ? ", COLUMNS.NOTE_ID.name()),
                new String[]{String.valueOf(id)});
    }

    public Repetition getRepetition(Note note) {
        Repetition repetition = null;
        Cursor cursor = query(database, QueryBuilder
                .select(COLUMNS.NOTE_ID.name(), COLUMNS.TYPE.name(), COLUMNS.INTERVAL.name(),
                        COLUMNS.START.name(), COLUMNS.END.name())
                .from(TABLE_NAME)
                .where(String.format("%s = ? ", COLUMNS.NOTE_ID.name()), String.valueOf(note.getId())));

        if (cursor.moveToFirst()) {

            repetition = Repetition
                    .repeat(note)
                    .every(cursor.getInt(COLUMNS.INTERVAL.ordinal()),
                            Repetition.TYPE.values()[cursor.getInt(COLUMNS.TYPE.ordinal())])
                    .from(parseDate(cursor.getString(COLUMNS.START.ordinal())));

            String end = cursor.getString(COLUMNS.END.ordinal());

            if (end != null) {
                repetition.until(parseDate(end));
            }
        }

        cursor.close();
        return repetition;
    }
}
