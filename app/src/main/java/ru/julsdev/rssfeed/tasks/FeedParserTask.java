package ru.julsdev.rssfeed.tasks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.julsdev.rssfeed.database.RssContract;
import ru.julsdev.rssfeed.database.RssDbHelper;
import ru.julsdev.rssfeed.models.PostModel;
import ru.julsdev.rssfeed.ui.activities.MainActivity;
import ru.julsdev.rssfeed.utils.NetworkConnectionChecker;


public class FeedParserTask extends AsyncTask<Void, Void, Void> {

    public static final String CHANNEL = "channel";
    public static final String ITEM = "item";

    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String PUBLISHEDDATE = "pubDate";

    private String title;
    private String description;
    private String pubDate;

    private String urlString;
    private int feedId;
    private Context context;
    private RssDbHelper dbHelper;

    public FeedParserTask(Context context) {
        this.context = context;
    }

    public FeedParserTask(String urlString, int feedId, Context context) {
        this.urlString = urlString;
        this.feedId = feedId;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "Обновление данных...", Toast.LENGTH_SHORT).show();
        swipeRefreshStart();
    }
    @Override
    protected Void doInBackground(Void... params) {
        if (NetworkConnectionChecker.isConnected(context)) {

            dbHelper = new RssDbHelper(context);
            if (urlString != null) {
                parsePostsForFeed();
            } else {
                parsePostsForAllFeeds();
            }
        } else {
            noInternetMsg();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "Обновление данных завершено", Toast.LENGTH_SHORT).show();
        swipeRefreshStop();
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    public void parsePostsForAllFeeds() {
        Cursor cursor = dbHelper.getAllFeeds();
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                feedId = cursor.getInt(cursor.getColumnIndex(RssContract.FeedsEntry.COLUMN_ID));
                urlString = cursor.getString(cursor.getColumnIndex(RssContract.FeedsEntry.COLUMN_URL));
                parsePostsForFeed();
            }
            cursor.close();
        }
    }


    public List<PostModel> parsePostsForFeed() {
        List<PostModel> postsList = new ArrayList<>();
            try {
                InputStream urlStream = downloadUrl(urlString);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(urlStream, null);

                int eventType = parser.getEventType();
                boolean done = false;
                String tagName;

                while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                    tagName = parser.getName();

                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if (tagName.equals(TITLE)) {
                                title = parser.nextText();
                            }
                            if (tagName.equals(DESCRIPTION)) {
                                description = Html.fromHtml(parser.nextText()).toString();
                            }
                            if (tagName.equals(PUBLISHEDDATE)) {
                                pubDate = parser.nextText();
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (tagName.equals(CHANNEL)) {
                                done = true;
                            } else if (tagName.equals(ITEM)) {
                                PostModel post = new PostModel(title, description, pubDate, feedId);
                                postsList.add(post);

                                Log.i("FEED PARSER", "post title: " + title + " pubDate: " + pubDate + " desc: " + description.substring(0, 20).trim());
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        if (!postsList.isEmpty()) {
            saveToDb(postsList);
        }

        return postsList;
    }

    private void saveToDb(List<PostModel> posts) {
        Cursor cursor = dbHelper.getPostsByFeed(feedId);
        if (cursor.getCount() > 0) {
            dbHelper.deletePostsByFeed(feedId);
            cursor.close();
        }

        for (PostModel post : posts) {
            dbHelper.insertPost(post);
        }
    }

    void swipeRefreshStart() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.SWIPE_START_ACTION);
        context.sendBroadcast(broadcastIntent);
    }

    void swipeRefreshStop() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.SWIPE_STOP_ACTION);
        context.sendBroadcast(broadcastIntent);
    }

    void noInternetMsg() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.NO_INTERNET);
        context.sendBroadcast(broadcastIntent);
    }

}

