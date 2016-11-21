package vc908.stickerfactory.provider.analytics;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code analytics} table.
 */
public class AnalyticsContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return AnalyticsColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable AnalyticsSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Event category
     */
    public AnalyticsContentValues putCategory(@Nullable String value) {
        mContentValues.put(AnalyticsColumns.CATEGORY, value);
        return this;
    }

    public AnalyticsContentValues putCategoryNull() {
        mContentValues.putNull(AnalyticsColumns.CATEGORY);
        return this;
    }

    /**
     * Event action
     */
    public AnalyticsContentValues putAction(@Nullable String value) {
        mContentValues.put(AnalyticsColumns.ACTION, value);
        return this;
    }

    public AnalyticsContentValues putActionNull() {
        mContentValues.putNull(AnalyticsColumns.ACTION);
        return this;
    }

    /**
     * Event label
     */
    public AnalyticsContentValues putLabel(@Nullable String value) {
        mContentValues.put(AnalyticsColumns.LABEL, value);
        return this;
    }

    public AnalyticsContentValues putLabelNull() {
        mContentValues.putNull(AnalyticsColumns.LABEL);
        return this;
    }

    /**
     * Event count
     */
    public AnalyticsContentValues putEventCount(@Nullable Integer value) {
        mContentValues.put(AnalyticsColumns.EVENT_COUNT, value);
        return this;
    }

    public AnalyticsContentValues putEventCountNull() {
        mContentValues.putNull(AnalyticsColumns.EVENT_COUNT);
        return this;
    }

    /**
     * Event time
     */
    public AnalyticsContentValues putEventtime(@Nullable Long value) {
        mContentValues.put(AnalyticsColumns.EVENTTIME, value);
        return this;
    }

    public AnalyticsContentValues putEventtimeNull() {
        mContentValues.putNull(AnalyticsColumns.EVENTTIME);
        return this;
    }
}
