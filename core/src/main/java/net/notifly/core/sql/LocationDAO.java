package net.notifly.core.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.notifly.core.entity.Location;

import java.util.ArrayList;
import java.util.List;

import static net.notifly.core.sql.NotiflySQLiteHelper.TRUE;

public class LocationDAO extends AbstractDAO
{
  public static final String TABLE_NAME = "location";

  public static final String CREATE_STATEMENT = " CREATE TABLE " + TABLE_NAME + "(" +
    COLUMNS.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    COLUMNS.NAME + " TEXT NOT NULL, " +
    COLUMNS.LATITUDE + " REAL NOT NULL, " +
    COLUMNS.LONGITUDE + " REAL NOT NULL, " +
    COLUMNS.IS_FAVORITE + " INTEGER NOT NULL, " +
    COLUMNS.TITLE + " TEXT NOT NULL, " +
    COLUMNS.ORDERING + " INTEGER NOT NULL " +
          ")";

  public static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

  enum COLUMNS { ID, NAME, LATITUDE, LONGITUDE, IS_FAVORITE, TITLE, ORDERING}

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
        values.put(COLUMNS.NAME.name(), location.getName());
        values.put(COLUMNS.LATITUDE.name(), location.getLatitude());
        values.put(COLUMNS.LONGITUDE.name(), location.getLongitude());
        values.put(COLUMNS.IS_FAVORITE.name(), location.isFavorite());
        values.put(COLUMNS.TITLE.name(), location.getTitle());
        values.put(COLUMNS.ORDERING.name(), location.getOrder());

        return database.insert(TABLE_NAME, null, values);
    }

    public Location getLocation(int id) {
        Location location = null/*todo: don't return null*/;
        Cursor cursor = query(database, QueryBuilder
                .select(COLUMNS.ID.name(), COLUMNS.NAME.name(),
                        COLUMNS.LATITUDE.name(), COLUMNS.LONGITUDE.name(),
                        COLUMNS.IS_FAVORITE.name(), COLUMNS.TITLE.name(), COLUMNS.ORDERING.name())
                .from(TABLE_NAME)
                .where(String.format("%s = ? ", COLUMNS.ID.name()), String.valueOf(id)));

        if (cursor.moveToFirst()) {

            location = new Location(
                    cursor.getInt(COLUMNS.ID.ordinal()),
                    cursor.getString(COLUMNS.NAME.ordinal()),
                    cursor.getDouble(COLUMNS.LATITUDE.ordinal()),
                    cursor.getDouble(COLUMNS.LONGITUDE.ordinal()),
                    cursor.getInt(COLUMNS.ORDERING.ordinal()));

            if (cursor.getInt(COLUMNS.IS_FAVORITE.ordinal()) == TRUE) {
                location = location.asFavorite(cursor.getString(COLUMNS.TITLE.ordinal()));
            }
        }

        cursor.close();
        return location;
    }

    public List<Location> getFavoriteLocations() {
        List<Location> locations = new ArrayList<Location>();
        Cursor cursor = query(database, QueryBuilder
                .select(COLUMNS.ID.name(), COLUMNS.NAME.name(),
                        COLUMNS.LATITUDE.name(), COLUMNS.LONGITUDE.name(),
                        COLUMNS.IS_FAVORITE.name(), COLUMNS.TITLE.name(), COLUMNS.ORDERING.name())
                .from(TABLE_NAME)
                .where(String.format("%s = ? ", COLUMNS.IS_FAVORITE.name()), String.valueOf(TRUE))
                .orderBy(COLUMNS.ORDERING.name()));

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location(
                        cursor.getInt(COLUMNS.ID.ordinal()),
                        cursor.getString(COLUMNS.NAME.ordinal()),
                        cursor.getDouble(COLUMNS.LATITUDE.ordinal()),
                        cursor.getDouble(COLUMNS.LONGITUDE.ordinal()),
                        cursor.getInt(COLUMNS.ORDERING.ordinal()));

                locations.add(location.asFavorite(cursor.getString(COLUMNS.TITLE.ordinal())));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return locations;
    }

    public void updateAsNotFavorite(Location location) {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.IS_FAVORITE.name(), false);

        database.update(TABLE_NAME, values,
                String.format("%s = ? ", COLUMNS.ID.name()),
                new String[]{String.valueOf(location.getId())});
    }

    public void updateOrder(Location location) {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.ORDERING.name(), location.getOrder());

        database.update(TABLE_NAME, values,
                String.format("%s = ? ", COLUMNS.ID.name()),
                new String[]{String.valueOf(location.getId())});
    }

    public void updateOrder(List<Location> locations) {
        database.beginTransaction();
        try {
            for (Location location : locations) {
                updateOrder(location);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
}
