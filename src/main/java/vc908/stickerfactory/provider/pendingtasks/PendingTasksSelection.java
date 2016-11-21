package vc908.stickerfactory.provider.pendingtasks;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import vc908.stickerfactory.provider.base.AbstractSelection;

/**
 * Selection for the {@code pending_tasks} table.
 */
public class PendingTasksSelection extends AbstractSelection<PendingTasksSelection> {
    @Override
    protected Uri baseUri() {
        return PendingTasksColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code PendingTasksCursor} object, which is positioned before the first entry, or null.
     */
    public PendingTasksCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new PendingTasksCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public PendingTasksCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public PendingTasksCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public PendingTasksSelection id(long... value) {
        addEquals("pending_tasks." + PendingTasksColumns._ID, toObjectArray(value));
        return this;
    }

    public PendingTasksSelection category(String... value) {
        addEquals(PendingTasksColumns.CATEGORY, value);
        return this;
    }

    public PendingTasksSelection categoryNot(String... value) {
        addNotEquals(PendingTasksColumns.CATEGORY, value);
        return this;
    }

    public PendingTasksSelection categoryLike(String... value) {
        addLike(PendingTasksColumns.CATEGORY, value);
        return this;
    }

    public PendingTasksSelection categoryContains(String... value) {
        addContains(PendingTasksColumns.CATEGORY, value);
        return this;
    }

    public PendingTasksSelection categoryStartsWith(String... value) {
        addStartsWith(PendingTasksColumns.CATEGORY, value);
        return this;
    }

    public PendingTasksSelection categoryEndsWith(String... value) {
        addEndsWith(PendingTasksColumns.CATEGORY, value);
        return this;
    }

    public PendingTasksSelection action(String... value) {
        addEquals(PendingTasksColumns.ACTION, value);
        return this;
    }

    public PendingTasksSelection actionNot(String... value) {
        addNotEquals(PendingTasksColumns.ACTION, value);
        return this;
    }

    public PendingTasksSelection actionLike(String... value) {
        addLike(PendingTasksColumns.ACTION, value);
        return this;
    }

    public PendingTasksSelection actionContains(String... value) {
        addContains(PendingTasksColumns.ACTION, value);
        return this;
    }

    public PendingTasksSelection actionStartsWith(String... value) {
        addStartsWith(PendingTasksColumns.ACTION, value);
        return this;
    }

    public PendingTasksSelection actionEndsWith(String... value) {
        addEndsWith(PendingTasksColumns.ACTION, value);
        return this;
    }

    public PendingTasksSelection value(String... value) {
        addEquals(PendingTasksColumns.VALUE, value);
        return this;
    }

    public PendingTasksSelection valueNot(String... value) {
        addNotEquals(PendingTasksColumns.VALUE, value);
        return this;
    }

    public PendingTasksSelection valueLike(String... value) {
        addLike(PendingTasksColumns.VALUE, value);
        return this;
    }

    public PendingTasksSelection valueContains(String... value) {
        addContains(PendingTasksColumns.VALUE, value);
        return this;
    }

    public PendingTasksSelection valueStartsWith(String... value) {
        addStartsWith(PendingTasksColumns.VALUE, value);
        return this;
    }

    public PendingTasksSelection valueEndsWith(String... value) {
        addEndsWith(PendingTasksColumns.VALUE, value);
        return this;
    }

    public PendingTasksSelection ispending(Boolean value) {
        addEquals(PendingTasksColumns.ISPENDING, toObjectArray(value));
        return this;
    }
}
