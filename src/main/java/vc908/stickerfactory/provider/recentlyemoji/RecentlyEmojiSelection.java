package vc908.stickerfactory.provider.recentlyemoji;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import vc908.stickerfactory.provider.base.AbstractSelection;

/**
 * Selection for the {@code recently_emoji} table.
 */
public class RecentlyEmojiSelection extends AbstractSelection<RecentlyEmojiSelection> {
    @Override
    protected Uri baseUri() {
        return RecentlyEmojiColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code RecentlyEmojiCursor} object, which is positioned before the first entry, or null.
     */
    public RecentlyEmojiCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new RecentlyEmojiCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public RecentlyEmojiCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public RecentlyEmojiCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public RecentlyEmojiSelection id(long... value) {
        addEquals("recently_emoji." + RecentlyEmojiColumns._ID, toObjectArray(value));
        return this;
    }

    public RecentlyEmojiSelection lastUsingTime(Long... value) {
        addEquals(RecentlyEmojiColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyEmojiSelection lastUsingTimeNot(Long... value) {
        addNotEquals(RecentlyEmojiColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyEmojiSelection lastUsingTimeGt(long value) {
        addGreaterThan(RecentlyEmojiColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyEmojiSelection lastUsingTimeGtEq(long value) {
        addGreaterThanOrEquals(RecentlyEmojiColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyEmojiSelection lastUsingTimeLt(long value) {
        addLessThan(RecentlyEmojiColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyEmojiSelection lastUsingTimeLtEq(long value) {
        addLessThanOrEquals(RecentlyEmojiColumns.LAST_USING_TIME, value);
        return this;
    }

    public RecentlyEmojiSelection code(String... value) {
        addEquals(RecentlyEmojiColumns.CODE, value);
        return this;
    }

    public RecentlyEmojiSelection codeNot(String... value) {
        addNotEquals(RecentlyEmojiColumns.CODE, value);
        return this;
    }

    public RecentlyEmojiSelection codeLike(String... value) {
        addLike(RecentlyEmojiColumns.CODE, value);
        return this;
    }

    public RecentlyEmojiSelection codeContains(String... value) {
        addContains(RecentlyEmojiColumns.CODE, value);
        return this;
    }

    public RecentlyEmojiSelection codeStartsWith(String... value) {
        addStartsWith(RecentlyEmojiColumns.CODE, value);
        return this;
    }

    public RecentlyEmojiSelection codeEndsWith(String... value) {
        addEndsWith(RecentlyEmojiColumns.CODE, value);
        return this;
    }
}
