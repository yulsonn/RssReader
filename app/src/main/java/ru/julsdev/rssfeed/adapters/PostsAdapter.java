package ru.julsdev.rssfeed.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.database.RssContract;

public class PostsAdapter extends CursorRecyclerViewAdapter<PostsAdapter.ViewHolder>{

    private ViewHolder.ClickListener clickListener;

    public PostsAdapter() {
    }

    public PostsAdapter(Cursor cursor, ViewHolder.ClickListener clickListener) {
        super(cursor);
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Cursor cursor = getItem(position);

        holder.postTitle.setText(cursor.getString(cursor.getColumnIndex(RssContract.PostsEntry.COLUMN_TITLE)));
        holder.postDesc.setText(cursor.getString(cursor.getColumnIndex(RssContract.PostsEntry.COLUMN_DESCRIPTION)));
        holder.postDate.setText(cursor.getString(cursor.getColumnIndex(RssContract.PostsEntry.COLUMN_DATE)));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ClickListener clickListener;

        protected TextView postTitle;
        protected TextView postDesc;
        protected TextView postDate;

        public ViewHolder(View itemView, ClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;
            postTitle = (TextView) itemView.findViewById(R.id.post_title);
            postDesc = (TextView) itemView.findViewById(R.id.post_desc);
            postDate = (TextView) itemView.findViewById(R.id.post_date);

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
