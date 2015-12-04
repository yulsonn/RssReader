package ru.julsdev.rssfeed.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.database.RssContract;
import ru.julsdev.rssfeed.database.RssDbHelper;
import ru.julsdev.rssfeed.models.FeedModel;

public class FeedsAdapter extends CursorRecyclerViewAdapter<FeedsAdapter.ViewHolder>{

    private static final String TAG = FeedsAdapter.class.getSimpleName();
    private ViewHolder.ClickListener clickListener;

    public FeedsAdapter() {
    }

    public FeedsAdapter(Cursor cursor, ViewHolder.ClickListener clickListener) {
        super(cursor);
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        return new ViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Cursor cursor = getItem(position);

        holder.feedName.setText(cursor.getString(cursor.getColumnIndex(RssContract.FeedsEntry.COLUMN_NAME)));
        holder.feedUrl.setText(cursor.getString(cursor.getColumnIndex(RssContract.FeedsEntry.COLUMN_URL)));
    }

    public long insertItem(FeedModel feed, RssDbHelper dbHelper) {
        return dbHelper.insertFeed(feed);
    }

    public int removeItem(int position, RssDbHelper dbHelper) {
        final Cursor cursor = getItem(position);
        int index = cursor.getColumnIndex(RssContract.FeedsEntry.COLUMN_ID);

        return dbHelper.deleteFeed(cursor.getInt(index));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ClickListener clickListener;

        protected TextView feedName;
        protected TextView feedUrl;

        public ViewHolder(View itemView, ClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;

            feedName = (TextView) itemView.findViewById(R.id.feed_name);
            feedUrl = (TextView) itemView.findViewById(R.id.feed_url);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onItemClicked(getAdapterPosition());
            }
        }


        public interface ClickListener {

            void onItemClicked(int position);
        }
    }
}
