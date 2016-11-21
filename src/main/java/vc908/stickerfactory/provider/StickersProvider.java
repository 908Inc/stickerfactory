package vc908.stickerfactory.provider;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;

import vc908.stickerfactory.BuildConfig;
import vc908.stickerfactory.provider.analytics.AnalyticsColumns;
import vc908.stickerfactory.provider.base.BaseContentProvider;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksColumns;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.stickers.StickersColumns;

public class StickersProvider extends BaseContentProvider {
    private static final String TAG = StickersProvider.class.getSimpleName();

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TYPE_CURSOR_ITEM = "vnd.android.cursor.item/";
    private static final String TYPE_CURSOR_DIR = "vnd.android.cursor.dir/";

    private static final int URI_TYPE_ANALYTICS = 0;
    private static final int URI_TYPE_ANALYTICS_ID = 1;

    private static final int URI_TYPE_PACKS = 2;
    private static final int URI_TYPE_PACKS_ID = 3;

    private static final int URI_TYPE_PENDING_TASKS = 4;
    private static final int URI_TYPE_PENDING_TASKS_ID = 5;

    private static final int URI_TYPE_RECENTLY_EMOJI = 6;
    private static final int URI_TYPE_RECENTLY_EMOJI_ID = 7;

    private static final int URI_TYPE_RECENTLY_STICKERS = 8;
    private static final int URI_TYPE_RECENTLY_STICKERS_ID = 9;

    private static final int URI_TYPE_STICKERS = 10;
    private static final int URI_TYPE_STICKERS_ID = 11;



    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    public static String AUTHORITY;
    public static String CONTENT_URI_BASE;

    public static void initAuthority(String packageName) {
        AUTHORITY = packageName + ".stickersProvider";
        CONTENT_URI_BASE = "content://" + AUTHORITY;

        URI_MATCHER.addURI(AUTHORITY, AnalyticsColumns.TABLE_NAME, URI_TYPE_ANALYTICS);
        URI_MATCHER.addURI(AUTHORITY, AnalyticsColumns.TABLE_NAME + "/#", URI_TYPE_ANALYTICS_ID);
        URI_MATCHER.addURI(AUTHORITY, PacksColumns.TABLE_NAME, URI_TYPE_PACKS);
        URI_MATCHER.addURI(AUTHORITY, PacksColumns.TABLE_NAME + "/#", URI_TYPE_PACKS_ID);
        URI_MATCHER.addURI(AUTHORITY, PendingTasksColumns.TABLE_NAME, URI_TYPE_PENDING_TASKS);
        URI_MATCHER.addURI(AUTHORITY, PendingTasksColumns.TABLE_NAME + "/#", URI_TYPE_PENDING_TASKS_ID);
        URI_MATCHER.addURI(AUTHORITY, RecentlyEmojiColumns.TABLE_NAME, URI_TYPE_RECENTLY_EMOJI);
        URI_MATCHER.addURI(AUTHORITY, RecentlyEmojiColumns.TABLE_NAME + "/#", URI_TYPE_RECENTLY_EMOJI_ID);
        URI_MATCHER.addURI(AUTHORITY, RecentlyStickersColumns.TABLE_NAME, URI_TYPE_RECENTLY_STICKERS);
        URI_MATCHER.addURI(AUTHORITY, RecentlyStickersColumns.TABLE_NAME + "/#", URI_TYPE_RECENTLY_STICKERS_ID);
        URI_MATCHER.addURI(AUTHORITY, StickersColumns.TABLE_NAME, URI_TYPE_STICKERS);
        URI_MATCHER.addURI(AUTHORITY, StickersColumns.TABLE_NAME + "/#", URI_TYPE_STICKERS_ID);
    }

    @Override
    protected SQLiteOpenHelper createSqLiteOpenHelper() {
        return StickersSQLiteOpenHelper.getInstance(getContext());
    }

    @Override
    protected boolean hasDebug() {
        return DEBUG;
    }

    @Override
    public String getType(Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_ANALYTICS:
                return TYPE_CURSOR_DIR + AnalyticsColumns.TABLE_NAME;
            case URI_TYPE_ANALYTICS_ID:
                return TYPE_CURSOR_ITEM + AnalyticsColumns.TABLE_NAME;

            case URI_TYPE_PACKS:
                return TYPE_CURSOR_DIR + PacksColumns.TABLE_NAME;
            case URI_TYPE_PACKS_ID:
                return TYPE_CURSOR_ITEM + PacksColumns.TABLE_NAME;

            case URI_TYPE_PENDING_TASKS:
                return TYPE_CURSOR_DIR + PendingTasksColumns.TABLE_NAME;
            case URI_TYPE_PENDING_TASKS_ID:
                return TYPE_CURSOR_ITEM + PendingTasksColumns.TABLE_NAME;

            case URI_TYPE_RECENTLY_EMOJI:
                return TYPE_CURSOR_DIR + RecentlyEmojiColumns.TABLE_NAME;
            case URI_TYPE_RECENTLY_EMOJI_ID:
                return TYPE_CURSOR_ITEM + RecentlyEmojiColumns.TABLE_NAME;

            case URI_TYPE_RECENTLY_STICKERS:
                return TYPE_CURSOR_DIR + RecentlyStickersColumns.TABLE_NAME;
            case URI_TYPE_RECENTLY_STICKERS_ID:
                return TYPE_CURSOR_ITEM + RecentlyStickersColumns.TABLE_NAME;

            case URI_TYPE_STICKERS:
                return TYPE_CURSOR_DIR + StickersColumns.TABLE_NAME;
            case URI_TYPE_STICKERS_ID:
                return TYPE_CURSOR_ITEM + StickersColumns.TABLE_NAME;

        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (DEBUG) Log.d(TAG, "insert uri=" + uri + " values=" + values);
        return super.insert(uri, values);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (DEBUG) Log.d(TAG, "bulkInsert uri=" + uri + " values.length=" + values.length);
        return super.bulkInsert(uri, values);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (DEBUG) Log.d(TAG, "update uri=" + uri + " values=" + values + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs));
        return super.update(uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (DEBUG) Log.d(TAG, "delete uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs));
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG)
            Log.d(TAG, "query uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString(selectionArgs) + " sortOrder=" + sortOrder
                    + " groupBy=" + uri.getQueryParameter(QUERY_GROUP_BY) + " having=" + uri.getQueryParameter(QUERY_HAVING) + " limit=" + uri.getQueryParameter(QUERY_LIMIT));
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected QueryParams getQueryParams(Uri uri, String selection, String[] projection) {
        QueryParams res = new QueryParams();
        String id = null;
        int matchedId = URI_MATCHER.match(uri);
        switch (matchedId) {
            case URI_TYPE_ANALYTICS:
            case URI_TYPE_ANALYTICS_ID:
                res.table = AnalyticsColumns.TABLE_NAME;
                res.idColumn = AnalyticsColumns._ID;
                res.tablesWithJoins = AnalyticsColumns.TABLE_NAME;
                res.orderBy = AnalyticsColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_PACKS:
            case URI_TYPE_PACKS_ID:
                res.table = PacksColumns.TABLE_NAME;
                res.idColumn = PacksColumns._ID;
                res.tablesWithJoins = PacksColumns.TABLE_NAME;
                res.orderBy = PacksColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_PENDING_TASKS:
            case URI_TYPE_PENDING_TASKS_ID:
                res.table = PendingTasksColumns.TABLE_NAME;
                res.idColumn = PendingTasksColumns._ID;
                res.tablesWithJoins = PendingTasksColumns.TABLE_NAME;
                res.orderBy = PendingTasksColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_RECENTLY_EMOJI:
            case URI_TYPE_RECENTLY_EMOJI_ID:
                res.table = RecentlyEmojiColumns.TABLE_NAME;
                res.idColumn = RecentlyEmojiColumns._ID;
                res.tablesWithJoins = RecentlyEmojiColumns.TABLE_NAME;
                res.orderBy = RecentlyEmojiColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_RECENTLY_STICKERS:
            case URI_TYPE_RECENTLY_STICKERS_ID:
                res.table = RecentlyStickersColumns.TABLE_NAME;
                res.idColumn = RecentlyStickersColumns._ID;
                res.tablesWithJoins = RecentlyStickersColumns.TABLE_NAME;
                res.orderBy = RecentlyStickersColumns.DEFAULT_ORDER;
                break;

            case URI_TYPE_STICKERS:
            case URI_TYPE_STICKERS_ID:
                res.table = StickersColumns.TABLE_NAME;
                res.idColumn = StickersColumns._ID;
                res.tablesWithJoins = StickersColumns.TABLE_NAME;
                res.orderBy = StickersColumns.DEFAULT_ORDER;
                break;

            default:
                throw new IllegalArgumentException("The uri '" + uri + "' is not supported by this ContentProvider");
        }

        switch (matchedId) {
            case URI_TYPE_ANALYTICS_ID:
            case URI_TYPE_PACKS_ID:
            case URI_TYPE_PENDING_TASKS_ID:
            case URI_TYPE_RECENTLY_EMOJI_ID:
            case URI_TYPE_RECENTLY_STICKERS_ID:
            case URI_TYPE_STICKERS_ID:
                id = uri.getLastPathSegment();
        }
        if (id != null) {
            if (selection != null) {
                res.selection = res.table + "." + res.idColumn + "=" + id + " and (" + selection + ")";
            } else {
                res.selection = res.table + "." + res.idColumn + "=" + id;
            }
        } else {
            res.selection = selection;
        }
        return res;
    }
}
