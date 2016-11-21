package vc908.stickerfactory.provider.pendingtasks;

import vc908.stickerfactory.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Pending tasks
 */
public interface PendingTasksModel extends BaseModel {

    /**
     * Task category
     * Can be {@code null}.
     */
    @Nullable
    String getCategory();

    /**
     * Task action
     * Can be {@code null}.
     */
    @Nullable
    String getAction();

    /**
     * Task value
     * Can be {@code null}.
     */
    @Nullable
    String getValue();

    /**
     * Is task waiting for execution
     * Can be {@code null}.
     */
    @Nullable
    Boolean getIspending();
}
