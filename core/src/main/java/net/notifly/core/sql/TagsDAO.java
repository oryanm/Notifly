package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.notifly.core.entity.Note;

import java.util.HashSet;
import java.util.Set;

public class TagsDAO extends AbstractDAO {
    public static final String TABLE_NAME = "tag";

    public static final String CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME + "(" +
            COLUMNS.TAG + " TEXT NOT NULL, " +
            COLUMNS.NOTE + " INT NOT NULL, " +
            " FOREIGN KEY( " + COLUMNS.NOTE + " ) REFERENCES " +
            LocationDAO.TABLE_NAME + "( " + LocationDAO.COLUMNS.ID + " ))";

    public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public TagsDAO(Context context) {
        super(context);
    }

    public TagsDAO(SQLiteDatabase database) {
        super(database);
    }

    enum COLUMNS {
        TAG, NOTE
    }

    public long addTag(Note note, String tag) {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.TAG.name(), tag);
        values.put(COLUMNS.NOTE.name(), note.getId());

        return database.insert(TABLE_NAME, null, values);
    }

    public void updateTags(Note note) {
        database.beginTransaction();
        try {
            deleteTags(note);

            for (String tag : note.getTags()) {
                addTag(note, tag);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void deleteTags(Note note) {
        database.delete(TABLE_NAME, String.format("%s = ? ", COLUMNS.NOTE.name()),
                new String[]{String.valueOf(note.getId())});
    }

    public Set<String> getTags(Note note) {
        Set<String> tags = new HashSet<String>();
        Cursor cursor = query(database, QueryBuilder
                .selectDistinct(COLUMNS.TAG.name(), COLUMNS.NOTE.name())
                .from(TABLE_NAME)
                .where(String.format("%s = ? ", COLUMNS.NOTE.name()), String.valueOf(note.getId())));

        if (cursor.moveToFirst()) {
            do {
                tags.add(cursor.getString(COLUMNS.TAG.ordinal()));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return tags;
    }

    public Set<String> getTags() {
        Set<String> tags = new HashSet<String>();
        Cursor cursor = query(database, QueryBuilder
                .selectDistinct(COLUMNS.TAG.name())
                .from(TABLE_NAME));

        if (cursor.moveToFirst()) {
            do {
                tags.add(cursor.getString(COLUMNS.TAG.ordinal()));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return tags;
    }
}
