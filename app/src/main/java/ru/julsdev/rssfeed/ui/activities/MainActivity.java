package ru.julsdev.rssfeed.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.database.RssDbHelper;
import ru.julsdev.rssfeed.ui.fragments.FeedsFragment;
import ru.julsdev.rssfeed.ui.fragments.FeedsFragment_;
import ru.julsdev.rssfeed.utils.DataLoadUtil;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    public static final String SWIPE_START_ACTION = "start_swipe_refresh";
    public static final String SWIPE_STOP_ACTION = "stop_swipe_refresh";
    public static final String NO_INTERNET = "no_internet";

    private static final String TAG = MainActivity.class.getSimpleName();

    public RssDbHelper getDbHelper() {
        return dbHelper;
    }

    private RssDbHelper dbHelper;

    @ViewById(R.id.fragment_container)
    View container;

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void init() {
        initToolbar();

    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    @Receiver(actions = SWIPE_START_ACTION)
    protected void startSwipeRefresh() {
        swipeRefreshVisible(true);
    }

    @Receiver(actions = SWIPE_STOP_ACTION)
    protected void stopSwipeRefresh() {
        swipeRefreshVisible(false);
    }

    @Receiver(actions = NO_INTERNET)
    protected void noInternetMsg() {
        Toast.makeText(MainActivity.this, "Нет подключения к интернету...", Toast.LENGTH_SHORT).show();
    }

    void swipeRefreshVisible(boolean isVisible) {
        Fragment current = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current instanceof FeedsFragment) {
            SwipeRefreshLayout swipe = ((FeedsFragment) current).getSwipeRefreshLayout();
            if (swipe != null) {
                swipe.setRefreshing(isVisible);
            }
        }
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new RssDbHelper(this);
        DataLoadUtil.loadInitialData(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedsFragment_()).commit();
        }
    }
}
