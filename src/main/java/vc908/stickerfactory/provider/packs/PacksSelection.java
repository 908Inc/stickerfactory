package vc908.stickerfactory.provider.packs;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import vc908.stickerfactory.provider.base.AbstractSelection;

/**
 * Selection for the {@code packs} table.
 */
public class PacksSelection extends AbstractSelection<PacksSelection> {
    @Override
    protected Uri baseUri() {
        return PacksColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code PacksCursor} object, which is positioned before the first entry, or null.
     */
    public PacksCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new PacksCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public PacksCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public PacksCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public PacksSelection id(long... value) {
        addEquals("packs." + PacksColumns._ID, toObjectArray(value));
        return this;
    }

    public PacksSelection name(String... value) {
        addEquals(PacksColumns.NAME, value);
        return this;
    }

    public PacksSelection nameNot(String... value) {
        addNotEquals(PacksColumns.NAME, value);
        return this;
    }

    public PacksSelection nameLike(String... value) {
        addLike(PacksColumns.NAME, value);
        return this;
    }

    public PacksSelection nameContains(String... value) {
        addContains(PacksColumns.NAME, value);
        return this;
    }

    public PacksSelection nameStartsWith(String... value) {
        addStartsWith(PacksColumns.NAME, value);
        return this;
    }

    public PacksSelection nameEndsWith(String... value) {
        addEndsWith(PacksColumns.NAME, value);
        return this;
    }

    public PacksSelection packOrder(Integer... value) {
        addEquals(PacksColumns.PACK_ORDER, value);
        return this;
    }

    public PacksSelection packOrderNot(Integer... value) {
        addNotEquals(PacksColumns.PACK_ORDER, value);
        return this;
    }

    public PacksSelection packOrderGt(int value) {
        addGreaterThan(PacksColumns.PACK_ORDER, value);
        return this;
    }

    public PacksSelection packOrderGtEq(int value) {
        addGreaterThanOrEquals(PacksColumns.PACK_ORDER, value);
        return this;
    }

    public PacksSelection packOrderLt(int value) {
        addLessThan(PacksColumns.PACK_ORDER, value);
        return this;
    }

    public PacksSelection packOrderLtEq(int value) {
        addLessThanOrEquals(PacksColumns.PACK_ORDER, value);
        return this;
    }

    public PacksSelection title(String... value) {
        addEquals(PacksColumns.TITLE, value);
        return this;
    }

    public PacksSelection titleNot(String... value) {
        addNotEquals(PacksColumns.TITLE, value);
        return this;
    }

    public PacksSelection titleLike(String... value) {
        addLike(PacksColumns.TITLE, value);
        return this;
    }

    public PacksSelection titleContains(String... value) {
        addContains(PacksColumns.TITLE, value);
        return this;
    }

    public PacksSelection titleStartsWith(String... value) {
        addStartsWith(PacksColumns.TITLE, value);
        return this;
    }

    public PacksSelection titleEndsWith(String... value) {
        addEndsWith(PacksColumns.TITLE, value);
        return this;
    }

    public PacksSelection artist(String... value) {
        addEquals(PacksColumns.ARTIST, value);
        return this;
    }

    public PacksSelection artistNot(String... value) {
        addNotEquals(PacksColumns.ARTIST, value);
        return this;
    }

    public PacksSelection artistLike(String... value) {
        addLike(PacksColumns.ARTIST, value);
        return this;
    }

    public PacksSelection artistContains(String... value) {
        addContains(PacksColumns.ARTIST, value);
        return this;
    }

    public PacksSelection artistStartsWith(String... value) {
        addStartsWith(PacksColumns.ARTIST, value);
        return this;
    }

    public PacksSelection artistEndsWith(String... value) {
        addEndsWith(PacksColumns.ARTIST, value);
        return this;
    }

    public PacksSelection price(Float... value) {
        addEquals(PacksColumns.PRICE, value);
        return this;
    }

    public PacksSelection priceNot(Float... value) {
        addNotEquals(PacksColumns.PRICE, value);
        return this;
    }

    public PacksSelection priceGt(float value) {
        addGreaterThan(PacksColumns.PRICE, value);
        return this;
    }

    public PacksSelection priceGtEq(float value) {
        addGreaterThanOrEquals(PacksColumns.PRICE, value);
        return this;
    }

    public PacksSelection priceLt(float value) {
        addLessThan(PacksColumns.PRICE, value);
        return this;
    }

    public PacksSelection priceLtEq(float value) {
        addLessThanOrEquals(PacksColumns.PRICE, value);
        return this;
    }

    public PacksSelection status(Status... value) {
        addEquals(PacksColumns.STATUS, value);
        return this;
    }

    public PacksSelection statusNot(Status... value) {
        addNotEquals(PacksColumns.STATUS, value);
        return this;
    }


    public PacksSelection subscription(Boolean value) {
        addEquals(PacksColumns.SUBSCRIPTION, toObjectArray(value));
        return this;
    }

    public PacksSelection lastModifyDate(Long... value) {
        addEquals(PacksColumns.LAST_MODIFY_DATE, value);
        return this;
    }

    public PacksSelection lastModifyDateNot(Long... value) {
        addNotEquals(PacksColumns.LAST_MODIFY_DATE, value);
        return this;
    }

    public PacksSelection lastModifyDateGt(long value) {
        addGreaterThan(PacksColumns.LAST_MODIFY_DATE, value);
        return this;
    }

    public PacksSelection lastModifyDateGtEq(long value) {
        addGreaterThanOrEquals(PacksColumns.LAST_MODIFY_DATE, value);
        return this;
    }

    public PacksSelection lastModifyDateLt(long value) {
        addLessThan(PacksColumns.LAST_MODIFY_DATE, value);
        return this;
    }

    public PacksSelection lastModifyDateLtEq(long value) {
        addLessThanOrEquals(PacksColumns.LAST_MODIFY_DATE, value);
        return this;
    }
}
