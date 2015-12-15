package ru.julsdev.rssfeed.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.database.RssContract;
import ru.julsdev.rssfeed.models.PostModel;
import ru.julsdev.rssfeed.tasks.FeedParserTask;

public class DataLoadUtil {

    private static final String TAG = DataLoadUtil.class.getSimpleName();

    public static void loadInitialData(Context context) {

        int count = 0;
        Cursor cursor = context.getContentResolver().query(RssContract.FeedsEntry.CONTENT_URI, null, null, null, null, null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        if (count < 3) {
            String[] rssTitles = context.getResources().getStringArray(R.array.rss_titles);
            String[] rssUrls = context.getResources().getStringArray(R.array.rss_urls);
            for (int i = count; i < 3; i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(RssContract.FeedsEntry.COLUMN_NAME, rssTitles[i]);
                contentValues.put(RssContract.FeedsEntry.COLUMN_URL, rssUrls[i]);

                context.getContentResolver().insert(RssContract.FeedsEntry.CONTENT_URI, contentValues);
            }
        }

        new FeedParserTask(context).execute();
    }

    public static void insertPosts(Context context, List<PostModel> posts) {

        if (!posts.isEmpty()) {
            context.getContentResolver().delete(RssContract.PostsEntry.buildPostsByFeedUri(posts.get(0).getFeedId()), null, null);
        }

        ArrayList<ContentValues> cvList = new ArrayList<>(posts.size());

        for (int i = 0; i < posts.size(); i++) {
            ContentValues postValues = new ContentValues();

            postValues.put(RssContract.PostsEntry.COLUMN_TITLE, posts.get(i).getTitle());
            postValues.put(RssContract.PostsEntry.COLUMN_DESCRIPTION, posts.get(i).getDescription());
            postValues.put(RssContract.PostsEntry.COLUMN_DATE, posts.get(i).getPublishDate());
            postValues.put(RssContract.PostsEntry.COLUMN_FEED_ID, posts.get(i).getFeedId());

            cvList.add(postValues);
        }

        if (cvList.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvList.size()];
            cvList.toArray(cvArray);

            context.getContentResolver()
                    .bulkInsert(RssContract.PostsEntry.CONTENT_URI, cvArray);

            cvList.clear();
        }
    }
}

