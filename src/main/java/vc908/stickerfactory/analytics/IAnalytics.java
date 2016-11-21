package vc908.stickerfactory.analytics;

import android.content.Context;

/**
 * Analytics interface. Describe common analytics methods
 *
 * @author Dmytro Nezhydenko
 */
public interface IAnalytics {

    enum Category {
        PACK("pack"),
        STAMP("stamp"),
        STICKER("sticker"),
        EMOJI("emoji"),
        MESSAGE("message"),
        DEV("dev"),
        CUSTOM("custom"),
        SUBSCRIPTION("subscription"),
        SUBSCRIPTION_SUCCESS("subscription_success"),
        SUBSCRIPTION_SHOW("subscription_show"),
        SUBSCRIPTION_CLICK("subscription_click"),
        APP_OPEN("app_open");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    enum Action {
        SOURCE_TAB("tab"),
        SOURCE_SUGGEST("suggest"),
        SOURCE_SEARCH("search"),
        SOURCE_RECENT("recent"),
        INSTALL("install"),
        REMOVE("remove"),
        CHECK("check"),
        CHANGE("change"),
        SHOW("show"),
        SEND("send"),
        ERROR("error"),
        WARNING("warning"),
        PUSH("push");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Initialize analytic instance
     *
     * @param context  Analytic context
     * @param isDryRun Is prevent analytics to send
     */
    void init(Context context, boolean isDryRun);

    /**
     * Call when user select a sticker
     *
     * @param contentId Content ID
     * @param source    Source of selected sticker
     */
    void onStickerSelected(String contentId, Action source);

    /**
     * Call when user select emoji
     *
     * @param code Emoji code
     */
    void onEmojiSelected(String code);

    /**
     * Call when pack deleted by user
     *
     * @param packName PAck name
     */
    void onPackDeleted(String packName);

    /**
     * Call when error occurred
     *
     * @param tag     error tag
     * @param message Error message
     */
    void onError(String tag, String message);

    /**
     * Call when warning occurred
     *
     * @param tag     warning tag
     * @param message Warning message
     */
    void onWarning(String tag, String message);

    /**
     * Call when user opens screen
     *
     * @param screenName Screen name
     */
    void onScreenViewed(String screenName);

    /**
     * Call when user's message was sent
     *
     * @param isSticker Is sent message sticker
     */
    void onUserMessageSent(boolean isSticker);

    /**
     * Call when client want to send custom statistic
     *
     * @param key   Custom statistic key
     * @param value Custom statistic value
     */
    void sendUserStatistic(String key, String value);

    /**
     * Call when subscription dialog shows to user
     *
     * @param source      Dialog showing source
     * @param stickerCode Clicked sticker code
     */
    void onSubscriptionDialogShowed(String source, String stickerCode);

    /**
     * Call when user click to activate button at subscription dialog
     *
     * @param source      Dialog showing source
     * @param stickerCode Clicked sticker code
     */
    void onSubscriptionActivateClicked(String source, String stickerCode);

    /**
     * Call when user's subscription status changes
     *
     * @param isSubscribed New subscription status
     */
    void onUserSubscriptionStatusChanged(boolean isSubscribed);

    /**
     * Call when user become subscriber from stickerpipe
     */
    void onUserBecomeSubscriberFromSP();

    /**
     * Call when app opened by push
     *
     * @param deepLink Deep link
     */
    void onAppOpenedByPush(String deepLink);

}
