package vc908.stickerfactory.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import vc908.stickerfactory.BuildConfig;
import vc908.stickerfactory.provider.analytics.AnalyticsColumns;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksColumns;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.stickers.StickersColumns;

public class StickersSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = StickersSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "stickers.db";
    private static final int DATABASE_VERSION = 8;
    private static StickersSQLiteOpenHelper sInstance;
    private final Context mContext;
    private final StickersSQLiteOpenHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_ANALYTICS = "CREATE TABLE IF NOT EXISTS "
            + AnalyticsColumns.TABLE_NAME + " ( "
            + AnalyticsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AnalyticsColumns.CATEGORY + " TEXT, "
            + AnalyticsColumns.ACTION + " TEXT, "
            + AnalyticsColumns.LABEL + " TEXT, "
            + AnalyticsColumns.EVENT_COUNT + " INTEGER DEFAULT 0, "
            + AnalyticsColumns.EVENTTIME + " INTEGER "
            + " );";

    public static final String SQL_CREATE_TABLE_PACKS = "CREATE TABLE IF NOT EXISTS "
            + PacksColumns.TABLE_NAME + " ( "
            + PacksColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PacksColumns.NAME + " TEXT, "
            + PacksColumns.PACK_ORDER + " INTEGER DEFAULT 0, "
            + PacksColumns.TITLE + " TEXT, "
            + PacksColumns.ARTIST + " TEXT, "
            + PacksColumns.PRICE + " REAL, "
            + PacksColumns.STATUS + " INTEGER DEFAULT 0, "
            + PacksColumns.SUBSCRIPTION + " INTEGER DEFAULT 0, "
            + PacksColumns.LAST_MODIFY_DATE + " INTEGER DEFAULT 0 "
            + ", CONSTRAINT unique_name UNIQUE (name) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_PENDING_TASKS = "CREATE TABLE IF NOT EXISTS "
            + PendingTasksColumns.TABLE_NAME + " ( "
            + PendingTasksColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PendingTasksColumns.CATEGORY + " TEXT, "
            + PendingTasksColumns.ACTION + " TEXT, "
            + PendingTasksColumns.VALUE + " TEXT, "
            + PendingTasksColumns.ISPENDING + " INTEGER DEFAULT 1 "
            + ", CONSTRAINT unique_action UNIQUE (category, action, value) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_RECENTLY_EMOJI = "CREATE TABLE IF NOT EXISTS "
            + RecentlyEmojiColumns.TABLE_NAME + " ( "
            + RecentlyEmojiColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RecentlyEmojiColumns.LAST_USING_TIME + " INTEGER DEFAULT 0, "
            + RecentlyEmojiColumns.CODE + " TEXT "
            + ", CONSTRAINT unique_name UNIQUE (code) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_RECENTLY_STICKERS = "CREATE TABLE IF NOT EXISTS "
            + RecentlyStickersColumns.TABLE_NAME + " ( "
            + RecentlyStickersColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RecentlyStickersColumns.LAST_USING_TIME + " INTEGER DEFAULT 0, "
            + RecentlyStickersColumns.CONTENT_ID + " TEXT "
            + ", CONSTRAINT unique_name UNIQUE (content_id) ON CONFLICT REPLACE"
            + " );";

    public static final String SQL_CREATE_TABLE_STICKERS = "CREATE TABLE IF NOT EXISTS "
            + StickersColumns.TABLE_NAME + " ( "
            + StickersColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + StickersColumns.CONTENT_ID + " TEXT, "
            + StickersColumns.PACK + " TEXT, "
            + StickersColumns.NAME + " TEXT "
            + ", CONSTRAINT unique_name UNIQUE (content_id) ON CONFLICT REPLACE"
            + " );";

    // @formatter:on

    public static StickersSQLiteOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static StickersSQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static StickersSQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new StickersSQLiteOpenHelper(context);
    }

    private StickersSQLiteOpenHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new StickersSQLiteOpenHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static StickersSQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new StickersSQLiteOpenHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private StickersSQLiteOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new StickersSQLiteOpenHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_ANALYTICS);
        db.execSQL(SQL_CREATE_TABLE_PACKS);
        db.execSQL(SQL_CREATE_TABLE_PENDING_TASKS);
        db.execSQL(SQL_CREATE_TABLE_RECENTLY_EMOJI);
        db.execSQL(SQL_CREATE_TABLE_RECENTLY_STICKERS);
        db.execSQL(SQL_CREATE_TABLE_STICKERS);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
