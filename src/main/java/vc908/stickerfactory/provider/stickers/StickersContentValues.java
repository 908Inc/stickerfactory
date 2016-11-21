package vc908.stickerfactory.provider.stickers;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code stickers} table.
 */
public class StickersContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return StickersColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable StickersSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Sticker's content ID
     */
    public StickersContentValues putContentId(@Nullable String value) {
        mContentValues.put(StickersColumns.CONTENT_ID, value);
        return this;
    }

    public StickersContentValues putContentIdNull() {
        mContentValues.putNull(StickersColumns.CONTENT_ID);
        return this;
    }

    /**
     * Stickers pack name
     */
    public StickersContentValues putPack(@Nullable String value) {
        mContentValues.put(StickersColumns.PACK, value);
        return this;
    }

    public StickersContentValues putPackNull() {
        mContentValues.putNull(StickersColumns.PACK);
        return this;
    }

    /**
     * Sticker's name
     */
    public StickersContentValues putName(@Nullable String value) {
        mContentValues.put(StickersColumns.NAME, value);
        return this;
    }

    public StickersContentValues putNameNull() {
        mContentValues.putNull(StickersColumns.NAME);
        return this;
    }
}
