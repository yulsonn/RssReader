package ru.julsdev.rssfeed.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class RssContract {

    public static final String CONTENT_AUTHORITY = "ru.julsdev.rssfeed";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FEEDS = "feeds";
    public static final String PATH_POSTS = "posts";


    public static final class FeedsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FEEDS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FEEDS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FEEDS;

        public static final String TABLE_NAME = "feeds";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_URL = "url";

        public static final String CREATE_FEEDS_TABLE =
                "CREATE TABLE " + TABLE_NAME +
                        " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_URL + " TEXT NOT NULL" +
                        " );";

        public static final String DROP_FEEDS_TABLE = "DROP TABLE IF EXISTS" + TABLE_NAME;

        public static Uri buildFeedsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFeedByIdUri(int id) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
        }

    }

    public static final class PostsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;

        public static final String TABLE_NAME = "posts";

        public static final String COLUMN_FEED_ID = "feed_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String CREATE_POSTS_TABLE =
                "CREATE TABLE " + TABLE_NAME +
                        " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_FEED_ID + " INTEGER," +
                        COLUMN_TITLE + " TEXT NOT NULL, " +
                        COLUMN_DATE + " TEXT NOT NULL, " +
                        COLUMN_DESCRIPTION + " TEXT NOT NULL, " +

                        "FOREIGN KEY (" + COLUMN_FEED_ID + ") REFERENCES " +
                        FeedsEntry.TABLE_NAME + " (" + FeedsEntry._ID + ") ON DELETE CASCADE" +
                        ");";

        public static final String DROP_POSTS_TABLE = "DROP TABLE IF EXISTS" + TABLE_NAME;

        public static Uri buildPostsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPostsByFeedUri(int feedId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(feedId)).build();
        }
    }
}
