package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    COLUMNS.TIME + " DATETIME " + ")";

  public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

  enum COLUMNS
  {
    /* todo: might need to change to _ID due to content provider, BaseColumns._ID?. */
    ID, TITLE, DESCRIPTION, TIME
  }

  SQLiteOpenHelper sqlHelper;

  public NotesDAO(Context context)
  {
    super(context);
  }

  public void addNote(Note note)
  {
    SQLiteDatabase database = sqlHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(COLUMNS.TITLE.name(), note.getTitle());
    values.put(COLUMNS.DESCRIPTION.name(), note.getDescription());
    values.put(COLUMNS.TIME.name(), note.getTime().toString(NotiflySQLiteHelper.DATETIME_PATTERN));

    database.insert(TABLE_NAME, null, values);
    database.close();
  }

  public void deleteNote(Note note)
  {
    SQLiteDatabase database = sqlHelper.getWritableDatabase();
    database.delete(TABLE_NAME, String.format("%s = ? ", COLUMNS.ID.name()),
      new String[]{String.valueOf(note.getId())});
    database.close();
  }

  public List<Note> getAllNotes()
  {
    List<Note> notes = new ArrayList<Note>();
    SQLiteDatabase database = sqlHelper.getReadableDatabase();
    Cursor cursor = query(database, QueryBuilder
      .select(COLUMNS.ID.name(), COLUMNS.TITLE.name(),
        COLUMNS.DESCRIPTION.name(), COLUMNS.TIME.name())
      .from(TABLE_NAME));

    if (cursor.moveToFirst())
    {
      do
      {
        Note note = new Note();
        note.setId(cursor.getInt(COLUMNS.ID.ordinal()));
        note.setTitle(cursor.getString(COLUMNS.TITLE.ordinal()));
        note.setTime(parseTime(cursor.getString(COLUMNS.TIME.ordinal())));
        note.setDescription(cursor.getString(COLUMNS.DESCRIPTION.ordinal()));
        notes.add(note);
      } while (cursor.moveToNext());
    }

    cursor.close();
    database.close();

    return notes;
  }
}
