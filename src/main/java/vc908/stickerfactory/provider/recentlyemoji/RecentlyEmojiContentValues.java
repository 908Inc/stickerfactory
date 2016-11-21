package vc908.stickerfactory.provider.recentlyemoji;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code recently_emoji} table.
 */
public class RecentlyEmojiContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return RecentlyEmojiColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable RecentlyEmojiSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Last using time
     */
    public RecentlyEmojiContentValues putLastUsingTime(@Nullable Long value) {
        mContentValues.put(RecentlyEmojiColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyEmojiContentValues putLastUsingTimeNull() {
        mContentValues.putNull(RecentlyEmojiColumns.LAST_USING_TIME);
        return this;
    }

    /**
     * Emoji code
     */
    public RecentlyEmojiContentValues putCode(@Nullable String value) {
        mContentValues.put(RecentlyEmojiColumns.CODE, value);
        return this;
    }

    public RecentlyEmojiContentValues putCodeNull() {
        mContentValues.putNull(RecentlyEmojiColumns.CODE);
        return this;
    }
}
