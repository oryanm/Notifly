package net.notifly.core.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class NotiflySQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "notifly.db";

    /**
     * The database version.
     * Change this const when the scheme has changed.
     * Changing this const will trigger the {@code onUpgrade} method
     * the next time a database instance will be created
     */
    public static final int DATABASE_VERSION = 13;

    /**
     * Default pattern used by SQLite for datetime columns
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern(DATETIME_PATTERN);

    /**
     * Const representing boolean values in SQLite
     */
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public NotiflySQLiteHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NotesDAO.CREATE_STATEMENT);
        db.execSQL(LocationDAO.CREATE_STATEMENT);
        db.execSQL(TagsDAO.CREATE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // put migration scripts here
        db.execSQL(NotesDAO.DROP_STATEMENT);
        db.execSQL(LocationDAO.DROP_STATEMENT);
        db.execSQL(TagsDAO.DROP_STATEMENT);
        onCreate(db);
    }
}
