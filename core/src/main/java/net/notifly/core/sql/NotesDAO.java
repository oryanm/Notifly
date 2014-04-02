package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import net.notifly.core.entity.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesDAO extends AbstractDAO
{
  public static final String TABLE_NAME = "note";

  public static final String CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME + "(" +
    COLUMNS.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    COLUMNS.TITLE + " TEXT NOT NULL, " +
    COLUMNS.DESCRIPTION + " TEXT, " +
    COLUMNS.TIME + " DATETIME, " +
    COLUMNS.LOCATION + " INT, " +
    " FOREIGN KEY( " + COLUMNS.LOCATION + " ) REFERENCES " +
    LocationDAO.TABLE_NAME + "( " + LocationDAO.COLUMNS.ID + " ) " + ")";

  public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

  enum COLUMNS
  {
    /* todo: might need to change to _ID due to content provider, BaseColumns._ID?. */
    ID, TITLE, DESCRIPTION, LOCATION, TIME
  }

  public NotesDAO(Context context)
  {
    super(context);
  }

  public long addNote(Note note)
  {
    long id = -1;
    database.beginTransaction();

    try
    {
      ContentValues values = new ContentValues();
      values.put(COLUMNS.TITLE.name(), note.getTitle());
      values.put(COLUMNS.DESCRIPTION.name(), note.getDescription());
      if (note.getLocation() != null)
      {
        values.put(COLUMNS.LOCATION.name(), new LocationDAO(database)
                .addLocationIfNotExists(note.getLocation()));
      }
      if (note.getTime() != null)
      {
        values.put(COLUMNS.TIME.name(), note.getTime().toString(NotiflySQLiteHelper.DATETIME_PATTERN));
      }

      id = database.insert(TABLE_NAME, null, values);
      database.setTransactionSuccessful();
    }
    finally
    {
      database.endTransaction();
    }

    return id;
  }

  public void deleteNote(Note note)
  {
    database.delete(TABLE_NAME, String.format("%s = ? ", COLUMNS.ID.name()),
      new String[]{String.valueOf(note.getId())});
  }

  public List<Note> getAllNotes()
  {
    List<Note> notes = new ArrayList<Note>();
    Cursor cursor = query(database, QueryBuilder
      .select(COLUMNS.ID.name(), COLUMNS.TITLE.name(),
        COLUMNS.DESCRIPTION.name(), COLUMNS.LOCATION.name(), COLUMNS.TIME.name())
      .from(TABLE_NAME));

    if (cursor.moveToFirst())
    {
      do
      {
        Note note = new Note();
        note.setId(cursor.getInt(COLUMNS.ID.ordinal()));
        note.setTitle(cursor.getString(COLUMNS.TITLE.ordinal()));
        String time = cursor.getString(COLUMNS.TIME.ordinal());
        if (time != null) note.setTime(parseTime(time));
        note.setDescription(cursor.getString(COLUMNS.DESCRIPTION.ordinal()));
        int locId = cursor.getInt(COLUMNS.LOCATION.ordinal());
        if (locId != 0) note.setLocation(new LocationDAO(database).getLocation(locId));
        notes.add(note);
      } while (cursor.moveToNext());
    }

    cursor.close();

    return notes;
  }
}
