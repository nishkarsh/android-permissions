package com.intentfilter.androidpermissions;

import android.util.Log;

public class Logger {
    private String LOG_TAG;

    private Logger(Class clazz) {
        this.LOG_TAG = clazz.getSimpleName();
    }

    public static Logger loggerFor(Class clazz) {
        return new Logger(clazz);
    }

    public void d(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, message);
        }
    }

    public void i(String message) {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, message);
        }
    }

    public void e(String message) {
        Log.e(LOG_TAG, message);
    }

    public void e(String message, Throwable throwable) {
        Log.e(LOG_TAG, message, throwable);
    }
}