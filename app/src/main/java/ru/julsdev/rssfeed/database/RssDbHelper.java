package ru.julsdev.rssfeed.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ru.julsdev.rssfeed.models.FeedModel;
import ru.julsdev.rssfeed.models.PostModel;

public class RssDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "rss.db";
    public static final int DATABASE_VERSION = 1;
    private static final String TAG = RssDbHelper.class.getSimpleName();
    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;


    public RssDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        writableDatabase = getWritableDatabase();
        readableDatabase = getReadableDatabase();
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

    public Cursor getAllFeeds() {
        String[] mDataSet = {
                RssContract.FeedsEntry.COLUMN_ID,
                RssContract.FeedsEntry.COLUMN_NAME,
                RssContract.FeedsEntry.COLUMN_URL
        };

        return readableDatabase.query(RssContract.FeedsEntry.TABLE_NAME, mDataSet, null, null, null, null, null);
    }

    public Cursor getPostsByFeed(int feedId) {
        String[] mDataSet = {
                RssContract.PostsEntry.COLUMN_ID,
                RssContract.PostsEntry.COLUMN_TITLE,
                RssContract.PostsEntry.COLUMN_DESCRIPTION,
                RssContract.PostsEntry.COLUMN_DATE,
        };

        return readableDatabase.query(RssContract.PostsEntry.TABLE_NAME, mDataSet, RssContract.PostsEntry.COLUMN_FEED_ID + " = " + feedId, null, null, null, null);
    }

    public long insertFeed(FeedModel feed) {
        ContentValues values = new ContentValues();
        values.put(RssContract.FeedsEntry.COLUMN_NAME, feed.getName());
        values.put(RssContract.FeedsEntry.COLUMN_URL, feed.getUrl());

        return writableDatabase.insert(RssContract.FeedsEntry.TABLE_NAME, null, values);
    }

    public long insertPost(PostModel post) {
        ContentValues values = new ContentValues();
        values.put(RssContract.PostsEntry.COLUMN_TITLE, post.getTitle());
        values.put(RssContract.PostsEntry.COLUMN_DESCRIPTION, post.getDescription());
        values.put(RssContract.PostsEntry.COLUMN_DATE, post.getPublishDate());
        values.put(RssContract.PostsEntry.COLUMN_FEED_ID, post.getFeedId());

        return writableDatabase.insert(RssContract.PostsEntry.TABLE_NAME, null, values);
    }

    public int deleteFeed(int id) {

        return writableDatabase.delete(RssContract.FeedsEntry.TABLE_NAME, RssContract.FeedsEntry.COLUMN_ID + " = " + id, null);
    }

    public int deletePostsByFeed(int feedId) {

        return writableDatabase.delete(RssContract.PostsEntry.TABLE_NAME, RssContract.PostsEntry.COLUMN_FEED_ID + " = " + feedId, null);
    }


}
