package vc908.stickerfactory.provider.stickers;

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
public class StickersColumns implements BaseColumns {
    public static final String TABLE_NAME = "stickers";
    public static final Uri CONTENT_URI = Uri.parse(StickersProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Sticker's content ID
     */
    public static final String CONTENT_ID = "content_id";

    /**
     * Stickers pack name
     */
    public static final String PACK = "pack";

    /**
     * Sticker's name
     */
    public static final String NAME = "name";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            CONTENT_ID,
            PACK,
            NAME
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(CONTENT_ID) || c.contains("." + CONTENT_ID)) return true;
            if (c.equals(PACK) || c.contains("." + PACK)) return true;
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
        }
        return false;
    }

}
