package vc908.stickerfactory.provider.analytics;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import vc908.stickerfactory.provider.base.AbstractSelection;

/**
 * Selection for the {@code analytics} table.
 */
public class AnalyticsSelection extends AbstractSelection<AnalyticsSelection> {
    @Override
    protected Uri baseUri() {
        return AnalyticsColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code AnalyticsCursor} object, which is positioned before the first entry, or null.
     */
    public AnalyticsCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new AnalyticsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public AnalyticsCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public AnalyticsCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public AnalyticsSelection id(long... value) {
        addEquals("analytics." + AnalyticsColumns._ID, toObjectArray(value));
        return this;
    }

    public AnalyticsSelection category(String... value) {
        addEquals(AnalyticsColumns.CATEGORY, value);
        return this;
    }

    public AnalyticsSelection categoryNot(String... value) {
        addNotEquals(AnalyticsColumns.CATEGORY, value);
        return this;
    }

    public AnalyticsSelection categoryLike(String... value) {
        addLike(AnalyticsColumns.CATEGORY, value);
        return this;
    }

    public AnalyticsSelection categoryContains(String... value) {
        addContains(AnalyticsColumns.CATEGORY, value);
        return this;
    }

    public AnalyticsSelection categoryStartsWith(String... value) {
        addStartsWith(AnalyticsColumns.CATEGORY, value);
        return this;
    }

    public AnalyticsSelection categoryEndsWith(String... value) {
        addEndsWith(AnalyticsColumns.CATEGORY, value);
        return this;
    }

    public AnalyticsSelection action(String... value) {
        addEquals(AnalyticsColumns.ACTION, value);
        return this;
    }

    public AnalyticsSelection actionNot(String... value) {
        addNotEquals(AnalyticsColumns.ACTION, value);
        return this;
    }

    public AnalyticsSelection actionLike(String... value) {
        addLike(AnalyticsColumns.ACTION, value);
        return this;
    }

    public AnalyticsSelection actionContains(String... value) {
        addContains(AnalyticsColumns.ACTION, value);
        return this;
    }

    public AnalyticsSelection actionStartsWith(String... value) {
        addStartsWith(AnalyticsColumns.ACTION, value);
        return this;
    }

    public AnalyticsSelection actionEndsWith(String... value) {
        addEndsWith(AnalyticsColumns.ACTION, value);
        return this;
    }

    public AnalyticsSelection label(String... value) {
        addEquals(AnalyticsColumns.LABEL, value);
        return this;
    }

    public AnalyticsSelection labelNot(String... value) {
        addNotEquals(AnalyticsColumns.LABEL, value);
        return this;
    }

    public AnalyticsSelection labelLike(String... value) {
        addLike(AnalyticsColumns.LABEL, value);
        return this;
    }

    public AnalyticsSelection labelContains(String... value) {
        addContains(AnalyticsColumns.LABEL, value);
        return this;
    }

    public AnalyticsSelection labelStartsWith(String... value) {
        addStartsWith(AnalyticsColumns.LABEL, value);
        return this;
    }

    public AnalyticsSelection labelEndsWith(String... value) {
        addEndsWith(AnalyticsColumns.LABEL, value);
        return this;
    }

    public AnalyticsSelection eventCount(Integer... value) {
        addEquals(AnalyticsColumns.EVENT_COUNT, value);
        return this;
    }

    public AnalyticsSelection eventCountNot(Integer... value) {
        addNotEquals(AnalyticsColumns.EVENT_COUNT, value);
        return this;
    }

    public AnalyticsSelection eventCountGt(int value) {
        addGreaterThan(AnalyticsColumns.EVENT_COUNT, value);
        return this;
    }

    public AnalyticsSelection eventCountGtEq(int value) {
        addGreaterThanOrEquals(AnalyticsColumns.EVENT_COUNT, value);
        return this;
    }

    public AnalyticsSelection eventCountLt(int value) {
        addLessThan(AnalyticsColumns.EVENT_COUNT, value);
        return this;
    }

    public AnalyticsSelection eventCountLtEq(int value) {
        addLessThanOrEquals(AnalyticsColumns.EVENT_COUNT, value);
        return this;
    }

    public AnalyticsSelection eventtime(Long... value) {
        addEquals(AnalyticsColumns.EVENTTIME, value);
        return this;
    }

    public AnalyticsSelection eventtimeNot(Long... value) {
        addNotEquals(AnalyticsColumns.EVENTTIME, value);
        return this;
    }

    public AnalyticsSelection eventtimeGt(long value) {
        addGreaterThan(AnalyticsColumns.EVENTTIME, value);
        return this;
    }

    public AnalyticsSelection eventtimeGtEq(long value) {
        addGreaterThanOrEquals(AnalyticsColumns.EVENTTIME, value);
        return this;
    }

    public AnalyticsSelection eventtimeLt(long value) {
        addLessThan(AnalyticsColumns.EVENTTIME, value);
        return this;
    }

    public AnalyticsSelection eventtimeLtEq(long value) {
        addLessThanOrEquals(AnalyticsColumns.EVENTTIME, value);
        return this;
    }
}
