package vc908.stickerfactory.events;

import android.support.annotation.NonNull;

import vc908.stickerfactory.TasksManager;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class TaskExecutedEvent {
    private TasksManager.Task pendingTask;
    private boolean isSuccess;

    public TaskExecutedEvent(@NonNull TasksManager.Task pendingTask, boolean isSuccess) {
        this.pendingTask = pendingTask;
        this.isSuccess = isSuccess;
    }

    @NonNull
    public TasksManager.Task getPendingTask() {
        return pendingTask;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
