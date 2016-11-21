package vc908.stickerfactory.provider.analytics;

import vc908.stickerfactory.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Analytics items list.
 */
public interface AnalyticsModel extends BaseModel {

    /**
     * Event category
     * Can be {@code null}.
     */
    @Nullable
    String getCategory();

    /**
     * Event action
     * Can be {@code null}.
     */
    @Nullable
    String getAction();

    /**
     * Event label
     * Can be {@code null}.
     */
    @Nullable
    String getLabel();

    /**
     * Event count
     * Can be {@code null}.
     */
    @Nullable
    Integer getEventCount();

    /**
     * Event time
     * Can be {@code null}.
     */
    @Nullable
    Long getEventtime();
}
