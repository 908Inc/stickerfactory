package vc908.stickerfactory.provider.packs;

import vc908.stickerfactory.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Packs list
 */
public interface PacksModel extends BaseModel {

    /**
     * Pack name
     * Can be {@code null}.
     */
    @Nullable
    String getName();

    /**
     * Pack order
     * Can be {@code null}.
     */
    @Nullable
    Integer getPackOrder();

    /**
     * Pack title
     * Can be {@code null}.
     */
    @Nullable
    String getTitle();

    /**
     * Pack Artist
     * Can be {@code null}.
     */
    @Nullable
    String getArtist();

    /**
     * Pack price
     * Can be {@code null}.
     */
    @Nullable
    Float getPrice();

    /**
     * Pack status
     * Can be {@code null}.
     */
    @Nullable
    Status getStatus();

    /**
     * Is pack available on subscription
     * Can be {@code null}.
     */
    @Nullable
    Boolean getSubscription();

    /**
     * Pack lat modify date
     * Can be {@code null}.
     */
    @Nullable
    Long getLastModifyDate();
}
