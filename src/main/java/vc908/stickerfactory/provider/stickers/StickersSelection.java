package vc908.stickerfactory.provider.stickers;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import vc908.stickerfactory.provider.base.AbstractSelection;

/**
 * Selection for the {@code stickers} table.
 */
public class StickersSelection extends AbstractSelection<StickersSelection> {
    @Override
    protected Uri baseUri() {
        return StickersColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code StickersCursor} object, which is positioned before the first entry, or null.
     */
    public StickersCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new StickersCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public StickersCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public StickersCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public StickersSelection id(long... value) {
        addEquals("stickers." + StickersColumns._ID, toObjectArray(value));
        return this;
    }

    public StickersSelection contentId(String... value) {
        addEquals(StickersColumns.CONTENT_ID, value);
        return this;
    }

    public StickersSelection contentIdNot(String... value) {
        addNotEquals(StickersColumns.CONTENT_ID, value);
        return this;
    }

    public StickersSelection contentIdLike(String... value) {
        addLike(StickersColumns.CONTENT_ID, value);
        return this;
    }

    public StickersSelection contentIdContains(String... value) {
        addContains(StickersColumns.CONTENT_ID, value);
        return this;
    }

    public StickersSelection contentIdStartsWith(String... value) {
        addStartsWith(StickersColumns.CONTENT_ID, value);
        return this;
    }

    public StickersSelection contentIdEndsWith(String... value) {
        addEndsWith(StickersColumns.CONTENT_ID, value);
        return this;
    }

    public StickersSelection pack(String... value) {
        addEquals(StickersColumns.PACK, value);
        return this;
    }

    public StickersSelection packNot(String... value) {
        addNotEquals(StickersColumns.PACK, value);
        return this;
    }

    public StickersSelection packLike(String... value) {
        addLike(StickersColumns.PACK, value);
        return this;
    }

    public StickersSelection packContains(String... value) {
        addContains(StickersColumns.PACK, value);
        return this;
    }

    public StickersSelection packStartsWith(String... value) {
        addStartsWith(StickersColumns.PACK, value);
        return this;
    }

    public StickersSelection packEndsWith(String... value) {
        addEndsWith(StickersColumns.PACK, value);
        return this;
    }

    public StickersSelection name(String... value) {
        addEquals(StickersColumns.NAME, value);
        return this;
    }

    public StickersSelection nameNot(String... value) {
        addNotEquals(StickersColumns.NAME, value);
        return this;
    }

    public StickersSelection nameLike(String... value) {
        addLike(StickersColumns.NAME, value);
        return this;
    }

    public StickersSelection nameContains(String... value) {
        addContains(StickersColumns.NAME, value);
        return this;
    }

    public StickersSelection nameStartsWith(String... value) {
        addStartsWith(StickersColumns.NAME, value);
        return this;
    }

    public StickersSelection nameEndsWith(String... value) {
        addEndsWith(StickersColumns.NAME, value);
        return this;
    }
}
