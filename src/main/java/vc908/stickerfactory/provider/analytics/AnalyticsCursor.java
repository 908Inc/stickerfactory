package vc908.stickerfactory.provider.analytics;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code analytics} table.
 */
public class AnalyticsCursor extends AbstractCursor implements AnalyticsModel {
    public AnalyticsCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(AnalyticsColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Event category
     * Can be {@code null}.
     */
    @Nullable
    public String getCategory() {
        String res = getStringOrNull(AnalyticsColumns.CATEGORY);
        return res;
    }

    /**
     * Event action
     * Can be {@code null}.
     */
    @Nullable
    public String getAction() {
        String res = getStringOrNull(AnalyticsColumns.ACTION);
        return res;
    }

    /**
     * Event label
     * Can be {@code null}.
     */
    @Nullable
    public String getLabel() {
        String res = getStringOrNull(AnalyticsColumns.LABEL);
        return res;
    }

    /**
     * Event count
     * Can be {@code null}.
     */
    @Nullable
    public Integer getEventCount() {
        Integer res = getIntegerOrNull(AnalyticsColumns.EVENT_COUNT);
        return res;
    }

    /**
     * Event time
     * Can be {@code null}.
     */
    @Nullable
    public Long getEventtime() {
        Long res = getLongOrNull(AnalyticsColumns.EVENTTIME);
        return res;
    }
}
