package vc908.stickerfactory.provider.recentlyemoji;

import vc908.stickerfactory.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Recently used emoji list.
 */
public interface RecentlyEmojiModel extends BaseModel {

    /**
     * Last using time
     * Can be {@code null}.
     */
    @Nullable
    Long getLastUsingTime();

    /**
     * Emoji code
     * Can be {@code null}.
     */
    @Nullable
    String getCode();
}
