package ru.julsdev.rssfeed.ui.fragments;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.adapters.FeedsAdapter;
import ru.julsdev.rssfeed.database.RssDbHelper;
import ru.julsdev.rssfeed.models.FeedModel;
import ru.julsdev.rssfeed.tasks.FeedParserTask;
import ru.julsdev.rssfeed.utils.InputValidationUtil;

@EFragment(R.layout.fragment_feeds)
public class FeedsFragment extends Fragment {

    private static final String TAG = FeedsFragment.class.getSimpleName();

    @ViewById(R.id.feeds_recycler)
    RecyclerView recyclerView;

    @ViewById(R.id.feed_fab)
    FloatingActionButton fab;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    RssDbHelper dbHelper;
    FeedsAdapter adapter;

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    @AfterViews
    void ready() {

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FeedParserTask(getContext()).execute();
            }
        });

        initRecyclerView();
    }

    public void stopSwipe() {
        swipeRefreshLayout.setEnabled(false);
    }

    @Click(R.id.feed_fab)
    void addFeed() {
        showAddFeedDialog();
    }

    private void showAddFeedDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_add_feed);
        final EditText etName = (EditText) dialog.findViewById(R.id.new_feed_name);
        final EditText etUrl = (EditText) dialog.findViewById(R.id.new_feed_url);
        final TextInputLayout feedNameWrapper = (TextInputLayout) dialog.findViewById(R.id.feedNameWrapper);
        final TextInputLayout feedUrlWrapper = (TextInputLayout) dialog.findViewById(R.id.feedUrlWrapper);
        final Editable name = etName.getText();
        final Editable url = etUrl.getText();

        feedNameWrapper.setHint("Name");
        feedUrlWrapper.setHint("Address");

        Button okButton = (Button) dialog.findViewById(R.id.btn_ok);
        Button cancelButton = (Button) dialog.findViewById(R.id.btn_cancel);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputValidationUtil.validateFeedFields(name.toString(), feedNameWrapper, url.toString(), feedUrlWrapper)) {
                    long res = adapter.insertItem(new FeedModel(name.toString(), url.toString()), dbHelper);
                    if (res > -1) {
                        Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                        adapter.swapCursor(dbHelper.getAllFeeds());
                        new FeedParserTask(url.toString(),(int)res, getContext()).execute();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Fail", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().setTitle("Feeds");
        dbHelper = new RssDbHelper(getContext());

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

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int cnt = adapter.removeItem(viewHolder.getAdapterPosition(), dbHelper);
                if (cnt > 0) {
                    Toast.makeText(getContext(), "Feed removed successfully", Toast.LENGTH_SHORT).show();
                    adapter.swapCursor(dbHelper.getAllFeeds());
                } else {
                    Toast.makeText(getContext(), "Removing failed", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadData() {
        getLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                final AsyncTaskLoader<Cursor> loader = new AsyncTaskLoader<Cursor>(getActivity()) {

                    @Override
                    public Cursor loadInBackground() {
                        return dbHelper.getAllFeeds();
                    }
                };
                loader.forceLoad();
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                adapter = new FeedsAdapter(data, new FeedsAdapter.ViewHolder.ClickListener() {
                    @Override
                    public void onItemClicked(int position) {
                        openFeedPosts(position);
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

    private void openFeedPosts(int position) {
        long id = adapter.getItemId(position);
        PostsFragment postsFragment = new PostsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong("ARG_ID", id);
        postsFragment.setArguments(bundle);

        FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, postsFragment).addToBackStack(null).commit();
    }
}
