package vc908.stickerfactory.provider.stickers;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code stickers} table.
 */
public class StickersCursor extends AbstractCursor implements StickersModel {
    public StickersCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(StickersColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Sticker's content ID
     * Can be {@code null}.
     */
    @Nullable
    public String getContentId() {
        String res = getStringOrNull(StickersColumns.CONTENT_ID);
        return res;
    }

    /**
     * Stickers pack name
     * Can be {@code null}.
     */
    @Nullable
    public String getPack() {
        String res = getStringOrNull(StickersColumns.PACK);
        return res;
    }

    /**
     * Sticker's name
     * Can be {@code null}.
     */
    @Nullable
    public String getName() {
        String res = getStringOrNull(StickersColumns.NAME);
        return res;
    }
}
