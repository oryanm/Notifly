package net.notifly.core.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.LocalDateTime;

public class AbstractDAO
{
  SQLiteDatabase database;

  public AbstractDAO(Context context)
  {
    this.database = new NotiflySQLiteHelper(context, null).getWritableDatabase();
  }

  public AbstractDAO(SQLiteDatabase database)
  {
    this.database = database;
  }

  public void close()
  {
    this.database.close();
  }

  public Cursor query(SQLiteDatabase database, QueryBuilder builder)
  {
    return database.query(
      builder.distinct, builder.table, builder.columns,
      builder.selection, builder.selectionArgs,
      builder.groupBy, builder.having, builder.orderBy, null);
  }

  public static LocalDateTime parseTime(String time)
  {
    return LocalDateTime.parse(time, NotiflySQLiteHelper.DATETIME_FORMATTER);
  }
}
