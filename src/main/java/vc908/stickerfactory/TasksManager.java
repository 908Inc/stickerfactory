package vc908.stickerfactory;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.io.IOException;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.adapter.rxjava.HttpException;
import vc908.stickerfactory.events.PendingTasksCompletedEvent;
import vc908.stickerfactory.events.TaskExecutedEvent;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class TasksManager {
    private static final String TAG = TasksManager.class.getSimpleName();
    private final Context mContext;
    private Task currentTask;

    public void addPackPurchaseTask(String packName, StickersPack.PurchaseType type, boolean selectTabAfterInstall) {
        TasksManager.Task pendingTask = new TasksManager.Task(
                TASK_CATEGORY_PURCHASE_PACK,
                packName,
                type.toString()

        );
        if (selectTabAfterInstall) {
            StorageManager.getInstance().storePackToShowName(packName);
        }
        addTask(pendingTask);
    }

    public void addRemovePackTask(String packName) {
        TasksManager.Task task = new TasksManager.Task(
                TASK_CATEGORY_HIDE_PACK,
                "",
                packName);
        addTask(task);
    }

    public void addSendUserDataTask() {
        Task task = new Task(
                TASK_CATEGORY_SEND_USER_DATA,
                "",
                "");
        addTask(task);
    }

    public void addSendTokenTask(String token, String type) {
        TasksManager.Task task = new TasksManager.Task(
                TASK_CATEGORY_SEND_TOKEN,
                type,
                token);
        addTask(task);
    }

    public void addSendDevReportTask(@TaskCategory int taskCategory, String tag, String message) {
        Task task = new Task(
                taskCategory,
                tag,
                message);
        addTask(task);
    }

    public static final String DEV_REPORT_CATEGORY_ERROR = "error";
    public static final String DEV_REPORT_CATEGORY_WARNING = "warning";

    @StringDef({DEV_REPORT_CATEGORY_ERROR, DEV_REPORT_CATEGORY_WARNING})
    public @interface DevReportCategory {
    }

    public static final int TASK_CATEGORY_UNKNOWN = -1;
    public static final int TASK_CATEGORY_PURCHASE_PACK = 0;
    public static final int TASK_CATEGORY_HIDE_PACK = 1;
    public static final int TASK_CATEGORY_SEND_USER_DATA = 2;
    public static final int TASK_CATEGORY_SEND_ERROR = 3;
    public static final int TASK_CATEGORY_SEND_WARNING = 4;
    public static final int TASK_CATEGORY_SEND_TOKEN = 5;

    @IntDef({TASK_CATEGORY_PURCHASE_PACK, TASK_CATEGORY_HIDE_PACK, TASK_CATEGORY_SEND_USER_DATA, TASK_CATEGORY_SEND_ERROR, TASK_CATEGORY_SEND_WARNING, TASK_CATEGORY_UNKNOWN, TASK_CATEGORY_SEND_TOKEN})
    public @interface TaskCategory {
    }

    private static TasksManager instance;

    public static TasksManager getInstance() throws RuntimeException {
        if (instance == null) {
            if (StickersManager.getApplicationContext() == null) {
                throw new RuntimeException("Stickers manager not initialized.");
            }
            instance = new TasksManager(StickersManager.getApplicationContext());
        }
        return instance;
    }

    private TasksManager(Context context) {
        this.mContext = context;
    }

    public static final class Task {
        private
        @TaskCategory
        int category;
        private String action;
        private String value;

        public Task(@TaskCategory int category, String action, String value) {
            this.category = category;
            this.action = action;
            this.value = value;
        }

        public
        @TaskCategory
        int getCategory() {
            return category;
        }

        public String getAction() {
            return action;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Task that = (Task) o;

            if (category != that.category) return false;
            if (action != null ? !action.equals(that.action) : that.action != null) return false;
            return !(value != null ? !value.equals(that.value) : that.value != null);

        }

        @Override
        public int hashCode() {
            int result = category;
            result = 31 * result + (action != null ? action.hashCode() : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "category=" + category +
                    ", action='" + action + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    private void startNextTask() {
        if (currentTask == null) {
            try {
                if (Utils.isNetworkAvailable(mContext)) {
                    Task task = StorageManager.getInstance().getFirstPendingTask();
                    if (task != null) {
                        executeTask(task);
                    } else {
                        StorageManager.getInstance().setAllTasksPending();
                        EventBus.getDefault().post(new PendingTasksCompletedEvent());
                    }
                } else {
                    StorageManager.getInstance().setAllTasksPending();
                }
            } catch (IllegalArgumentException e){
                // Unusual behavior when content provider can't process specific uri
                // Appears for some clients when closing app
            }
        }
    }

    public void executeTasks() {
        startNextTask();
    }

    private void addTask(Task task) {
        StorageManager.getInstance().storeTask(task);
        startNextTask();
    }

    private void executeTask(Task task) {
        currentTask = task;
        switch (task.getCategory()) {
            case TASK_CATEGORY_PURCHASE_PACK:
                executePurchasePackTask(task);
                break;
            case TASK_CATEGORY_HIDE_PACK:
                executeHidePackTask(task);
                break;
            case TASK_CATEGORY_SEND_USER_DATA:
                executeSendUserDataTask(task);
                break;
            case TASK_CATEGORY_SEND_ERROR:
                executeSendReportTask(DEV_REPORT_CATEGORY_ERROR, task);
                break;
            case TASK_CATEGORY_SEND_WARNING:
                executeSendReportTask(DEV_REPORT_CATEGORY_WARNING, task);
                break;
            case TASK_CATEGORY_SEND_TOKEN:
                executeSendTokenTask(task);
                break;
            case TASK_CATEGORY_UNKNOWN:
            default:
                Logger.w(TAG, "Unknown category of task");
                onTaskCompleted(task, false, false);
        }
    }

    private void executeSendReportTask(@DevReportCategory String category, Task task) {
        NetworkManager.getInstance().sendDeveloperReport(category, task.getAction(), task.getValue()).subscribe(
                response -> onTaskCompleted(task, true, false),
                th -> {
                    onTaskCompleted(task, false, isNeedToRetryTask(th));
                }
        );
    }

    private void executeSendUserDataTask(Task task) {
        Map<String, String> userData = StorageManager.getInstance().getUserData();
        NetworkManager.getInstance().requestSendUserData(userData).subscribe(
                r -> onTaskCompleted(task, true, false),
                th -> {
                    Logger.e(TAG, "Can't complete send user data task", th);
                    onTaskCompleted(task, false, isNeedToRetryTask(th));
                }
        );
    }

    private void executeHidePackTask(Task task) {
        NetworkManager.getInstance().requestHidePack(task.getValue())
                .subscribe(
                        response -> onTaskCompleted(task, true, false),
                        th -> {
                            Logger.e(TAG, "Can't complete pack hide task", th);
                            onTaskCompleted(task, false, isNeedToRetryTask(th));
                        });
    }

    private void executePurchasePackTask(Task task) {
        try {
            StickersPack.PurchaseType purchaseType = StickersPack.PurchaseType.valueOf(task.getValue());
            NetworkManager.getInstance().requestPackPurchase(task.getAction(), purchaseType)
                    .flatMap(response -> StorageManager.getInstance().storeOrUpdatePackWithStickers(response.getData()))
                    .subscribe(
                            result -> onTaskCompleted(task, true, false),
                            th -> {
                                Logger.e(TAG, "Can't complete pack storing", th);
                                onTaskCompleted(task, false, isNeedToRetryTask(th));
                            });

        } catch (IllegalArgumentException e) {
            Logger.w(TAG, "Can't parse purchase type: " + task.getValue());
            onTaskCompleted(task, false, false);
        }
    }

    private void executeSendTokenTask(Task task) {
        NetworkManager.getInstance().requestSendToken(task.getValue(), task.getAction())
                .subscribe(
                        response -> {
                            if (NetworkService.TOKEN_TYPE_GCM.equals(task.getAction())) {
                                StorageManager.getInstance().storeIsGcmTokenSent(true);
                            }
                            onTaskCompleted(task, true, false);
                        },
                        th -> {
                            Logger.e(TAG, "Can't complete send token task", th);
                            onTaskCompleted(task, false, isNeedToRetryTask(th));
                        });
    }

    private boolean isNeedToRetryTask(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            switch (httpException.code()) {
                case 503:
                    return true;
                default:
                    return false;
            }
        } else {
            return throwable instanceof IOException;
        }
    }

    private void onTaskCompleted(Task task, boolean isSuccess, boolean retryTask) {
        EventBus.getDefault().post(new TaskExecutedEvent(task, isSuccess));
        if (retryTask) {
            StorageManager.getInstance().setTaskPendingStatus(task, false);
        } else {
            StorageManager.getInstance().removeTask(task);
        }
        currentTask = null;
        startNextTask();
    }
}
