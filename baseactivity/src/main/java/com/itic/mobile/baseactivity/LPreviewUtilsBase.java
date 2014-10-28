package com.itic.mobile.baseactivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Created by andrew on 2014/8/8.
 */
public class LPreviewUtilsBase {

    protected Activity mActivity;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBarDrawerToggleWrapper mDrawerToggleWrapper;

    LPreviewUtilsBase(Activity activity) {
        mActivity = activity;
    }

    public ActionBarDrawerToggleWrapper setupDrawerToggle(DrawerLayout drawerLayout, final DrawerLayout.DrawerListener drawerListener) {
        /**
         * 初始化ActionBarDrawerToggle对象，并实现菜单打开、关闭、状态改变、菜单滑动方法
         */
        mDrawerToggle = new ActionBarDrawerToggle(mActivity,
                drawerLayout, R.drawable.ic_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerListener.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerListener.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                drawerListener.onDrawerStateChanged(newState);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                drawerListener.onDrawerSlide(drawerView,slideOffset);
            }
        };

        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggleWrapper = new ActionBarDrawerToggleWrapper();
        return mDrawerToggleWrapper;
    }

    public void trySetActionBar() {
        // Do nothing pre-L
    }

    public boolean hasLPreviewAPIs() {
        return false;
    }

    public boolean shouldChangeActionBarForDrawer() {
        return true;
    }

    public void showHideActionBarIfPartOfDecor(boolean show) {
        // Android L ,action bar 一般是window装饰的一部分
        if (show) {
            mActivity.getActionBar().show();
        } else {
            mActivity.getActionBar().hide();
        }
    }

    public void setMediumTypeface(TextView textView) {
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    }

    /**
     * ActionBarDrawerToggleWrapper类，封装ActionBarDrawerToggle的行为
     */
    public class ActionBarDrawerToggleWrapper {
        public void syncState() {
            if (mDrawerToggle != null) {
                //同步Drawer状态
                mDrawerToggle.syncState();
            }
        }

        public void onConfigurationChanged(Configuration newConfig) {
            if (mDrawerToggle != null) {
                mDrawerToggle.onConfigurationChanged(newConfig);
            }
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            if (mDrawerToggle != null) {
                return mDrawerToggle.onOptionsItemSelected(item);
            }
            return false;
        }
    }

    public void startActivityWithTransition(Intent intent, View clickedView,
                                            String sharedElementName) {
        mActivity.startActivity(intent);
    }

    public void setViewName(View v, String viewName) {
        // Can't do this pre-L
    }

}
