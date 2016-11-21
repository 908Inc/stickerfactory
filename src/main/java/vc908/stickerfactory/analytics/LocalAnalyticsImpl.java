package vc908.stickerfactory.analytics;

import android.content.Context;

import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.TasksManager;
import vc908.stickerfactory.utils.Logger;

/**
 * @author Dmitry Nezhydenko
 */
public class LocalAnalyticsImpl implements IAnalytics {
    private static final String TAG = LocalAnalyticsImpl.class.getSimpleName();
    private boolean isDryRun;

    @Override
    public void init(Context context, boolean isDryRun) {
        this.isDryRun = isDryRun;
    }

    @Override
    public void onStickerSelected(String contentId, Action source) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.STICKER, source.getValue(), contentId);
        }
    }

    @Override
    public void onEmojiSelected(String code) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.EMOJI, Action.SOURCE_TAB.getValue(), code);
        }
    }

    @Override
    public void onPackDeleted(String packName) {
        Logger.i(TAG, "Pack deleted event: " + packName);
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.PACK, Action.REMOVE.getValue(), packName);
        }
    }

    @Override
    public void onError(String tag, String message) {
        if (!isDryRun) {
            TasksManager.getInstance().addSendDevReportTask(TasksManager.TASK_CATEGORY_SEND_ERROR, tag, message);
        }
    }

    @Override
    public void onWarning(String tag, String message) {
        if (!isDryRun) {
            TasksManager.getInstance().addSendDevReportTask(TasksManager.TASK_CATEGORY_SEND_WARNING, tag, message);
        }
    }

    @Override
    public void onScreenViewed(String screenName) {
        // nothing to do
    }

    @Override
    public void onUserMessageSent(boolean isSticker) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.MESSAGE, Action.SEND.getValue(), isSticker ? "sticker" : "text");
        }
    }

    @Override
    public void sendUserStatistic(String key, String value) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.CUSTOM, key, value);
        }
    }

    @Override
    public void onSubscriptionDialogShowed(String source, String stickerCode) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.SUBSCRIPTION_SHOW, source, stickerCode);
        }
    }

    @Override
    public void onSubscriptionActivateClicked(String source, String stickerCode) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.SUBSCRIPTION_CLICK, source, stickerCode);
        }
    }

    @Override
    public void onUserSubscriptionStatusChanged(boolean isSubscribed) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.SUBSCRIPTION, Action.CHANGE.getValue(), isSubscribed ? "1" : "0");
        }
    }

    @Override
    public void onUserBecomeSubscriberFromSP() {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.SUBSCRIPTION_SUCCESS, Action.CHANGE.getValue(), "1");
        }
    }

    @Override
    public void onAppOpenedByPush(String pushId) {
        if (!isDryRun) {
            StorageManager.getInstance().addAnalyticsItem(Category.APP_OPEN, Action.PUSH.getValue(), pushId);
        }
    }

}
