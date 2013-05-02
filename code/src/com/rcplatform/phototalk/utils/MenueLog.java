package com.rcplatform.phototalk.utils;

import android.util.Log;

public class MenueLog {

    private static String TAG = "MenueLog";

    private static final boolean DEBUG = true;

    public static void log(String tag, String log) {
        if (DEBUG) {
            if (tag != null)
                Log.i(tag, "" + log);
            else
                Log.i(TAG, "" + log);
        }
    }
}
