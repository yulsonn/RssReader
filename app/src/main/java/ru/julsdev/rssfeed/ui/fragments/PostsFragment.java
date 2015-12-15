package ru.julsdev.rssfeed.ui.fragments;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.EFragment;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.adapters.PostsAdapter;
import ru.julsdev.rssfeed.database.RssContract;

@EFragment(R.layout.fragment_posts)
public class PostsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerView;
    private PostsAdapter adapter;

    private int feedId = -1;

    private static final int POSTS_LOADER = 1;

    private static final String[] POSTS_COLUMNS = {
            RssContract.PostsEntry._ID,
            RssContract.PostsEntry.COLUMN_TITLE,
            RssContract.PostsEntry.COLUMN_DESCRIPTION,
            RssContract.PostsEntry.COLUMN_DATE,
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        getActivity().setTitle("Posts");
        feedId = (int)getArguments().getLong("ARG_ID");

        recyclerView = (RecyclerView) rootView.findViewById(R.id.posts_recycler);

        initRecyclerView();

        return rootView;
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new PostsAdapter(null, new PostsAdapter.ViewHolder.ClickListener() {
            @Override
            public void onItemClicked(int position) {
               openPostDetails(position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(POSTS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void openPostDetails(int position) {
        final Cursor cursor = adapter.getItem(position);

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_details);
        final TextView tvTitle = (TextView) dialog.findViewById(R.id.details_title);
        final TextView tvText = (TextView) dialog.findViewById(R.id.detail_text);

        tvTitle.setText(cursor.getString(cursor.getColumnIndex(RssContract.PostsEntry.COLUMN_TITLE)));
        tvText.setText(cursor.getString(cursor.getColumnIndex(RssContract.PostsEntry.COLUMN_DESCRIPTION)));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (feedId == -1) {
            Toast.makeText(getActivity(), "Posts not found...", Toast.LENGTH_SHORT).show();
            return null;
        }

        String sortOrder = RssContract.PostsEntry._ID + " DESC";
        String selection = RssContract.PostsEntry.COLUMN_FEED_ID + " = ?";
        String[] selectionArgs  = new String[]{String.valueOf(feedId)};

        return new CursorLoader(getActivity(),
                RssContract.PostsEntry.CONTENT_URI,
                POSTS_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
