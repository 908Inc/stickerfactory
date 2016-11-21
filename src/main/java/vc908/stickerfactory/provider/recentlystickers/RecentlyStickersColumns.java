package vc908.stickerfactory.provider.recentlystickers;

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
 * Stickers list.
 */
public class RecentlyStickersColumns implements BaseColumns {
    public static final String TABLE_NAME = "recently_stickers";
    public static final Uri CONTENT_URI = Uri.parse(StickersProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Last using time
     */
    public static final String LAST_USING_TIME = "last_using_time";

    /**
     * Sticker's content ID
     */
    public static final String CONTENT_ID = "content_id";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            LAST_USING_TIME,
            CONTENT_ID
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(LAST_USING_TIME) || c.contains("." + LAST_USING_TIME)) return true;
            if (c.equals(CONTENT_ID) || c.contains("." + CONTENT_ID)) return true;
        }
        return false;
    }

}
