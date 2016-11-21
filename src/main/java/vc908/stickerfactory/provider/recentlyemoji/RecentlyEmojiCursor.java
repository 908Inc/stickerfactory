package vc908.stickerfactory.provider.recentlyemoji;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code recently_emoji} table.
 */
public class RecentlyEmojiCursor extends AbstractCursor implements RecentlyEmojiModel {
    public RecentlyEmojiCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(RecentlyEmojiColumns._ID);
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
        Long res = getLongOrNull(RecentlyEmojiColumns.LAST_USING_TIME);
        return res;
    }

    /**
     * Emoji code
     * Can be {@code null}.
     */
    @Nullable
    public String getCode() {
        String res = getStringOrNull(RecentlyEmojiColumns.CODE);
        return res;
    }
}
