package vc908.stickerfactory.provider.recentlystickers;

import vc908.stickerfactory.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Stickers list.
 */
public interface RecentlyStickersModel extends BaseModel {

    /**
     * Last using time
     * Can be {@code null}.
     */
    @Nullable
    Long getLastUsingTime();

    /**
     * Sticker's content ID
     * Can be {@code null}.
     */
    @Nullable
    String getContentId();
}
