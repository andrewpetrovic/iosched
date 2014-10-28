package com.itic.mobile.baseactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by andrew on 2014/8/10.
 */
public class PrefUtils {
    public static final String PREF_ATTENDEE_AT_VENUE = "pref_attendee_at_venue";

    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }
}
