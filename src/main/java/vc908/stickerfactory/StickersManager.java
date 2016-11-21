package vc908.stickerfactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;
import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.analytics.LocalAnalyticsImpl;
import vc908.stickerfactory.billing.BillingManager;
import vc908.stickerfactory.billing.Prices;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.provider.StickersProvider;
import vc908.stickerfactory.ui.activity.ShopWebViewActivity;
import vc908.stickerfactory.ui.fragment.StickersFragment;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;

/**
 * Main stickers manager class
 *
 * @author Dmitry Nezhydenko
 */
public class StickersManager {
    private static final String TAG = StickersManager.class.getSimpleName();
    private static StickersManager instance;
    private static Context applicationContext;
    private static String clientApiKey;
    public static Class<? extends ShopWebViewActivity> shopClass = ShopWebViewActivity.class;
    public static boolean isShopEnabled = true;
    public static boolean isEmojiTabEnabled = true;
    public static boolean hideEmptyRecentTab = false;
    public static boolean isSearchTabEnabled = true;
    public static boolean isStickerPreviewEnabled = false;
    private static EmojiSettingsBuilder emojiSettingsBuilder;
    private static Prices prices;
    public static boolean useMaxImagesSize = false;
    public static String defaultTab = StickersFragment.TAB_RECENT;

    /**
     * Manager initialization. Must be call before using.
     *
     * @param apiKey  Client API key
     * @param context Manager context
     * @param isDebug Debug indicator
     */
    public static void initialize(@NonNull String apiKey, @NonNull Context context, boolean isDebug) {
        if (instance == null) {
            instance = new StickersManager(apiKey, context, isDebug);
        } else {
            Logger.e(TAG, "Sticker manager already initialized");
        }
    }

    public static void initialize(@NonNull String apiKey, @NonNull Context context) {
        initialize(apiKey, context, false);
    }

    @Nullable
    public static Context getApplicationContext() {
        return applicationContext;
    }

    /**
     * Initialize {@link BillingManager} by given license key
     *
     * @param licenseKey License key
     */
    public static void setLicenseKey(String licenseKey) {
        if (!TextUtils.isEmpty(licenseKey)) {
            BillingManager.init(licenseKey);
        }
    }

    /**
     * Change logging level
     *
     * @param isEnabled Is logging enabled
     */
    public static void setLoggingEnabled(boolean isEnabled) {
        Logger.setConsoleLoggingEnabled(isEnabled);
    }

    /**
     * Check, is given code is sticker
     *
     * @param code String code
     * @return Result of inspection
     */
    public static boolean isSticker(String code) {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            return false;
        } else {
            return NamesHelper.isSticker(code);
        }
    }

    public static StickerLoader with(Context context) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(context);
        }
    }

    public static StickerLoader with(Activity activity) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(activity);
        }
    }

    public static StickerLoader with(android.support.v4.app.Fragment fragment) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(fragment);
        }
    }

    public static StickerLoader with(FragmentActivity fragmentActivity) throws RuntimeException {
        if (instance == null) {
            Logger.e(TAG, "Stickers manager not initialized");
            throw new RuntimeException("Stickers manager not initialized");
        } else {
            return new StickerLoader(fragmentActivity);
        }
    }

    /**
     * Private constructor to prevent another objects creation. Init main logic
     *
     * @param apiKey  Application api key
     * @param context Manager context
     */
    private StickersManager(String apiKey, Context context, boolean isDebug) {
        applicationContext = context;
        clientApiKey = apiKey;
        setLoggingEnabled(isDebug);
        StickersProvider.initAuthority(context.getPackageName());
        prices = StorageManager.getInstance().getPrices();

        initAnalytics(context, isDebug);
        initEventBus();
        startTasks();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Utils.isNetworkAvailable(context)) {
                    TasksManager.getInstance().executeTasks();
                    if (!StorageManager.getInstance().isStickersExists()) {
                        NetworkManager.getInstance().checkPackUpdates();
                    }
                }
            }
        };
        context.registerReceiver(networkStateReceiver, filter);

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            Utils.statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
    }

    private void initEventBus() {
        try {
            EventBus.builder()
                    .logNoSubscriberMessages(false)
                    .logSubscriberExceptions(false)
                    .installDefaultEventBus();
        } catch (EventBusException e) {
            Logger.e(TAG, "Error when setting default EventBus configs", e);
        }
    }

    /**
     * Start background job
     */
    private void startTasks() {
        JobScheduler.getInstance().start();
    }

    /**
     * Add analytics implementations and init them
     *
     * @param context  Manager context
     * @param isDryRun Disable analytics send for debug builds
     */
    private void initAnalytics(Context context, boolean isDryRun) {
        AnalyticsManager am = AnalyticsManager.getInstance();
        am.addAnalytics(new LocalAnalyticsImpl());
        am.init(context, isDryRun);
    }

    /**
     * Get apiKey manager initialized with
     *
     * @return Api key
     */
    public static String getApiKey() {
        return clientApiKey;
    }

    /**
     * Set current authorized user ID
     *
     * @param userID User ID
     */
    public static void setUserID(@Nullable String userID) {
        StorageManager.getInstance().storeUser(userID, clientApiKey, null);
    }

    public static void setUser(String userId, Map<String, String> data) {
        StorageManager.getInstance().storeUser(userId, clientApiKey, data);
    }

    public static void setUserSubscribed(boolean isSubscribed) {
        setUserSubscribed(isSubscribed, false);
    }

    public static void setUserSubscribed(boolean isSubscribed, boolean fromSP) {
        StorageManager.getInstance().storeUserSubscriptionStatus(isSubscribed, fromSP);
    }

    public static void setHideEmptyRecentTab(boolean hideEmptyRecentTab) {
        StickersManager.hideEmptyRecentTab = hideEmptyRecentTab;
    }

    public static void setIsSearchTabEnabled(boolean isSearchTabEnabled) {
        StickersManager.isSearchTabEnabled = isSearchTabEnabled;
    }

    public static void setUseMaxImagesSize(boolean useMaxImagesSize) {
        StickersManager.useMaxImagesSize = useMaxImagesSize;
    }

    public static void setStickerPreviewEnabled(boolean isEnabled) {
        isStickerPreviewEnabled = isEnabled;
    }

    /**
     * Set pack info activity class inheritor for starting
     *
     * @param clazz Class
     */
    public static void setShopClass(Class<? extends ShopWebViewActivity> clazz) {
        shopClass = clazz;
    }

    /**
     * Send token to serveside
     *
     * @param token GCM token
     */
    public static void sendGcmToken(String token) {
        sendGcmToken(token, NetworkService.TOKEN_TYPE_GCM);
    }

    /**
     * Create task for send token to serveside
     *
     * @param token Push token
     * @param type  Token type
     */
    public static void sendGcmToken(String token, @NetworkService.PushTokenType String type) {
        TasksManager.getInstance().addSendTokenTask(token, type);
    }

    /**
     * Enable and disable shop icon at stickers fragment
     *
     * @param isShopEnabled Is shop icon enabled
     */
    public static void setShopEnabled(boolean isShopEnabled) {
        StickersManager.isShopEnabled = isShopEnabled;
    }

    /**
     * Call when user's message was sent to send this analytics event
     *
     * @param isSticker Is sent message sticker
     */
    public static void onUserMessageSent(boolean isSticker) {
        AnalyticsManager.getInstance().onUserMessageSent(isSticker);
    }

    /**
     * Store custom user's localization
     *
     * @param localizationCode Localization code
     */
    public static void setUserLocalization(String localizationCode) {
        if (!TextUtils.isEmpty(localizationCode)) {
            StorageManager.getInstance().storeCustomLocalization(localizationCode);
        }
    }

    /**
     * Store custom content localization for purchasing
     *
     * @param customContentLocalizationCode Localization code
     */
    public static void setCustomContentLocalization(String customContentLocalizationCode) {
        if (!TextUtils.isEmpty(customContentLocalizationCode)) {
            StorageManager.getInstance().storeCustomContentLocalization(customContentLocalizationCode);
        }
    }

    /**
     * Set custom emoji settings builder
     *
     * @param emojiSettingsBuilder Emoji settings builder
     */
    public static void setEmojiSettingsBuilder(EmojiSettingsBuilder emojiSettingsBuilder) {
        StickersManager.emojiSettingsBuilder = emojiSettingsBuilder;
    }

    /**
     * Get custom emoji settings builder if set
     *
     * @return Emoji settings builder
     */
    @Nullable
    public static EmojiSettingsBuilder getEmojiSettingsBuilder() {
        return emojiSettingsBuilder;
    }

    /**
     * Show pack info screen
     *
     * @param context     Context
     * @param stickerCode Sticker code
     */
    public static void showPackInfoByCode(Context context, String stickerCode) {
        if (!TextUtils.isEmpty(stickerCode)) {
            String packName = StorageManager.getInstance().getContentPackName(NamesHelper.getContentIdFromCode(stickerCode));
            if (!TextUtils.isEmpty(packName)) {
                showPackInfoByPackName(context, packName);
            }
        }
    }

    /**
     * Show pack info screen
     *
     * @param context  Context
     * @param packName Pack name
     */
    public static void showPackInfoByPackName(Context context, String packName) {
        if (!TextUtils.isEmpty(packName)) {
            Intent intent = new Intent(context, StickersManager.shopClass);
            intent.putExtra(ShopWebViewActivity.ARG_PACK_NAME, packName);
            context.startActivity(intent);
        }
    }

    /**
     * Set prices or sku list for price points
     *
     * @param pricesHolder Prices holder
     */
    public static void setPrices(@NonNull Prices pricesHolder) {
        prices = pricesHolder;
        StorageManager.getInstance().storePrices(pricesHolder);
    }

    /**
     * Get prices for price points
     *
     * @return Prices
     */
    @Nullable
    public static Prices getPrices() {
        return prices;
    }

    /**
     * Set emoji tab enabled status
     *
     * @param isEnabled Is tab enabled
     */
    public static void setEmojiTabEnabled(boolean isEnabled) {
        isEmojiTabEnabled = isEnabled;
    }

    /**
     * Call, when user succefully purchase pack
     *
     * @param packName Pack name
     */
    public static void onPackPurchased(String packName) {
        TasksManager.getInstance().addPackPurchaseTask(packName, StickersPack.PurchaseType.ONEOFF, true);
    }

    /**
     * Open stickerpipe shop
     *
     * @param context Context
     */
    public static void openShop(@NonNull Context context) {
        context.startActivity(new Intent(context, StickersManager.shopClass));
    }

    /**
     * Clear all stored stickers
     */
    public static void clearCache() {
        StorageManager.getInstance().clearCache();
    }

    /**
     * Call to update stamps list
     */
    public static void updateStampsForce() {
        NetworkManager.getInstance().updateStamps();
    }

    /**
     * Get stored filters
     *
     * @return Filters list
     */
//    public static LinkedHashMap<String, Filter> getFilters() {
//        return StorageManager.getInstance().getFilters();
//    }
}
