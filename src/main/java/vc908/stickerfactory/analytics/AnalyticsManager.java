package vc908.stickerfactory.analytics;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import vc908.stickerfactory.utils.Logger;

/**
 * Class wrapper for holding all analytics implementations and interact
 * with them through {@link IAnalytics} interface
 *
 * @author Dmitry Nezhydenko
 */

public class AnalyticsManager implements IAnalytics {

    private static final String TAG = AnalyticsManager.class.getSimpleName();
    private List<IAnalytics> analyticsList = new ArrayList<>();

    private static AnalyticsManager instance;

    /**
     * Singleton getter implementation
     *
     * @return manager instance
     */
    public static synchronized AnalyticsManager getInstance() {
        if (instance == null) {
            instance = new AnalyticsManager();
        }
        return instance;
    }

    /**
     * Private constructor to prevent new instances creation
     */
    private AnalyticsManager() {
    }

    /**
     * Add analytic implementation to list
     *
     * @param analytics Analytic implementation
     */
    public void addAnalytics(IAnalytics analytics) {
        analyticsList.add(analytics);
    }

    @Override
    public void init(Context context, boolean isDryRun) {
        Logger.i(TAG, "Initialize analytics with dry run key: " + isDryRun);
        for (IAnalytics analytics : analyticsList) {
            analytics.init(context, isDryRun);
        }
    }

    @Override
    public void onStickerSelected(String contentId, Action source) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onStickerSelected(contentId, source);
        }
    }

    @Override
    public void onEmojiSelected(String code) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onEmojiSelected(code);
        }
    }

    @Override
    public void onPackDeleted(String packName) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onPackDeleted(packName);
        }
    }

    @Override
    public void onError(String tag, String message) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onError(tag, message);
        }
    }

    @Override
    public void onWarning(String tag, String message) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onWarning(tag, message);
        }
    }

    @Override
    public void onScreenViewed(String screenName) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onScreenViewed(screenName);
        }
    }

    @Override
    public void onUserMessageSent(boolean isSticker) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onUserMessageSent(isSticker);
        }
    }

    @Override
    public void sendUserStatistic(String key, String value) {
        for (IAnalytics analytics : analyticsList) {
            analytics.sendUserStatistic(key, value);
        }
    }

    @Override
    public void onSubscriptionDialogShowed(String source, String stickerCode) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onSubscriptionDialogShowed(source, stickerCode);
        }
    }

    @Override
    public void onSubscriptionActivateClicked(String source, String stickerCode) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onSubscriptionActivateClicked(source, stickerCode);
        }
    }

    @Override
    public void onUserSubscriptionStatusChanged(boolean isSubscribed) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onUserSubscriptionStatusChanged(isSubscribed);
        }
    }

    @Override
    public void onUserBecomeSubscriberFromSP() {
        for (IAnalytics analytics : analyticsList) {
            analytics.onUserBecomeSubscriberFromSP();
        }
    }

    @Override
    public void onAppOpenedByPush(String pushId) {
        for (IAnalytics analytics : analyticsList) {
            analytics.onAppOpenedByPush(pushId);
        }
    }
}