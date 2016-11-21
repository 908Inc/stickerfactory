package vc908.stickerfactory.provider.packs;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code packs} table.
 */
public class PacksContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return PacksColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable PacksSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Pack name
     */
    public PacksContentValues putName(@Nullable String value) {
        mContentValues.put(PacksColumns.NAME, value);
        return this;
    }

    public PacksContentValues putNameNull() {
        mContentValues.putNull(PacksColumns.NAME);
        return this;
    }

    /**
     * Pack order
     */
    public PacksContentValues putPackOrder(@Nullable Integer value) {
        mContentValues.put(PacksColumns.PACK_ORDER, value);
        return this;
    }

    public PacksContentValues putPackOrderNull() {
        mContentValues.putNull(PacksColumns.PACK_ORDER);
        return this;
    }

    /**
     * Pack title
     */
    public PacksContentValues putTitle(@Nullable String value) {
        mContentValues.put(PacksColumns.TITLE, value);
        return this;
    }

    public PacksContentValues putTitleNull() {
        mContentValues.putNull(PacksColumns.TITLE);
        return this;
    }

    /**
     * Pack Artist
     */
    public PacksContentValues putArtist(@Nullable String value) {
        mContentValues.put(PacksColumns.ARTIST, value);
        return this;
    }

    public PacksContentValues putArtistNull() {
        mContentValues.putNull(PacksColumns.ARTIST);
        return this;
    }

    /**
     * Pack price
     */
    public PacksContentValues putPrice(@Nullable Float value) {
        mContentValues.put(PacksColumns.PRICE, value);
        return this;
    }

    public PacksContentValues putPriceNull() {
        mContentValues.putNull(PacksColumns.PRICE);
        return this;
    }

    /**
     * Pack status
     */
    public PacksContentValues putStatus(@Nullable Status value) {
        mContentValues.put(PacksColumns.STATUS, value == null ? null : value.ordinal());
        return this;
    }

    public PacksContentValues putStatusNull() {
        mContentValues.putNull(PacksColumns.STATUS);
        return this;
    }

    /**
     * Is pack available on subscription
     */
    public PacksContentValues putSubscription(@Nullable Boolean value) {
        mContentValues.put(PacksColumns.SUBSCRIPTION, value);
        return this;
    }

    public PacksContentValues putSubscriptionNull() {
        mContentValues.putNull(PacksColumns.SUBSCRIPTION);
        return this;
    }

    /**
     * Pack lat modify date
     */
    public PacksContentValues putLastModifyDate(@Nullable Long value) {
        mContentValues.put(PacksColumns.LAST_MODIFY_DATE, value);
        return this;
    }

    public PacksContentValues putLastModifyDateNull() {
        mContentValues.putNull(PacksColumns.LAST_MODIFY_DATE);
        return this;
    }
}
