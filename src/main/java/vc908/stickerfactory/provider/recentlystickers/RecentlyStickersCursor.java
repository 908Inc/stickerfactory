package vc908.stickerfactory.provider.recentlystickers;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code recently_stickers} table.
 */
public class RecentlyStickersCursor extends AbstractCursor implements RecentlyStickersModel {
    public RecentlyStickersCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(RecentlyStickersColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Last using time
     * Can be {@code null}.
     */
    @Nullable
    public Long getLastUsingTime() {
        Long res = getLongOrNull(RecentlyStickersColumns.LAST_USING_TIME);
        return res;
    }

    /**
     * Sticker's content ID
     * Can be {@code null}.
     */
    @Nullable
    public String getContentId() {
        String res = getStringOrNull(RecentlyStickersColumns.CONTENT_ID);
        return res;
    }
}
