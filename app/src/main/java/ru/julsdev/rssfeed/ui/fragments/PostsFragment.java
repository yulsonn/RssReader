package ru.julsdev.rssfeed.ui.fragments;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.adapters.PostsAdapter;
import ru.julsdev.rssfeed.database.RssContract;
import ru.julsdev.rssfeed.database.RssDbHelper;

@EFragment(R.layout.fragment_posts)
public class PostsFragment extends Fragment {

    @ViewById(R.id.posts_recycler)
    RecyclerView recyclerView;

    private RssDbHelper dbHelper;
    private PostsAdapter adapter;
    private int feedId;

    @AfterViews
    void ready() {
        initRecyclerView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Posts");
        dbHelper = new RssDbHelper(getContext());
        feedId = (int)getArguments().getLong("ARG_ID");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        getLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                final AsyncTaskLoader<Cursor> loader = new AsyncTaskLoader<Cursor>(getActivity()) {

                    @Override
                    public Cursor loadInBackground() {
                        return dbHelper.getPostsByFeed(feedId);
                    }
                };
                loader.forceLoad();
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                adapter = new PostsAdapter(data, new PostsAdapter.ViewHolder.ClickListener() {
                    @Override
                    public void onItemClicked(int position) {
                        openPostDetails(position);
                    }
                });
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
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
}
