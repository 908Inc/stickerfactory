package vc908.stickerfactory.provider.pendingtasks;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vc908.stickerfactory.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code pending_tasks} table.
 */
public class PendingTasksCursor extends AbstractCursor implements PendingTasksModel {
    public PendingTasksCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(PendingTasksColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Task category
     * Can be {@code null}.
     */
    @Nullable
    public String getCategory() {
        String res = getStringOrNull(PendingTasksColumns.CATEGORY);
        return res;
    }

    /**
     * Task action
     * Can be {@code null}.
     */
    @Nullable
    public String getAction() {
        String res = getStringOrNull(PendingTasksColumns.ACTION);
        return res;
    }

    /**
     * Task value
     * Can be {@code null}.
     */
    @Nullable
    public String getValue() {
        String res = getStringOrNull(PendingTasksColumns.VALUE);
        return res;
    }

    /**
     * Is task waiting for execution
     * Can be {@code null}.
     */
    @Nullable
    public Boolean getIspending() {
        Boolean res = getBooleanOrNull(PendingTasksColumns.ISPENDING);
        return res;
    }
}
