package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.notifly.core.entity.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationDAO extends AbstractDAO
{
  public static final String TABLE_NAME = "location";

  public static final String CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME + "(" +
    COLUMNS.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    COLUMNS.LATITUDE + " REAL NOT NULL, " +
    COLUMNS.LONGITUDE + " REAL NOT NULL, " +
    COLUMNS.IS_FAVORITE + " INTEGER NOT NULL, " +
    COLUMNS.TITLE + " TEXT NOT NULL " +
          ")";

  public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

  enum COLUMNS { ID, LATITUDE, LONGITUDE, IS_FAVORITE, TITLE }

  public LocationDAO(Context context)
  {
    super(context);
  }

  public LocationDAO(SQLiteDatabase database)
  {
    super(database);
  }

    public long addLocationIfNotExists(Location location) {
        Location oldLocation = getLocation(location.getId());

        if (oldLocation == null) {
            return addLocation(location);
        } else {
            return oldLocation.getId();
        }
    }

    public long addLocation(Location location) {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.LATITUDE.name(), location.getLatitude());
        values.put(COLUMNS.LONGITUDE.name(), location.getLongitude());
        values.put(COLUMNS.IS_FAVORITE.name(), location.isFavorite());
        values.put(COLUMNS.TITLE.name(), location.getTitle());

        return database.insert(TABLE_NAME, null, values);
    }

    public Location getLocation(int id) {
        Location location = null/*todo: don't return null*/;
        Cursor cursor = query(database, QueryBuilder
                .select(COLUMNS.ID.name(), COLUMNS.LATITUDE.name(), COLUMNS.LONGITUDE.name(),
                        COLUMNS.IS_FAVORITE.name(), COLUMNS.TITLE.name())
                .from(TABLE_NAME)
                .where(String.format("%s = ? ", COLUMNS.ID.name()), String.valueOf(id)));

        if (cursor.moveToFirst()) {

            location = new Location(
                    cursor.getInt(COLUMNS.ID.ordinal()),
                    cursor.getDouble(COLUMNS.LATITUDE.ordinal()),
                    cursor.getDouble(COLUMNS.LONGITUDE.ordinal()));

            if (cursor.getInt(COLUMNS.IS_FAVORITE.ordinal()) == 1) {
                location = location.asFavorite(cursor.getString(COLUMNS.TITLE.ordinal()));
            }
        }

        cursor.close();
        return location;
    }

    public List<Location> getFavoriteLocations() {
        List<Location> locations = new ArrayList<Location>();
        Cursor cursor = query(database, QueryBuilder
                .select(COLUMNS.ID.name(), COLUMNS.LATITUDE.name(), COLUMNS.LONGITUDE.name(),
                        COLUMNS.IS_FAVORITE.name(), COLUMNS.TITLE.name())
                .from(TABLE_NAME)
                .where(String.format("%s = ? ", COLUMNS.IS_FAVORITE.name()), String.valueOf(1)));

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location(
                        cursor.getInt(COLUMNS.ID.ordinal()),
                        cursor.getDouble(COLUMNS.LATITUDE.ordinal()),
                        cursor.getDouble(COLUMNS.LONGITUDE.ordinal()));

                locations.add(location.asFavorite(cursor.getString(COLUMNS.TITLE.ordinal())));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return locations;
    }
}
