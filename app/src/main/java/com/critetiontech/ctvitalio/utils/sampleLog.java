package com.critetiontech.ctvitalio.utils;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronDebugLog;

import android.content.Context;

public class sampleLog {
    private static final String TAG = "[APP]";
    /**
     *Log level settings<br>
     *
     * @param context
     *App contest
     * @param logLevel
     *Log level
     */
    synchronized public static void setLoggable(Context context, int logLevel) {
        OmronDebugLog.initialize(context);
        OmronDebugLog.setLoggable(logLevel);
    }
    /**
     *System log<br>
     *Output INFO message<br>
     *
     * @param msg
     *Log message(Multiple)
     */
    public static void d(Object... msg) {
        OmronDebugLog.d(TAG, msg);
    }

    /**
     *System log<br>
     *Output INFO message<br>
     *
     * @param msg
     *Log message(Multiple)
     */
    public static void i(Object... msg) {
        OmronDebugLog.i(TAG, msg);
    }

    /**
     *System log<br>
     *Output INFO message<br>
     *
     * @param msg
     *Log message(Multiple)
     */
    public static void i(String tag, Object... msg) {
        OmronDebugLog.i(TAG, tag, msg);
    }
    /**
     *System log/Debug log<br>
     *Output Warn message<br>
     *
     * @param msg
     *Log message(Multiple)
     */
    public static void w(Object... msg) {
        OmronDebugLog.w(TAG, msg);
    }

    /**
     *System log/Debug log<br>
     *Output Warn message<br>
     *
     * @param msg
     *Log message(Multiple)
     */
    public static void w(String tag, Object... msg) {
        OmronDebugLog.w(TAG, tag, msg);
    }


    /**
     *Output error message
     *
     * @param msg
     *Log message(Multiple)
     */
    public static void e(Object... msg) {
        OmronDebugLog.e(TAG, msg);
    }

    /**
     *Output error message
     *
     * @param msg
     *Log message(Multiple)
     */
    public static void e(String tag, Object... msg) {
        OmronDebugLog.e(TAG, tag, msg);
    }

    /**
     *Output error message
     *
     * @param e
     *Exceptional error
     */
    public static void e(Exception e) {
        OmronDebugLog.e(TAG, e);
    }
}
