package vc908.stickerfactory.provider.recentlyemoji;

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
 * Recently used emoji list.
 */
public class RecentlyEmojiColumns implements BaseColumns {
    public static final String TABLE_NAME = "recently_emoji";
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
     * Emoji code
     */
    public static final String CODE = "code";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            LAST_USING_TIME,
            CODE
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(LAST_USING_TIME) || c.contains("." + LAST_USING_TIME)) return true;
            if (c.equals(CODE) || c.contains("." + CODE)) return true;
        }
        return false;
    }

}
