package ru.julsdev.rssfeed.ui.fragments;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.adapters.FeedsAdapter;
import ru.julsdev.rssfeed.constants.BundleArgConstants;
import ru.julsdev.rssfeed.database.RssContract;
import ru.julsdev.rssfeed.tasks.FeedParserTask;
import ru.julsdev.rssfeed.utils.InputValidationUtil;

@EFragment(R.layout.fragment_feeds)
public class FeedsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, BundleArgConstants{

    @ViewById(R.id.feed_fab)
    FloatingActionButton fab;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @StringRes(R.string.fragment_feeds_title)
    String title;

    @StringRes(R.string.msg_add_feed_success)
    String msgFeedAddSuccess;

    @StringRes(R.string.msg_add_feed_fail)
    String msgFeedAddFail;

    @StringRes(R.string.msg_remove_feed_success)
    String msgFeedRemoveSuccess;

    @StringRes(R.string.msg_remove_feed_fail)
    String msgFeedRemoveFail;

    @StringRes(R.string.hint_name)
    String hintName;

    @StringRes(R.string.hint_address)
    String hintAddress;

    private RecyclerView recyclerView;
    private FeedsAdapter adapter;

    private static final int FEEDS_LOADER = 0;

    private static final String[] FEEDS_COLUMNS = {
            RssContract.FeedsEntry._ID,
            RssContract.FeedsEntry.COLUMN_NAME,
            RssContract.FeedsEntry.COLUMN_URL
    };

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

        feedNameWrapper.setHint(hintName);
        feedUrlWrapper.setHint(hintAddress);

        Button okButton = (Button) dialog.findViewById(R.id.btn_ok);
        Button cancelButton = (Button) dialog.findViewById(R.id.btn_cancel);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputValidationUtil.validateFeedFields(name.toString(), feedNameWrapper, url.toString(), feedUrlWrapper)) {

                    ContentValues cv = new ContentValues();
                    cv.put(RssContract.FeedsEntry.COLUMN_NAME, name.toString());
                    cv.put(RssContract.FeedsEntry.COLUMN_URL, url.toString());

                    Uri newFeed = getContext().getContentResolver().insert(RssContract.FeedsEntry.CONTENT_URI, cv);

                    if (newFeed != null) {
                        Cursor cursor = getContext().getContentResolver().query(newFeed, new String[]{RssContract.FeedsEntry._ID}, null, null, null);

                        int res = -1;
                        if (cursor != null && cursor.moveToFirst()) {
                            res = Integer.parseInt(cursor.getString(cursor.getColumnIndex(RssContract.FeedsEntry._ID)));
                            cursor.close();
                        }

                        if (res > -1) {
                            Toast.makeText(getContext(), msgFeedAddSuccess, Toast.LENGTH_SHORT).show();
                            new FeedParserTask(url.toString(), res, getContext()).execute();
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(getContext(), msgFeedAddFail, Toast.LENGTH_SHORT).show();
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
        View rootView = inflater.inflate(R.layout.fragment_feeds, container, false);
        getActivity().setTitle(title);
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.feeds_recycler);

        initRecyclerView();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new FeedsAdapter(null, new FeedsAdapter.ViewHolder.ClickListener() {
            @Override
            public void onItemClicked(int position) {
                openFeedPosts(position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FEEDS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int cnt = 0;
                int id = -1;
                Cursor cursor = adapter.getItem(viewHolder.getAdapterPosition());
                if (cursor != null) {
                    id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(RssContract.FeedsEntry._ID)));
                }

                if (id > -1 ) {
                    cnt = getContext().getContentResolver().delete(RssContract.FeedsEntry.buildFeedByIdUri(id), null, null);
                }

                if (cnt > 0) {
                    Toast.makeText(getContext(), msgFeedRemoveSuccess, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), msgFeedRemoveFail, Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void openFeedPosts(int position) {
        long id = adapter.getItemId(position);
        Cursor cursor = adapter.getItem(position);
        String title = cursor.getString(cursor.getColumnIndex(RssContract.FeedsEntry.COLUMN_NAME));
        PostsFragment postsFragment = new PostsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_ID, id);
        bundle.putString(ARG_TITLE, title);
        postsFragment.setArguments(bundle);

        FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, postsFragment).addToBackStack(null).commit();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = RssContract.FeedsEntry._ID + " DESC";

        return new CursorLoader(getActivity(),
                RssContract.FeedsEntry.CONTENT_URI,
                FEEDS_COLUMNS,
                null,
                null,
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
