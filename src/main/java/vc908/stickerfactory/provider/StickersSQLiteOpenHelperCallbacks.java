package vc908.stickerfactory.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import vc908.stickerfactory.BuildConfig;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.stickers.StickersColumns;
import vc908.stickerfactory.utils.Logger;

/**
 * Implement your custom database creation or upgrade code here.
 * <p>
 * This file will not be overwritten if you re-run the content provider generator.
 */
public class StickersSQLiteOpenHelperCallbacks {
    private static final String TAG = StickersSQLiteOpenHelperCallbacks.class.getSimpleName();

    public void onOpen(final Context context, final SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Logger.d(TAG, "onOpen");
        // Insert your db open code here.
    }

    public void onPreCreate(final Context context, final SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Logger.d(TAG, "onPreCreate");
        // Insert your db creation code here. This is called before your tables are created.
    }

    public void onPostCreate(final Context context, final SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Logger.d(TAG, "onPostCreate");
        // Insert your db creation code here. This is called after your tables are created.
    }

    public void onUpgrade(final Context context, final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (BuildConfig.DEBUG)
            Logger.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion < 2) {
            String query = "ALTER TABLE " + PacksColumns.TABLE_NAME + " ADD COLUMN " + PacksColumns.STATUS + " INTEGER DEFAULT 0;";
            db.execSQL(query);
        }
        if (oldVersion < 4) {
            String query = "ALTER TABLE " + PacksColumns.TABLE_NAME + " ADD COLUMN " + PacksColumns.LAST_MODIFY_DATE + " INTEGER DEFAULT 0;";
            db.execSQL(query);
        }
        if (oldVersion < 5) {
            String query = "ALTER TABLE " + PacksColumns.TABLE_NAME + " ADD COLUMN " + PacksColumns.SUBSCRIPTION + " INTEGER DEFAULT 0;";
            db.execSQL(query);
        }
        if (oldVersion < 6) {
            db.execSQL(StickersSQLiteOpenHelper.SQL_CREATE_TABLE_PENDING_TASKS);
        }
        if (oldVersion < 7) {
            // Add content id column to stickers table
            String query = "ALTER TABLE " + StickersColumns.TABLE_NAME + " ADD COLUMN " + StickersColumns.CONTENT_ID + " TEXT;";
            db.execSQL(query);
            // Recreate stickers and recently stickers tables with new constraints
            db.execSQL("DROP TABLE IF EXISTS '" + RecentlyStickersColumns.TABLE_NAME + "'");
            db.execSQL(StickersSQLiteOpenHelper.SQL_CREATE_TABLE_RECENTLY_STICKERS);
            db.execSQL("DROP TABLE IF EXISTS '" + StickersColumns.TABLE_NAME + "'");
            db.execSQL(StickersSQLiteOpenHelper.SQL_CREATE_TABLE_STICKERS);
            db.execSQL("DROP TABLE IF EXISTS '" + PendingTasksColumns.TABLE_NAME + "'");
            db.execSQL(StickersSQLiteOpenHelper.SQL_CREATE_TABLE_PENDING_TASKS);
        }
        if (oldVersion < 8) {
            db.execSQL(StickersSQLiteOpenHelper.SQL_CREATE_TABLE_RECENTLY_EMOJI);
        }
    }
}
