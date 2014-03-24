package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.notifly.core.entity.Location;

public class LocationDAO extends AbstractDAO
{
  public static final String TABLE_NAME = "location";

  public static final String CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME + "(" +
    COLUMNS.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    COLUMNS.LONGITUDE + " REAL NOT NULL, " +
    COLUMNS.LATITUDE + " REAL NOT NULL" + ")";

  public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

  enum COLUMNS
  {
    ID, LONGITUDE, LATITUDE
  }

  public LocationDAO(Context context)
  {
    super(context);
  }

  public LocationDAO(SQLiteDatabase database)
  {
    super(database);
  }

  public long addLocation(Location location)
  {
    ContentValues values = new ContentValues();
    values.put(COLUMNS.LONGITUDE.name(), location.getLongitude());
    values.put(COLUMNS.LATITUDE.name(), location.getLatitude());

    return database.insert(TABLE_NAME, null, values);
  }

  public void deleteNote(Location note)
  {
    database.delete(TABLE_NAME, String.format("%s = ? ", COLUMNS.ID.name()),
      new String[]{String.valueOf(note.getId())});
  }

  public Location getLocation(int id) {
    Cursor cursor = query(database, QueryBuilder
      .select(COLUMNS.ID.name(), COLUMNS.LONGITUDE.name(), COLUMNS.LATITUDE.name())
      .from(TABLE_NAME)
      .where(String.format("%s = ? ", COLUMNS.ID.name()), String.valueOf(id)));
    cursor.moveToFirst();

    Location location = new Location(
      cursor.getInt(COLUMNS.ID.ordinal()),
      cursor.getDouble(COLUMNS.LONGITUDE.ordinal()),
      cursor.getDouble(COLUMNS.LATITUDE.ordinal()));

    cursor.close();
    return location;
  }
}
