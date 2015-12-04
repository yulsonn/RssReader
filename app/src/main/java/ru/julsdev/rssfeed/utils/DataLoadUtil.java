package ru.julsdev.rssfeed.utils;

import android.content.Context;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.database.RssDbHelper;
import ru.julsdev.rssfeed.models.FeedModel;
import ru.julsdev.rssfeed.tasks.FeedParserTask;

public class DataLoadUtil {

    private static final String TAG = DataLoadUtil.class.getSimpleName();

    public static void loadInitialData(RssDbHelper dbHelper, Context context) {
        int count = dbHelper.getAllFeeds().getCount();
        if (count < 3) {
            String[] rssTitles = context.getResources().getStringArray(R.array.rss_titles);
            String[] rssUrls = context.getResources().getStringArray(R.array.rss_urls);
            for (int i = count; i < 3; i++) {
                FeedModel feed = new FeedModel();
                feed.setName(rssTitles[i]);
                feed.setUrl(rssUrls[i]);
                dbHelper.insertFeed(feed);
            }
        }

        new FeedParserTask(context).execute();
    }
}

