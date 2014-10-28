package com.itic.mobile.baseactivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public abstract class BaseActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener{
    // Navigation drawer 主菜单
    private DrawerLayout mDrawerLayout;
    private LPreviewUtilsBase.ActionBarDrawerToggleWrapper mDrawerToggle;

    private LPreviewUtilsBase mLPreviewUtils;

    private ViewGroup mDrawerItemsListContainer;

    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();

    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    protected static final int NAVDRAWER_ITEM_SHOW_ZQ = 0;
    protected static final int NAVDRAWER_ITEM_SHOW_CONTACTS = 1;
    protected static final int NAVDRAWER_ITEM_SHOW_MAP = 2;
    protected static final int NAVDRAWER_ITEM_DOWNLOAD_DATA = 3;
    protected static final int NAVDRAWER_ITEM_ABOUT = 4;

    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navigation_drawer_item_show_zq,
            R.string.navigation_drawer_item_show_contacts,
            R.string.navigation_drawer_item_show_map,
            R.string.navigation_drawer_item_download_data,
            R.string.navigation_drawer_item_about
    };

    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_drawer_my_schedule,
            R.drawable.ic_drawer_people_met,
            R.drawable.ic_drawer_map,
            R.drawable.ic_action_download,
            R.drawable.ic_action_about
    };

    // 设置一个延迟时间，保证drawer关闭后再构造界面
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // 在主菜单选择界面时，淡出时间为150ms
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    // 在主菜单选择界面时，淡入时间为250ms
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    //主菜单item列表
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();
    //主菜单item元素对应的视图对相关
    private View[] mNavDrawerItemViews = null;

    // 控制ActionBar自动打开关闭的行为
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;

    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mLPreviewUtils = LPreviewUtils.getInstance(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    private void setupNavDrawer() {
        Log.i("BaseActivity","setupNavDrawer");
        int selfItem = getSelfNavDrawerItem();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null){
            Log.i("BaseActivity","mDrawerLayout is null");
            return;
        }
        if (selfItem == NAVDRAWER_ITEM_INVALID){
            // do not show a nav drawer
            Log.i("BaseActivity","mDrawerLayout need make null");
            View navDrawer = mDrawerLayout.findViewById(R.id.navdrawer);
            if (navDrawer != null){
                ((ViewGroup)navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        mDrawerToggle = mLPreviewUtils.setupDrawerToggle(mDrawerLayout,new DrawerLayout.DrawerListener(){

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,Gravity.START);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        populateNavDrawer();
        mDrawerToggle.syncState();
        if (!PrefUtils.isWelcomeDone(this)) {
            // first run of the app starts with the nav drawer open
            PrefUtils.markWelcomeDone(this);
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zaiqing, menu);
        return true;
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_show_nav_manu){
            mDrawerLayout.openDrawer(Gravity.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PrefUtils.PREF_ATTENDEE_AT_VENUE)){
            populateNavDrawer();
            invalidateOptionsMenu();
        }
    }

    protected void onNavDrawerSlide(float offset) {

    }

    private void populateNavDrawer() {
        mNavDrawerItems.add(NAVDRAWER_ITEM_SHOW_ZQ);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SHOW_CONTACTS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SHOW_MAP);
        mNavDrawerItems.add(NAVDRAWER_ITEM_DOWNLOAD_DATA);
        mNavDrawerItems.add(NAVDRAWER_ITEM_ABOUT);

        createNavDrawerItems();
    }

    private void goToNavDrawerItem(int item){
        Intent intent;
        switch(item){
            case NAVDRAWER_ITEM_SHOW_ZQ:
                intent = new Intent(this,ZaiqingActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_SHOW_CONTACTS:
                intent = new Intent(this,ContactsActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_SHOW_MAP:
                intent = new Intent(this,MapActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_DOWNLOAD_DATA:
                intent = new Intent(this,DownloadDataActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_ABOUT:
                intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void createNavDrawerItems(){
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == itemId;
        int layoutToInflate = 0;
        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else if (itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            // we are done
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ?
                NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ?
                NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });
        return view;
    }

    private void onNavDrawerItemClicked(final int itemId){
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(itemId);
            }
        },NAVDRAWER_LAUNCH_DELAY);

        setSelectedNavDrawerItem(itemId);

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    private void setSelectedNavDrawerItem(int itemId) {
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }
}
