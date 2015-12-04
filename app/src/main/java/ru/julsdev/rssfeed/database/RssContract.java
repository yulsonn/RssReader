package ru.julsdev.rssfeed.database;

import android.provider.BaseColumns;

public class RssContract {

    public static final class FeedsEntry implements BaseColumns {

        public static final String TABLE_NAME = "feeds";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_URL = "url";

        public static final String CREATE_FEEDS_TABLE =
                "CREATE TABLE " + TABLE_NAME +
                        " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_URL + " TEXT NOT NULL" +
                        " );";

        public static final String DROP_FEEDS_TABLE =
                "DROP TABLE IF EXISTS" + TABLE_NAME;

    }

    public static final class PostsEntry implements BaseColumns {

        public static final String TABLE_NAME = "posts";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_FEED_ID = "feed_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String CREATE_POSTS_TABLE =
                "CREATE TABLE " + TABLE_NAME +
                        " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_FEED_ID + " INTEGER," +
                        COLUMN_TITLE + " TEXT NOT NULL, " +
                        COLUMN_DATE + " TEXT NOT NULL, " +
                        COLUMN_DESCRIPTION + " TEXT NOT NULL " +
                        " );";

        public static final String DROP_POSTS_TABLE =
                "DROP TABLE IF EXISTS" + TABLE_NAME;

    }

}
