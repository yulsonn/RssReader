package ru.julsdev.rssfeed.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import static ru.julsdev.rssfeed.database.RssContract.CONTENT_AUTHORITY;
import static ru.julsdev.rssfeed.database.RssContract.FeedsEntry;
import static ru.julsdev.rssfeed.database.RssContract.PATH_FEEDS;
import static ru.julsdev.rssfeed.database.RssContract.PATH_POSTS;
import static ru.julsdev.rssfeed.database.RssContract.PostsEntry;

public class RssProvider extends ContentProvider {

    private final static UriMatcher sUriMatcher = buildUriMatcher();
    private RssDbHelper mOpenHelper;

    static final int FEEDS = 100;
    static final int FEED_BY_ID = 101;
    static final int POSTS = 200;
    static final int POSTS_BY_FEED = 201;

    private static final SQLiteQueryBuilder sPostsByFeedSettingQueryBuilder;
    private static final SQLiteQueryBuilder sFeedByIdSettingQueryBuilder;

    static {
        sPostsByFeedSettingQueryBuilder = new SQLiteQueryBuilder();
        sPostsByFeedSettingQueryBuilder.setTables(
                PostsEntry.TABLE_NAME + " INNER JOIN " +
                        FeedsEntry.TABLE_NAME +
                        " ON " + FeedsEntry.TABLE_NAME +
                        "." + FeedsEntry._ID +
                        " = " + PostsEntry.TABLE_NAME +
                        "." + PostsEntry.COLUMN_FEED_ID
        );

        sFeedByIdSettingQueryBuilder = new SQLiteQueryBuilder();
        sFeedByIdSettingQueryBuilder.setTables(FeedsEntry.TABLE_NAME);

    }

    private static final String sFeedSettingSelection =
            PostsEntry.TABLE_NAME + "." + PostsEntry.COLUMN_FEED_ID + " = ?";

    private static final String sFeedIdSettingSelection =
            FeedsEntry.TABLE_NAME + "." + FeedsEntry._ID + " = ?";

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, PATH_FEEDS, FEEDS);
        matcher.addURI(authority, PATH_FEEDS + "/#", FEED_BY_ID);
        matcher.addURI(authority, PATH_POSTS, POSTS);
        matcher.addURI(authority, PATH_POSTS + "/#", POSTS_BY_FEED);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RssDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            case FEEDS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RssContract.FeedsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case FEED_BY_ID:
                retCursor = getFeedById(uri, projection, sortOrder);
                break;

            case POSTS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RssContract.PostsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case POSTS_BY_FEED:
                retCursor = getPostsByFeed(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FEEDS:
                return FeedsEntry.CONTENT_TYPE;
            case FEED_BY_ID:
                return FeedsEntry.CONTENT_ITEM_TYPE;
            case POSTS:
                return PostsEntry.CONTENT_TYPE;
            case POSTS_BY_FEED:
                return PostsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FEEDS: {
                long _id = db.insert(FeedsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = FeedsEntry.buildFeedsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case POSTS: {
                long _id = db.insert(PostsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PostsEntry.buildPostsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        if (null == selection) selection = "1";

        int rowsDeleted;
        switch (match) {
            case FEEDS:
                rowsDeleted = db.delete(FeedsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case FEED_BY_ID:
                String[] feedIdSetting = new String[] {uri.getLastPathSegment()};
                rowsDeleted = db.delete(FeedsEntry.TABLE_NAME, sFeedIdSettingSelection, feedIdSetting);
                break;

            case POSTS:
                rowsDeleted = db.delete(PostsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case POSTS_BY_FEED:
                String[] postsFeedIdSetting = new String[] {uri.getLastPathSegment()};
                rowsDeleted = db.delete(PostsEntry.TABLE_NAME, sFeedSettingSelection, postsFeedIdSetting);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if (null == selection) selection = "1";

        switch (match) {
            case FEEDS:
                rowsUpdated = db.update(FeedsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case POSTS:
                rowsUpdated = db.update(PostsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    private Cursor getPostsByFeed(Uri uri, String[] projection, String sortOrder) {
        String feedSetting = uri.getLastPathSegment();

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[] {feedSetting};
        selection = sFeedSettingSelection;

        return sPostsByFeedSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getFeedById(Uri uri, String[] projection, String sortOrder) {
        String feedIdSetting = uri.getLastPathSegment();

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[] {feedIdSetting};
        selection = sFeedIdSettingSelection;

        return sFeedByIdSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POSTS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PostsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
