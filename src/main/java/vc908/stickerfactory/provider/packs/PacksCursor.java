package vc908.stickerfactory.provider.packs;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code packs} table.
 */
public class PacksCursor extends AbstractCursor implements PacksModel {
    public PacksCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(PacksColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Pack name
     * Can be {@code null}.
     */
    @Nullable
    public String getName() {
        String res = getStringOrNull(PacksColumns.NAME);
        return res;
    }

    /**
     * Pack order
     * Can be {@code null}.
     */
    @Nullable
    public Integer getPackOrder() {
        Integer res = getIntegerOrNull(PacksColumns.PACK_ORDER);
        return res;
    }

    /**
     * Pack title
     * Can be {@code null}.
     */
    @Nullable
    public String getTitle() {
        String res = getStringOrNull(PacksColumns.TITLE);
        return res;
    }

    /**
     * Pack Artist
     * Can be {@code null}.
     */
    @Nullable
    public String getArtist() {
        String res = getStringOrNull(PacksColumns.ARTIST);
        return res;
    }

    /**
     * Pack price
     * Can be {@code null}.
     */
    @Nullable
    public Float getPrice() {
        Float res = getFloatOrNull(PacksColumns.PRICE);
        return res;
    }

    /**
     * Pack status
     * Can be {@code null}.
     */
    @Nullable
    public Status getStatus() {
        Integer intValue = getIntegerOrNull(PacksColumns.STATUS);
        if (intValue == null) return null;
        return Status.values()[intValue];
    }

    /**
     * Is pack available on subscription
     * Can be {@code null}.
     */
    @Nullable
    public Boolean getSubscription() {
        Boolean res = getBooleanOrNull(PacksColumns.SUBSCRIPTION);
        return res;
    }

    /**
     * Pack lat modify date
     * Can be {@code null}.
     */
    @Nullable
    public Long getLastModifyDate() {
        Long res = getLongOrNull(PacksColumns.LAST_MODIFY_DATE);
        return res;
    }
}
