package org.maupu.android.tmh.util;

import android.util.Log;

import org.maupu.android.tmh.core.TmhApplication;

public abstract class TmhLogger {
    public static void d(Class cls, String message) {
        if(TmhApplication.LOGGING)
            Log.d(cls.getName(), message);
    }

    public static void i(Class cls, String message) {
        if(TmhApplication.LOGGING)
            Log.i(cls.getName(), message);
    }

    public static void w(Class cls, String message) {
        if(TmhApplication.LOGGING)
            Log.w(cls.getName(), message);
    }

    public static void e(Class cls, String message) {
        if(TmhApplication.LOGGING)
            Log.e(cls.getName(), message);
    }
}
