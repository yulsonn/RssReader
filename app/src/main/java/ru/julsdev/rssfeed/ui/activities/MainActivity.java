package ru.julsdev.rssfeed.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import ru.julsdev.rssfeed.R;
import ru.julsdev.rssfeed.constants.ActionConstants;
import ru.julsdev.rssfeed.ui.fragments.FeedsFragment;
import ru.julsdev.rssfeed.ui.fragments.FeedsFragment_;
import ru.julsdev.rssfeed.utils.DataLoadUtil;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements ActionConstants {

    @StringRes(R.string.msg_error_no_internet)
    String msgErrorNoInternet;

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
        Toast.makeText(MainActivity.this, msgErrorNoInternet, Toast.LENGTH_SHORT).show();
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataLoadUtil.loadInitialData(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedsFragment_()).commit();
        }
    }
}
