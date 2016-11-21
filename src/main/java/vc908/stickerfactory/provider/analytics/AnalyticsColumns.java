package vc908.stickerfactory.provider.analytics;

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
 * Analytics items list.
 */
public class AnalyticsColumns implements BaseColumns {
    public static final String TABLE_NAME = "analytics";
    public static final Uri CONTENT_URI = Uri.parse(StickersProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Event category
     */
    public static final String CATEGORY = "category";

    /**
     * Event action
     */
    public static final String ACTION = "action";

    /**
     * Event label
     */
    public static final String LABEL = "label";

    /**
     * Event count
     */
    public static final String EVENT_COUNT = "event_count";

    /**
     * Event time
     */
    public static final String EVENTTIME = "eventTime";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            CATEGORY,
            ACTION,
            LABEL,
            EVENT_COUNT,
            EVENTTIME
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(CATEGORY) || c.contains("." + CATEGORY)) return true;
            if (c.equals(ACTION) || c.contains("." + ACTION)) return true;
            if (c.equals(LABEL) || c.contains("." + LABEL)) return true;
            if (c.equals(EVENT_COUNT) || c.contains("." + EVENT_COUNT)) return true;
            if (c.equals(EVENTTIME) || c.contains("." + EVENTTIME)) return true;
        }
        return false;
    }

}
