package com.itic.mobile.baseactivity;

import android.app.Activity;

/**
 * Created by andrew on 2014/8/11.
 */
public class LPreviewUtils {
    private LPreviewUtils() {
    }

    public static LPreviewUtilsBase getInstance(Activity activity) {
        return new LPreviewUtilsBase(activity);
    }
}
