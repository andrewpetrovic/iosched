package com.itic.mobile.baseactivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class AboutActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        // add about内容
        addPreferencesFromResource(R.xml.preferences);
    }
}
