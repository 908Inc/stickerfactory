package vc908.stickerfactory.utils;

import android.util.Log;

import vc908.stickerfactory.analytics.AnalyticsManager;

/**
 * Logger class is entry point of any logging operations.
 * Log messages wrap all tags with prefix {@link Logger#TAG_PREFIX}
 * Use {@link Logger#setConsoleLoggingEnabled(boolean)} to enable or disable console logging
 *
 * @author Dmitry Nezhydenko
 */
public class Logger {
    private static boolean isConsoleLogEnabled = false;
    private static boolean isNetworkLogEnabled = true;

    private static final String TAG_PREFIX = "Stickers SDK: ";

    /**
     * Disable or enable console logging
     *
     * @param isEnabled logging status
     */
    public static void setConsoleLoggingEnabled(boolean isEnabled) {
        isConsoleLogEnabled = isEnabled;
    }

    /**
     * Send an {@link Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if (isConsoleLogEnabled) {
            Log.e(TAG_PREFIX + tag, "" + msg);
        }
        if (isNetworkLogEnabled) {
            AnalyticsManager.getInstance().onError(tag, msg);
        }
    }

    /**
     * Log error by given throwable
     *
     * @param tag Tag to identify message
     * @param th  Input throwable
     */
    public static void e(String tag, Throwable th) {
        if (th != null) {
            e(tag, th.getMessage());
        }
    }

    /**
     * Send a {@link Log#ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static void e(String tag, String msg, Throwable tr) {
        if (isConsoleLogEnabled) {
            Log.e(TAG_PREFIX + tag, "" + msg, tr);
        }
        if (isNetworkLogEnabled) {
            AnalyticsManager.getInstance().onError(tag, (tr != null ? tr.getMessage() : "") + ": " + msg);
        }
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (isConsoleLogEnabled) {
            Log.d(TAG_PREFIX + tag, "" + msg);
        }

    }

    /**
     * Send an {@link Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message..
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        if (isConsoleLogEnabled) {
            Log.i(TAG_PREFIX + tag, "" + msg);
        }
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        if (isConsoleLogEnabled) {
            Log.w(TAG_PREFIX + tag, "" + msg);
        }
        if (isNetworkLogEnabled) {
            AnalyticsManager.getInstance().onWarning(tag, msg);
        }
    }
}
