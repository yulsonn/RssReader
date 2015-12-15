package ru.julsdev.rssfeed.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RssDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "rss.db";
    public static final int DATABASE_VERSION = 1;
    private static final String TAG = RssDbHelper.class.getSimpleName();

    public RssDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(RssContract.FeedsEntry.CREATE_FEEDS_TABLE);
            db.execSQL(RssContract.PostsEntry.CREATE_POSTS_TABLE);
        } catch (SQLException e) {
            Log.e(TAG, "" + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(RssContract.FeedsEntry.DROP_FEEDS_TABLE);
            db.execSQL(RssContract.PostsEntry.DROP_POSTS_TABLE);
            onCreate(db);
        } catch (SQLException e) {
            Log.e(TAG, "" + e);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
