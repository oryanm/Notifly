package net.notifly.core.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.joda.time.LocalDateTime;

public class AbstractDAO
{
  SQLiteOpenHelper sqlHelper;

  public AbstractDAO(Context context)
  {
    this.sqlHelper = new NotiflySQLiteHelper(context, null);
  }

  public void close()
  {
    this.sqlHelper.close();
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
