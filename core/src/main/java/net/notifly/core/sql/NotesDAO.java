package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.notifly.core.Note;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class NotesDAO
{
  public static final String NAME = "note";

  public static final String CREATE_STATEMENT = " CREATE TABLE " + NAME + "(" +
    COLUMNS.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    COLUMNS.TITLE + " TEXT NOT NULL, " +
    COLUMNS.TIME + " DATETIME " + ")";

  public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + NAME;

  enum COLUMNS
  {
    /* todo: might need to change to _ID due to content provider, BaseColumns._ID?. */
    ID, TITLE, TIME
  }

  SQLiteOpenHelper sqlHelper;

  public NotesDAO(Context context)
  {
    this.sqlHelper = new NotiflySQLiteHelper(context, null);
  }

  public void close()
  {
    this.sqlHelper.close();
  }

  public void addNote(Note note)
  {
    SQLiteDatabase database = sqlHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(COLUMNS.TITLE.name(), note.getTitle());
    values.put(COLUMNS.TIME.name(), note.getTime().toString(NotiflySQLiteHelper.DATETIME_PATTERN));

    database.insert(NAME, null, values);
    database.close();
  }

  public List<Note> getAllNotes()
  {
    // TODO: move to JDK 1.7
    List<Note> notes = new ArrayList<Note>();
    SQLiteDatabase database = sqlHelper.getWritableDatabase();
    Cursor cursor = query(database, QueryBuilder.select(NAME,
      COLUMNS.ID.name(), COLUMNS.TITLE.name(), COLUMNS.TIME.name()));

    if (cursor.moveToFirst())
    {
      do
      {
        Note note = new Note(
          cursor.getString(COLUMNS.TITLE.ordinal()),
          parseTime(cursor.getString(COLUMNS.TIME.ordinal())));
        notes.add(note);
      } while (cursor.moveToNext());
    }

    cursor.close();
    database.close();

    return notes;
  }

  private LocalDateTime parseTime(String time)
  {
    return LocalDateTime.parse(time, NotiflySQLiteHelper.DATETIME_FORMATTER);
  }

  public Cursor query(SQLiteDatabase database, QueryBuilder builder)
  {
    return database.query(
      builder.distinct, builder.table, builder.columns,
      builder.selection, builder.selectionArgs,
      builder.groupBy, builder.having, builder.orderBy, null);
  }
}
