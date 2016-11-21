package vc908.stickerfactory.provider.pendingtasks;

import android.net.Uri;
import android.provider.BaseColumns;

import vc908.stickerfactory.provider.StickersProvider;
import vc908.stickerfactory.provider.analytics.AnalyticsColumns;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksColumns;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.stickers.StickersColumns;

/**
 * Pending tasks
 */
public class PendingTasksColumns implements BaseColumns {
    public static final String TABLE_NAME = "pending_tasks";
    public static final Uri CONTENT_URI = Uri.parse(StickersProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Task category
     */
    public static final String CATEGORY = "category";

    /**
     * Task action
     */
    public static final String ACTION = "action";

    /**
     * Task value
     */
    public static final String VALUE = "value";

    /**
     * Is task waiting for execution
     */
    public static final String ISPENDING = "isPending";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            CATEGORY,
            ACTION,
            VALUE,
            ISPENDING
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(CATEGORY) || c.contains("." + CATEGORY)) return true;
            if (c.equals(ACTION) || c.contains("." + ACTION)) return true;
            if (c.equals(VALUE) || c.contains("." + VALUE)) return true;
            if (c.equals(ISPENDING) || c.contains("." + ISPENDING)) return true;
        }
        return false;
    }

}
