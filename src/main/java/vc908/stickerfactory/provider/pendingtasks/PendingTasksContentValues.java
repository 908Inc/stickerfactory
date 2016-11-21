package vc908.stickerfactory.provider.pendingtasks;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code pending_tasks} table.
 */
public class PendingTasksContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return PendingTasksColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable PendingTasksSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Task category
     */
    public PendingTasksContentValues putCategory(@Nullable String value) {
        mContentValues.put(PendingTasksColumns.CATEGORY, value);
        return this;
    }

    public PendingTasksContentValues putCategoryNull() {
        mContentValues.putNull(PendingTasksColumns.CATEGORY);
        return this;
    }

    /**
     * Task action
     */
    public PendingTasksContentValues putAction(@Nullable String value) {
        mContentValues.put(PendingTasksColumns.ACTION, value);
        return this;
    }

    public PendingTasksContentValues putActionNull() {
        mContentValues.putNull(PendingTasksColumns.ACTION);
        return this;
    }

    /**
     * Task value
     */
    public PendingTasksContentValues putValue(@Nullable String value) {
        mContentValues.put(PendingTasksColumns.VALUE, value);
        return this;
    }

    public PendingTasksContentValues putValueNull() {
        mContentValues.putNull(PendingTasksColumns.VALUE);
        return this;
    }

    /**
     * Is task waiting for execution
     */
    public PendingTasksContentValues putIspending(@Nullable Boolean value) {
        mContentValues.put(PendingTasksColumns.ISPENDING, value);
        return this;
    }

    public PendingTasksContentValues putIspendingNull() {
        mContentValues.putNull(PendingTasksColumns.ISPENDING);
        return this;
    }
}
