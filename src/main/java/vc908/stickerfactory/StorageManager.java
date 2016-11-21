package vc908.stickerfactory;

import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;
import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.analytics.IAnalytics;
import vc908.stickerfactory.billing.Prices;
import vc908.stickerfactory.events.PackMarkStatusChangedEvent;
import vc908.stickerfactory.events.PackTabImageDownloadedEvent;
import vc908.stickerfactory.events.UserShopContentVisitLastModifiedUpdatedEvent;
import vc908.stickerfactory.model.Sticker;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.provider.StickersProvider;
import vc908.stickerfactory.provider.StickersSQLiteOpenHelper;
import vc908.stickerfactory.provider.analytics.AnalyticsColumns;
import vc908.stickerfactory.provider.analytics.AnalyticsContentValues;
import vc908.stickerfactory.provider.analytics.AnalyticsCursor;
import vc908.stickerfactory.provider.analytics.AnalyticsSelection;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.packs.PacksContentValues;
import vc908.stickerfactory.provider.packs.PacksCursor;
import vc908.stickerfactory.provider.packs.PacksSelection;
import vc908.stickerfactory.provider.packs.Status;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksColumns;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksContentValues;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksCursor;
import vc908.stickerfactory.provider.pendingtasks.PendingTasksSelection;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiColumns;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiContentValues;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersContentValues;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersCursor;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersSelection;
import vc908.stickerfactory.provider.stickers.StickersColumns;
import vc908.stickerfactory.provider.stickers.StickersContentValues;
import vc908.stickerfactory.provider.stickers.StickersCursor;
import vc908.stickerfactory.provider.stickers.StickersSelection;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;

/**
 * Storage manager is used for all interaction with internal storage, database and preferences
 *
 * @author Dmitry Nezhydenko
 */
public class StorageManager extends PreferenceHelper {

    private static final String TAG = StorageManager.class.getSimpleName();
    private static final String PREF_KEY_KEYBOARD_HEIGHT = "keyboard_height";
    private static final String PREF_KEY_DEVICE_ID = "pref_key_device_id";
    private static final String PREF_KEY_USER_ID = "user_id_key";
    private static final String PREF_KEY_IS_GCM_TOKEN_SENT_KEY = "sp_is_gcm_token_sent_key";
    private static final String PREF_KEY_CUSTOM_LOCALIZATION = "pref_key_custom_localization";
    private static final String PREF_KEY_CUSTOM_CONTENT_LOCALIZATION = "pref_key_custom_content_localization";
    private static final String PREF_KEY_USER_SUBSCRIPTION = "pref_user_subscription";
    private static final String PREF_KEY_USER_DATA = "pref_key_user_data";
    private static final String PREF_KEY_PRICE_POINTS = "pref_key_pricepoints";
    private static final String PREF_KEY_CONTENT_PACK_NAME_PREFIX = "pref_key_content_pack_name_prefix_";
    private static final String PREF_KEY_SHOP_CONTENT_LAST_MODIFIED = "pref_key_shop_content_last_modified";
    private static final String PREF_KEY_USER_SHOP_CONTENT_VISIT_LAST_MODIFIED = "pref_key_user_shop_content_visit_last_modified";
    private static final String PREF_KEY_PACK_TO_SHOW_NAME = "pref_key_pack_to_show";
    private static final String PREF_KEY_MARKED_PACK_PREFIX = "pref_key_marked_pack_prefix_";
    //    private static final String PREF_KEY_FILTERS = "pref_key_filters";
    private static final String PREF_KEY_USER_SPLIT_GROUP = "pref_key_user_split_group";

    private final AsyncQueryHandler asyncQueryHandler;

    private Context mContext;
    private static StorageManager instance;

    private String userId;

    public static int recentStickersCount = -1;
    private boolean isUserSubscribed;
    private Gson gson;

    /**
     * Private constructor to prevent new objects creating
     *
     * @param context Storage manager context
     */
    private StorageManager(Context context) {
        super(context);
        mContext = context;
        asyncQueryHandler = new AsyncQueryHandler(mContext.getContentResolver()) {
        };
        isUserSubscribed = getBooleanValue(PREF_KEY_USER_SUBSCRIPTION);
        gson = new Gson();
    }

    /**
     * Singleton getter implementation for StorageManager.
     *
     * @return Singleton instance
     * @throws RuntimeException Throws when try to get instance when Stickers manager not initialized
     */
    public static StorageManager getInstance() throws RuntimeException {
        if (instance == null) {
            if (StickersManager.getApplicationContext() == null) {
                throw new RuntimeException("Stickers manager not initialized.");
            }
            instance = new StorageManager(StickersManager.getApplicationContext());
        }
        return instance;
    }

    /**
     * Increment all packs order to given value
     *
     * @param value Order increment value
     */
    private void incrementPacksOrder(int value) {
        StickersSQLiteOpenHelper.getInstance(mContext).getWritableDatabase().execSQL(
                String.format("UPDATE %s SET %s=%s+%s",
                        PacksColumns.TABLE_NAME,
                        PacksColumns.PACK_ORDER,
                        PacksColumns.PACK_ORDER,
                        value));
    }

    @NonNull
    private List<ContentValues> createStickersValues(@NonNull List<Sticker> stickers, @NonNull String pack) {
        List<ContentValues> stickersBulk = new ArrayList<>();
        if (stickers.size() == 0) {
            Logger.w(TAG, "Trying to create values for empty stickers list");
        } else if (TextUtils.isEmpty(pack)) {
            Logger.w(TAG, "Trying to create values for empty pack name");
        } else {
            for (Sticker sticker : stickers) {
                StickersContentValues stickerCv = new StickersContentValues()
                        .putContentId(sticker.getContentId())
                        .putPack(pack.toLowerCase())
                        .putName(sticker.getName().toLowerCase());
                stickersBulk.add(stickerCv.values());
            }
        }
        return stickersBulk;
    }

    /**
     * Store stickers list to database
     *
     * @param stickers Stickers list
     */
    public void storeStickers(@NonNull List<Sticker> stickers, String pack) {
        if (stickers.size() == 0) {
            Logger.w(TAG, "Trying to store empty stickers list");
        } else if (TextUtils.isEmpty(pack)) {
            Logger.w(TAG, "Trying to store stickers with empty pack name");
        } else {
            List<ContentValues> stickersBulk = createStickersValues(stickers, pack);
            mContext.getContentResolver().bulkInsert(StickersColumns.CONTENT_URI, stickersBulk.toArray(new ContentValues[stickersBulk.size()]));
        }
    }

    /**
     * Update last using time for given sticker to current time.
     * Create new record or replace existing with new time
     *
     * @param contentId Content id
     */
    public void updateStickerUsingTime(String contentId) {
        if (!TextUtils.isEmpty(contentId)) {
            RecentlyStickersContentValues contentValues = new RecentlyStickersContentValues()
                    .putContentId(contentId)
                    .putLastUsingTime(System.currentTimeMillis());
            asyncQueryHandler.startInsert(-1, null, RecentlyStickersColumns.CONTENT_URI, contentValues.values());
        }
    }

    /**
     * Update last using time for given emoji to current time.
     * Create new record or replace existing with new time
     *
     * @param code Emoji code
     */
    public void updateEmojiUsingTime(String code) {
        if (!TextUtils.isEmpty(code)) {
            RecentlyEmojiContentValues contentValues = new RecentlyEmojiContentValues()
                    .putCode(code)
                    .putLastUsingTime(System.currentTimeMillis());
            asyncQueryHandler.startInsert(-1, null, RecentlyEmojiColumns.CONTENT_URI, contentValues.values());
        }
    }

    /**
     * Check is stickers list exists at database
     *
     * @return Result of inspection
     */
    public boolean isStickersExists() {
        StickersCursor cursor = new StickersSelection().query(mContext.getContentResolver());
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    /**
     * Add new record to analytics table
     *
     * @param category Item Category
     * @param action   Item action
     * @param label    Item label
     */
    public void addAnalyticsItem(IAnalytics.Category category, String action, String label) {
        addAnalyticsItem(category, action, label, 0);
    }

    /**
     * Add new record to analytics table
     *
     * @param category Item Category
     * @param action   Item action
     * @param label    Item label
     * @param count    Item count
     */
    public void addAnalyticsItem(IAnalytics.Category category, String action, String label, int count) {
        AnalyticsContentValues cv = new AnalyticsContentValues()
                .putCategory(category.getValue())
                .putAction(action)
                .putLabel(label)
                .putEventCount(count)
                .putEventtime(System.currentTimeMillis() / 1000L);
        asyncQueryHandler.startInsert(-1, null, AnalyticsColumns.CONTENT_URI, cv.values());
    }

    /**
     * Send pending analytics events
     */
    public void sendAnalyticsEvents() {
        if (Utils.isNetworkAvailable(mContext)) {
            try {
                AnalyticsCursor cursor = new AnalyticsSelection().query(mContext.getContentResolver());
                if (cursor.getCount() > 0) {
                    JSONArray data = new JSONArray();
                    while (cursor.moveToNext()) {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("category", cursor.getCategory());
                            obj.put("action", cursor.getAction());
                            obj.put("label", cursor.getLabel());
                            obj.put("time", cursor.getEventtime());
                            if (cursor.getEventCount() != null && cursor.getEventCount() > 0) {
                                obj.put("value", cursor.getEventCount());
                            }
                            data.put(obj);
                        } catch (JSONException e) {
                            Logger.e(TAG, "Can't create analytics request", e);
                        }
                    }
                    Logger.d(TAG, "Send analytics data: " + data);
                    NetworkManager.getInstance().sendAnalyticsData(data);
                }
                cursor.close();
            } catch (IllegalArgumentException e) {
                // Unusual behavior when content provider can't process specific uri
            }
        }
    }

    /**
     * Get stored keyboard height according to screen orientation.
     *
     * @param orientation Orientation, like {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE}
     *                    or {@link android.content.res.Configuration#ORIENTATION_PORTRAIT}
     * @return Stored or default value
     */
    public int getKeyboardHeight(int orientation) {
        int height = getIntValue(PREF_KEY_KEYBOARD_HEIGHT + orientation);
        if (height == 0) {
            height = mContext.getResources().getDimensionPixelSize(R.dimen.sp_default_keyboard_size);
        }
        return height;
    }

    /**
     * Store keyboard height according to screen orientation.
     *
     * @param orientation Orientation, like {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE}
     *                    or {@link android.content.res.Configuration#ORIENTATION_PORTRAIT}
     * @param value       Height value
     */
    public void storeKeyboardHeight(int orientation, int value) {
        storeValue(PREF_KEY_KEYBOARD_HEIGHT + orientation, value);
    }

    /**
     * Clear analytics data after successful request
     */
    public void clearAnalytics() {
        mContext.getContentResolver().delete(AnalyticsColumns.CONTENT_URI, null, null);
    }

    /**
     * Update packs order according to its positions at list
     *
     * @param data Ordered packs list
     */
    public void updatePacksOrder(List<String> data) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            operations.add(ContentProviderOperation.newUpdate(
                    PacksColumns.CONTENT_URI)
                    .withSelection(PacksColumns.NAME + "=?", new String[]{data.get(i)})
                    .withValue(PacksColumns.PACK_ORDER, String.valueOf(i))
                    .build());
        }
        try {
            mContext.getContentResolver().applyBatch(StickersProvider.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            Logger.e(TAG, "Can't update packs order", e);
        }
    }

    /**
     * Set pack to inactive and remove related info
     *
     * @param packName Name of pack
     */
    public void deactivatePack(String packName) {
        // Set pack status as inactive
        mContext.getContentResolver().update(
                PacksColumns.CONTENT_URI,
                new PacksContentValues()
                        .putStatus(Status.INACTIVE)
                        .values(),
                PacksColumns.NAME + "=?",
                new String[]{packName});
        // remove stickers from recent table
        removeRecentStickers(packName);
    }

    /**
     * Remove stickers from recent for given pack
     *
     * @param packName Pack name
     */
    private void removeRecentStickers(String packName) {
        // find all stickers from this pack
        StickersCursor cursor = new StickersSelection().pack(packName).query(mContext.getContentResolver());
        if (cursor.getCount() > 0) {
            RecentlyStickersSelection selection = new RecentlyStickersSelection();
            cursor.moveToFirst();
            selection.contentId(cursor.getContentId());
            while (cursor.moveToNext()) {
                selection.or();
                selection.contentId(cursor.getContentId());
            }
            selection.delete(mContext.getContentResolver());
        }
        cursor.close();
    }

    /**
     * Remove pack and all related info
     *
     * @param packName Pack name
     */
    public void removePack(String packName) {
        // Remove from database
        mContext.getContentResolver().delete(
                PacksColumns.CONTENT_URI,
                PacksColumns.NAME + "=?",
                new String[]{packName});
        // Remove related stickers
        mContext.getContentResolver().delete(
                StickersColumns.CONTENT_URI,
                StickersColumns.PACK + "=?",
                new String[]{packName});
        // Remove stickers from recent table
        removeRecentStickers(packName);


    }

    /**
     * Get stored device id value
     */
    public String getDeviceId() {
        return getStringValue(PREF_KEY_DEVICE_ID);
    }

    /**
     * Store device id value to shared preferences
     *
     * @param deviceId Device id
     */
    public void storeDeviceId(String deviceId) {
        storeValue(PREF_KEY_DEVICE_ID, deviceId);
    }

    /**
     * Store status is gcm token sent to serverside
     *
     * @param isSent Is gcm token sent
     */
    public void storeIsGcmTokenSent(boolean isSent) {
        storeValue(PREF_KEY_IS_GCM_TOKEN_SENT_KEY, isSent);
    }

    /**
     * Get status is gcm token sent to serverside
     *
     * @return Is gcm token sent
     */
    public boolean isGcmTokenSent() {
        return getBooleanValue(PREF_KEY_IS_GCM_TOKEN_SENT_KEY);
    }

    /**
     * Get collection with packs info
     *
     * @return Stored packs
     */
    private Map<String, PackInfoHolder> getStoredPacksInfo() {
        Map<String, PackInfoHolder> packs = new HashMap<>();
        PacksCursor cursor = new PacksSelection().query(mContext.getContentResolver());
        while (cursor.moveToNext()) {
            packs.put(cursor.getName(),
                    new PackInfoHolder(
                            cursor.getStatus() == Status.ACTIVE,
                            cursor.getLastModifyDate() != null ? cursor.getLastModifyDate() : 0));
        }
        cursor.close();
        return packs;
    }

    /**
     * Store current authorized user ID, hashed with md5 and salted with API key
     * and user related meta data
     *
     * @param userId       User ID
     * @param clientApiKey Client API key
     * @param data         User relate meta data
     */
    public void storeUser(@Nullable String userId, @NonNull String clientApiKey, @Nullable Map<String, String> data) {
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(clientApiKey)) {
            // try initialize user id
            getUserID();
            String hashedUserId = Utils.md5(userId + clientApiKey);
            boolean isNewUser = TextUtils.isEmpty(this.userId) || !this.userId.equals(hashedUserId);
            this.userId = hashedUserId;
            storeValue(PREF_KEY_USER_ID, hashedUserId);
            if (isNewUser) {
                clearUserData();
                NetworkManager.getInstance().checkPackUpdates();
            }
            data = SplitManager.addSplitData(data);
            // check meta data
            if (data != null && !data.isEmpty()) {
                Map<String, String> storedData = StorageManager.getInstance().getUserData();
                if (!data.equals(storedData) || isNewUser) {
                    StorageManager.getInstance().storeUserdata(data);
                    TasksManager.getInstance().addSendUserDataTask();
                }
            }
        } else if (userId == null) {
            removeValue(PREF_KEY_USER_ID);
            clearUserData();
        }
    }

    /**
     * Get current user split group
     *
     * @return Split group name
     */
    public String getUserSplitGroup() {
        return getStringValue(PREF_KEY_USER_SPLIT_GROUP);
    }

    /**
     * Strore split group for current user
     *
     * @param group Split group name
     */
    public void storeUserSplitGroup(String group) {
        storeValue(PREF_KEY_USER_SPLIT_GROUP, group);
    }


    // Remove all user related data
    private void clearUserData() {
        removeValue(PREF_KEY_USER_SUBSCRIPTION);
        removeValue(PREF_KEY_USER_DATA);
        removeValue(PREF_KEY_SHOP_CONTENT_LAST_MODIFIED);
        removeValue(PREF_KEY_USER_SHOP_CONTENT_VISIT_LAST_MODIFIED);
        removeValue(PREF_KEY_PACK_TO_SHOW_NAME);
        mContext.getContentResolver().delete(PacksColumns.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(StickersColumns.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(RecentlyStickersColumns.CONTENT_URI, null, null);
    }

    /**
     * Get authorized user ID
     *
     * @return vc908.stickerfactory.User ID
     */
    public String getUserID() {
        if (TextUtils.isEmpty(userId)) {
            userId = getStringValue(PREF_KEY_USER_ID);
        }
        return userId;
    }

    /**
     * Check and update packs info from given list
     *
     * @param packs Packs list
     */
    public void updatePacksInfo(@NonNull List<StickersPack> packs) {
        Map<String, PackInfoHolder> storedPacks = getStoredPacksInfo();
        List<ImageForCaching> imagesCacheQueue = new ArrayList<>();
        for (int i = packs.size() - 1; i >= 0; i--) {
            StickersPack pack = packs.get(i);
            if (pack.getUserStatus() == StickersPack.UserStatus.ACTIVE) {
                PackInfoHolder packInfo = storedPacks.get(pack.getName());
                if (packInfo == null) {
                    storeOrUpdatePackInfo(pack);
                    addImagesToCacheQueue(imagesCacheQueue, pack);
                } else {
                    if (pack.getLastModifyDate() > packInfo.lastModifyDate) {
                        // pack info changed, restore pack
                        TasksManager.getInstance().addPackPurchaseTask(pack.getName(), StickersPack.PurchaseType.FREE, false);
                    }
                    // check cached main and tab icons
                    File tabIcon = getImageFile(NamesHelper.getTabIconName(pack.getName()));
                    File mainIcon = getImageFile(NamesHelper.getMainIconName(pack.getName()));
                    if (tabIcon == null || !tabIcon.exists() || mainIcon == null || !mainIcon.exists()) {
                        addImagesToCacheQueue(imagesCacheQueue, pack);
                    }
                }
            } else {
                if (storedPacks.get(pack.getName()) == null && pack.getUserStatus() == StickersPack.UserStatus.HIDDEN) {
                    storePack(pack);
                }
            }
            storedPacks.remove(pack.getName());
        }
        for (String packName : storedPacks.keySet()) {
            removePack(packName);
        }
        // cache images
        if (imagesCacheQueue.size() > 0) {
            Observable.from(imagesCacheQueue)
                    .concatMap(this::downloadPackImage)
                    .onErrorResumeNext(throwable -> {
                        Logger.e(TAG, "Can't cache image", throwable);
                        return Observable.just(null);
                    })
                    .subscribe(result -> {
                        if (result != null) {
                            switch (result.type) {
                                case ImageForCaching.IMAGE_TYPE_TAB:
                                    EventBus.getDefault().post(new PackTabImageDownloadedEvent(result.packName));
                                    break;
                                default:
                                    // nothing to do
                            }
                        }
                    });
        }
    }

    /**
     * Store pack to database
     *
     * @param pack Pack for store
     */
    private void storePack(StickersPack pack) {
        incrementPacksOrder(1);
        storePackMarkedStatus(pack.getName(), true);
        mContext.getContentResolver().insert(
                PacksColumns.CONTENT_URI,
                createPackContentValues(pack, true)
        );
    }

    /**
     * Update store visited if need
     */
    public void storeShopVisited() {
        long shopContentLastModified = getShopContentLastModified();
        if (shopContentLastModified != getUserShopContentVisitLastModifiedDate()) {
            storeUserShopContentVisitLastModifies(shopContentLastModified);
            EventBus.getDefault().post(new UserShopContentVisitLastModifiedUpdatedEvent());
        }
    }

    /**
     * Check is shop has new content, which user don't see
     *
     * @return Result of check
     */
    public boolean isShopHasNewContent() {
        return StorageManager.getInstance().getShopContentLastModified()
                > StorageManager.getInstance().getUserShopContentVisitLastModifiedDate();
    }

    /**
     * Calculate recent stickers count
     */
    public void updateRecentStickersCount() {
        recentStickersCount = new RecentlyStickersSelection().query(mContext.getContentResolver()).getCount();
    }

    /**
     * Try to find pakc at database and check it user status
     *
     * @param packName Pack name
     * @return Pack user status
     */
    @Nullable
    public StickersPack.UserStatus getPackStatus(@NonNull String packName) {
        StickersPack.UserStatus userStatus = StickersPack.UserStatus.NONE;
        PacksCursor cursor = new PacksSelection().name(packName).query(mContext.getContentResolver());
        if (cursor.moveToFirst() && cursor.getStatus() != null) {
            switch (cursor.getStatus()) {
                case ACTIVE:
                    userStatus = StickersPack.UserStatus.ACTIVE;
                    break;
                case INACTIVE:
                    userStatus = StickersPack.UserStatus.HIDDEN;
                    break;
                default:
                    Logger.w(TAG, "Unknown pack status: " + packName);
            }
        }
        cursor.close();
        return userStatus;
    }

    /**
     * Clear stored stickers from cache
     */
    public void clearCache() {
        new Thread(() -> {
            // remove stickers
            StickersSelection stickersSelection = new StickersSelection();
            StickersCursor stickersCursor = stickersSelection.query(mContext.getContentResolver());
            while (stickersCursor.moveToNext()) {
                removeFile(stickersCursor.getContentId());
            }
            stickersSelection.delete(mContext.getContentResolver());
            stickersCursor.close();
            // remove recent stickers
            RecentlyStickersSelection recentlyStickersSelection = new RecentlyStickersSelection();
            RecentlyStickersCursor recentlyStickersCursor = recentlyStickersSelection.query(mContext.getContentResolver());
            while (recentlyStickersCursor.moveToNext()) {
                removeFile(recentlyStickersCursor.getContentId());
            }
            recentlyStickersSelection.delete(mContext.getContentResolver());
            recentlyStickersCursor.close();
        }).start();
    }

    /**
     * Helper class for holding information about image for caching
     */
    private static class ImageForCaching {
        public static final String IMAGE_TYPE_TAB = "tab_image";
        public static final String IMAGE_TYPE_MAIN = "main_image";

        @StringDef({IMAGE_TYPE_TAB, IMAGE_TYPE_MAIN})
        public @interface ImageType {
        }

        private String type;
        private String cacheKey;
        private String url;
        private String packName;

        public ImageForCaching(@ImageType String type, @NonNull String cacheKey, @NonNull String url, String packName) {
            this.type = type;
            this.cacheKey = cacheKey;
            this.url = url;
            this.packName = packName;
        }
    }

    /**
     * Add tab and main icons to images cache queue
     *
     * @param imagesCacheQueue Images cache queue
     * @param pack             Pack with images links
     */
    private void addImagesToCacheQueue(List<ImageForCaching> imagesCacheQueue, StickersPack pack) {
        // add tab icon
        if (pack.getTabIconLinks() != null
                && pack.getTabIconLinks().size() > 0) {
            String imageLink = pack.getTabIconLinks().get(Utils.getDensityName(mContext));
            if (!TextUtils.isEmpty(imageLink)) {
                imagesCacheQueue.add(
                        new ImageForCaching(
                                ImageForCaching.IMAGE_TYPE_TAB,
                                NamesHelper.getTabIconName(pack.getName()),
                                imageLink,
                                pack.getName()));
            }
        }
        // add main icon
        if (pack.getMainIconLinks() != null
                && pack.getMainIconLinks().size() > 0) {
            String imageLink = pack.getMainIconLinks().get(Utils.getDensityName(mContext));
            if (!TextUtils.isEmpty(imageLink)) {
                imagesCacheQueue.add(
                        new ImageForCaching(
                                ImageForCaching.IMAGE_TYPE_MAIN,
                                NamesHelper.getMainIconName(pack.getName()),
                                imageLink,
                                pack.getName()));
            }
        }
    }

    /**
     * Download tab image for given pack
     *
     * @param imageForCaching image for caching
     */
    private Observable<ImageForCaching> downloadPackImage(@NonNull ImageForCaching imageForCaching) {
        return Observable.<ImageForCaching>create(subscriber -> {
            try {
                storeFile(NetworkManager.getInstance().getFile(imageForCaching.url), imageForCaching.cacheKey);
                subscriber.onNext(imageForCaching);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Get packs names
     *
     * @param limit Packs to select
     * @return List of selected packs
     */
    public List<String> getPacksName(int limit) {
        List<String> resultList = new ArrayList<>();
        if (limit > 0) {
            PacksCursor cursor = new PacksSelection()
                    .status(Status.ACTIVE)
                    .query(
                            mContext.getContentResolver(),
                            new String[]{PacksColumns.NAME, PacksColumns.PACK_ORDER},
                            PacksColumns.PACK_ORDER + " ASC LIMIT " + limit);
            while (cursor.moveToNext()) {
                resultList.add(cursor.getName());
            }
            cursor.close();
        }
        return resultList;
    }

    /**
     * Store use isForSubscribers status
     *
     * @param isSubscribed Is user subscribed
     * @param fromSP       Flag, which indicates that user become subscriber form stickerpipe
     */
    public void storeUserSubscriptionStatus(boolean isSubscribed, boolean fromSP) {
        if (isSubscribed != isUserSubscribed) {
            storeValue(PREF_KEY_USER_SUBSCRIPTION, isSubscribed);
            isUserSubscribed = isSubscribed;
            AnalyticsManager.getInstance().onUserSubscriptionStatusChanged(isSubscribed);
            if (isSubscribed && fromSP) {
                AnalyticsManager.getInstance().onUserBecomeSubscriberFromSP();
            }
            NetworkManager.getInstance().checkPackUpdates(true);
        }
    }

    /**
     * Get current user isForSubscribers status
     *
     * @return Is subscribed
     */
    public boolean isUserSubscriber() {
        return isUserSubscribed;
    }

    /**
     * Store custom localization
     *
     * @param localizationCode Localization code
     */
    public void storeCustomLocalization(String localizationCode) {
        storeValue(PREF_KEY_CUSTOM_LOCALIZATION, localizationCode);
    }

    /**
     * Get stored custom localization
     *
     * @return Stored custom localization
     */
    @Nullable
    public String getCustomLocalization() {
        return getStringValue(PREF_KEY_CUSTOM_LOCALIZATION);
    }

    /**
     * Store custom content localization for purchasing
     *
     * @param customContentLocalizationCode Content localization
     */
    public void storeCustomContentLocalization(String customContentLocalizationCode) {
        storeValue(PREF_KEY_CUSTOM_CONTENT_LOCALIZATION, customContentLocalizationCode);
    }

    /**
     * Get stored custom content localization for purchasing
     *
     * @return Stored custom localization
     */
    @Nullable
    public String getCustomContentLocalization() {
        return getStringValue(PREF_KEY_CUSTOM_CONTENT_LOCALIZATION);
    }

    /**
     * Get current custom or default localization
     *
     * @return Localization string
     */
    public String getCurrentLocalization() {
        String storedLocalization = getCustomLocalization();
        if (TextUtils.isEmpty(storedLocalization)) {
            return Utils.getLocalization();
        } else {
            return storedLocalization;
        }
    }

    /**
     * Get user related meta data
     *
     * @return Data
     */

    public Map<String, String> getUserData() {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> data = gson.fromJson(getStringValue(PREF_KEY_USER_DATA), type);
        if (data == null) {
            data = new HashMap<>();
        }
        return data;
    }

    /**
     * Store user related data to preferences
     *
     * @param data vc908.stickerfactory.User data
     */
    public void storeUserdata(Map<String, String> data) {
        storeValue(PREF_KEY_USER_DATA, gson.toJson(data));
    }

    /**
     * Get first stored task from database
     *
     * @return First tasks
     */
    @Nullable
    public TasksManager.Task getFirstPendingTask() {
        TasksManager.Task task = null;
        PendingTasksCursor cursor = new PendingTasksSelection()
                .ispending(true)
                .limit(1)
                .query(mContext.getContentResolver());
        if (cursor.moveToFirst()) {
            try {
                // TODO remove intDef?
                @TasksManager.TaskCategory int category = TasksManager.TASK_CATEGORY_UNKNOWN;
                switch (Integer.valueOf(cursor.getCategory())) {
                    case TasksManager.TASK_CATEGORY_PURCHASE_PACK:
                        category = TasksManager.TASK_CATEGORY_PURCHASE_PACK;
                        break;
                    case TasksManager.TASK_CATEGORY_HIDE_PACK:
                        category = TasksManager.TASK_CATEGORY_HIDE_PACK;
                        break;
                    case TasksManager.TASK_CATEGORY_SEND_USER_DATA:
                        category = TasksManager.TASK_CATEGORY_SEND_USER_DATA;
                        break;
                    case TasksManager.TASK_CATEGORY_SEND_ERROR:
                        category = TasksManager.TASK_CATEGORY_SEND_ERROR;
                        break;
                    case TasksManager.TASK_CATEGORY_SEND_WARNING:
                        category = TasksManager.TASK_CATEGORY_SEND_WARNING;
                        break;
                    case TasksManager.TASK_CATEGORY_SEND_TOKEN:
                        category = TasksManager.TASK_CATEGORY_SEND_TOKEN;
                        break;
                    default:
                }
                task = new TasksManager.Task(
                        category,
                        cursor.getAction(),
                        cursor.getValue()
                );
            } catch (NumberFormatException ignored) {
            }
        }
        cursor.close();
        return task;
    }

    /**
     * Remove pending task from database
     *
     * @param pendingTask Task for remove
     */
    public void removeTask(TasksManager.Task pendingTask) {
        mContext.getContentResolver().delete(
                PendingTasksColumns.CONTENT_URI,
                PendingTasksColumns.CATEGORY + "=? AND " +
                        PendingTasksColumns.ACTION + "=? AND " +
                        PendingTasksColumns.VALUE + "=?",
                new String[]{
                        String.valueOf(pendingTask.getCategory()),
                        pendingTask.getAction(),
                        pendingTask.getValue()
                });
    }

    /**
     * Store pending task at databse
     *
     * @param pendingTask Task for store
     */
    public void storeTask(@NonNull TasksManager.Task pendingTask) {
        if (pendingTask.getCategory() >= 0
                && pendingTask.getAction() != null
                && pendingTask.getValue() != null) {
            PendingTasksContentValues contentValues = new PendingTasksContentValues()
                    .putCategory(String.valueOf(pendingTask.getCategory()))
                    .putAction(pendingTask.getAction())
                    .putValue(pendingTask.getValue());

            asyncQueryHandler.startInsert(-1, null, PendingTasksColumns.CONTENT_URI, contentValues.values());
        }
    }

    /**
     * Get stored price points values
     *
     * @return Prices
     */
    @Nullable
    public Prices getPrices() {
        String serializedPrices = getStringValue(PREF_KEY_PRICE_POINTS);
        if (TextUtils.isEmpty(serializedPrices)) {
            return null;
        } else {
            return gson.fromJson(serializedPrices, Prices.class);
        }
    }

    /**
     * Store prices to preferences
     *
     * @param prices Pricepoints holder
     */
    public void storePrices(@NonNull Prices prices) {
        storeValue(PREF_KEY_PRICE_POINTS, gson.toJson(prices));
    }

    /**
     * Cache image by given url to internal storage
     *
     * @param key       Cache key
     * @param imagesUrl Image url
     * @return Result of caching
     */
    public boolean cacheImage(String key, String imagesUrl) throws IOException {
        if (TextUtils.isEmpty(imagesUrl) || TextUtils.isEmpty(key)) {
            return false;
        } else {
            // check image for existing
            File file = new File(mContext.getFilesDir(), key);
            if (file.exists()) {
                return true;
            }
            byte[] fileBytes = NetworkManager.getInstance().getFile(imagesUrl);
            storeFile(fileBytes, key);
            return true;
        }
    }

    /**
     * Store file to local storage by given name and input bytes.
     *
     * @param bytes Input file bytes
     * @param name  File name
     */
    public void storeFile(byte[] bytes, String name) throws IOException {
        FileOutputStream fOut = mContext.openFileOutput(name, Context.MODE_PRIVATE);
        fOut.write(bytes);
        fOut.close();
    }

    /**
     * Remove file from internal storage
     *
     * @param filename File to remove
     */
    public void removeFile(String filename) {
        mContext.deleteFile(filename);
    }

    /**
     * Process pack and stickers data storing with caching
     *
     * @param pack Pack for cache and store
     * @return Observable for storing process
     */
    public Observable<Boolean> storeOrUpdatePackWithStickers(@NonNull StickersPack pack) {
        return Observable.<Boolean>create(subscriber -> {
            Map<String, String> imagesToCache = new HashMap<>();
//            Filter filter = new Filter(pack.getName());
            // cache tab icon
            if (pack.getTabIconLinks() != null) {
                String imageLink = pack.getTabIconLinks().get(Utils.getDensityName(mContext));
                if (!TextUtils.isEmpty(imageLink)) {
                    imagesToCache.put(NamesHelper.getTabIconName(pack.getName()), imageLink);
                }
            }
            // cache main icon
            if (pack.getMainIconLinks() != null) {
                String imageLink = pack.getMainIconLinks().get(Utils.getDensityName(mContext));
                if (!TextUtils.isEmpty(imageLink)) {
                    imagesToCache.put(NamesHelper.getMainIconName(pack.getName()), imageLink);
                }
            }
            // cache stickers
            if (pack.getStickers() != null && pack.getStickers().size() > 0) {
                for (Sticker sticker : pack.getStickers()) {
                    if (sticker != null
                            && !TextUtils.isEmpty(sticker.getContentId())
                            && sticker.getImageLinks() != null
                            && sticker.getImageLinks().size() > 0) {
                        String imageLink = sticker.getImageLinks().get(Utils.getDensityName(mContext));
                        if (!TextUtils.isEmpty(imageLink)) {
                            imagesToCache.put(sticker.getContentId(), imageLink);
                        }
                    }
//                    if (sticker != null && sticker.getTags().size() > 0) {
//                        Filter.Item item = new Filter.Item(sticker.getContentId());
//                        for (String tag : sticker.getTags()) {
//                            item.getTags().add(tag);
//                        }
//                        filter.getItems().add(item);
//                    }
                }
            }
            if (imagesToCache.size() > 0) {
                for (Map.Entry<String, String> entry : imagesToCache.entrySet()) {
                    try {
                        cacheImage(entry.getKey(), entry.getValue());
                    } catch (IOException e) {
                        subscriber.onError(e);
                        return;
                    }
                }
            } else {
                Logger.w(TAG, "Nothing to cache for pack: " + pack.getName());
            }
            storeOrUpdatePackInfo(pack);
            updateStickersInfo(pack);
//            if (filter.getItems().size() > 0) {
//                StorageManager.getInstance().storeFilter(filter);
//            }

            subscriber.onNext(true);
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

//    private void storeFilter(Filter filter) {
//        Map<String, Filter> currentFilters = getFilters();
//        currentFilters.put(filter.getPackName(), filter);
//        storeValue(PREF_KEY_FILTERS, gson.toJson(currentFilters));
//    }

//    public void removeFilters(Set<String> filters) {
//        Map<String, Filter> currentFilters = getFilters();
//        for (String filterName : filters) {
//            currentFilters.remove(filterName);
//        }
//        storeValue(PREF_KEY_FILTERS, gson.toJson(currentFilters));
//    }

//    public LinkedHashMap<String, Filter> getFilters() {
//        Type type = new TypeToken<LinkedHashMap<String, Filter>>() {
//        }.getType();
//        LinkedHashMap<String, Filter> data = gson.fromJson(getStringValue(PREF_KEY_FILTERS), type);
//        if (data == null) {
//            data = new LinkedHashMap<>();
//        }
//        return data;
//    }

    /**
     * Store or update info about pack.
     * Also pack make active and first
     *
     * @param pack Pack
     */
    private void storeOrUpdatePackInfo(@NonNull StickersPack pack) {
        PacksCursor cursor = new PacksSelection()
                .name(pack.getName())
                .query(mContext.getContentResolver());
        // store or update pack
        if (cursor.moveToFirst()) {
            // update current pack if need
            PacksCursor packsCursor = new PacksCursor(cursor);
            if (packsCursor.getLastModifyDate() == null
                    || pack.getLastModifyDate() > packsCursor.getLastModifyDate()
                    || packsCursor.getStatus() != Status.ACTIVE) {
                boolean setPackFirst = false;
                if (packsCursor.getStatus() != Status.ACTIVE) {
                    incrementPacksOrder(1);
                    setPackFirst = true;
                }
                mContext.getContentResolver().update(
                        PacksColumns.CONTENT_URI,
                        createPackContentValues(pack, setPackFirst),
                        PacksColumns.NAME + "=?",
                        new String[]{pack.getName()});
            }
        } else {
            storePack(pack);
        }
        cursor.close();
    }

    /**
     * Update stickers of given pack
     *
     * @param pack Pack
     */
    private void updateStickersInfo(@NonNull StickersPack pack) {
        // store or remove stickers
        List<String> storedStickers = new ArrayList<>();
        StickersCursor stickersCursor = new StickersSelection().pack(pack.getName()).query(mContext.getContentResolver());
        while (stickersCursor.moveToNext()) {
            storedStickers.add(stickersCursor.getContentId());
        }
        stickersCursor.close();
        // check - if sticker not exists, add for new stickers list
        List<Sticker> newStickers = new ArrayList<>();
        for (Sticker sticker : pack.getStickers()) {
            if (!storedStickers.contains(sticker.getContentId())) {
                newStickers.add(sticker);
            }
            storedStickers.remove(sticker.getContentId());
        }
        // Store new stickers
        if (newStickers.size() > 0) {
            storeStickers(newStickers, pack.getName());
        }
        // Remove deleted stickers from own table and recent table
        if (storedStickers.size() > 0) {
            StickersSelection stickersSelection = new StickersSelection();
            RecentlyStickersSelection recentSelection = new RecentlyStickersSelection();
            for (int i = 0; i < storedStickers.size(); i++) {
                if (i > 0) {
                    stickersSelection.or();
                    recentSelection.or();
                }
                stickersSelection.contentId(storedStickers.get(i)).and().pack(pack.getName());
                recentSelection.contentId(storedStickers.get(i));
            }
            stickersSelection.delete(mContext.getContentResolver());
            recentSelection.delete(mContext.getContentResolver());
        }
    }

    private ContentValues createPackContentValues(@NonNull StickersPack pack, boolean setPackFirst) {
        PacksContentValues values = new PacksContentValues()
                .putName(pack.getName().toLowerCase())
                .putTitle(pack.getTitle())
                .putArtist(pack.getArtist())
                .putLastModifyDate(pack.getLastModifyDate())
                .putStatus(pack.getUserStatus() == StickersPack.UserStatus.ACTIVE ? Status.ACTIVE : Status.INACTIVE);
        if (setPackFirst) {
            values.putPackOrder(0);
        }
        return values.values();
    }


    /**
     * Get image file if exists from internal storage
     *
     * @param contentId Content id
     * @return Pack tab image
     */
    public File getImageFile(@NonNull String contentId) {
        return new File(mContext.getFilesDir(), contentId);
    }

    /**
     * Change task pending status
     *
     * @param task   Task
     * @param status Is task pending
     */
    public void setTaskPendingStatus(TasksManager.Task task, boolean status) {
        mContext.getContentResolver().update(
                PendingTasksColumns.CONTENT_URI,
                new PendingTasksContentValues()
                        .putIspending(status)
                        .values(),
                PendingTasksColumns.CATEGORY + "=? AND "
                        + PendingTasksColumns.ACTION + "=? AND "
                        + PendingTasksColumns.VALUE + "=?",
                new String[]{String.valueOf(task.getCategory()), task.getAction(), task.getValue()});
    }

    /**
     * Set all tasks as pending
     */
    public void setAllTasksPending() {
        mContext.getContentResolver().update(
                PendingTasksColumns.CONTENT_URI,
                new PendingTasksContentValues()
                        .putIspending(true)
                        .values(),
                null,
                null);
    }

    /**
     * Store content pack name at preferences
     *
     * @param contentId Content ID
     * @param pack      Pack name
     */
    public void storeContentPackName(String contentId, String pack) {
        storeValue(PREF_KEY_CONTENT_PACK_NAME_PREFIX + contentId, pack);
    }

    /**
     * Get pack name for given content id from database or preferences
     *
     * @param contentId Content ID
     * @return Pack name
     */
    @Nullable
    public String getContentPackName(String contentId) {
        String packName = null;
        StickersCursor cursor = new StickersSelection()
                .contentId(contentId)
                .query(mContext.getContentResolver());
        if (cursor.moveToFirst()) {
            packName = cursor.getPack();
        }
        if (TextUtils.isEmpty(packName)) {
            packName = getStringValue(PREF_KEY_CONTENT_PACK_NAME_PREFIX + contentId);
        }
        return packName;
    }

    /**
     * Get last modified date for shop content
     *
     * @return Last modified date
     */
    public long getShopContentLastModified() {
        return getLongValue(PREF_KEY_SHOP_CONTENT_LAST_MODIFIED);
    }

    /**
     * Store last modified date for shop content
     *
     * @param date Last modified date
     */
    public void storeShopContentLastModified(long date) {
        storeValue(PREF_KEY_SHOP_CONTENT_LAST_MODIFIED, date);
    }

    /**
     * Get last modified date for shop content, which user visited
     *
     * @return Last modified date
     */
    public long getUserShopContentVisitLastModifiedDate() {
        return getLongValue(PREF_KEY_USER_SHOP_CONTENT_VISIT_LAST_MODIFIED);
    }

    /**
     * Store last modified date for shop content, which user visited
     *
     * @param date Last modified date
     */
    public void storeUserShopContentVisitLastModifies(long date) {
        storeValue(PREF_KEY_USER_SHOP_CONTENT_VISIT_LAST_MODIFIED, date);
    }

    /**
     * Get stored pack name, which need to select at tabs
     *
     * @return Pack name
     */
    public String getPackToShowName() {
        return getStringValue(PREF_KEY_PACK_TO_SHOW_NAME);
    }

    /**
     * Store pack name, which need to select at tabs
     *
     * @param packName Pack name
     */
    public void storePackToShowName(String packName) {
        storeValue(PREF_KEY_PACK_TO_SHOW_NAME, packName);
    }

    /**
     * Clear stored pack name, which need to select at tabs
     */
    public void clearPackToShowName() {
        removeValue(PREF_KEY_PACK_TO_SHOW_NAME);
    }

    /**
     * Store is pack marked
     *
     * @param packName Pack
     * @param isMarked Is marked status
     */
    public void storePackMarkedStatus(String packName, boolean isMarked) {
        if (getIsPackMarked(packName) != isMarked) {
            storeValue(PREF_KEY_MARKED_PACK_PREFIX + packName, isMarked);
            EventBus.getDefault().post(new PackMarkStatusChangedEvent(packName, isMarked));
        }
    }

    public boolean getIsPackMarked(String packName) {
        return getBooleanValue(PREF_KEY_MARKED_PACK_PREFIX + packName);
    }

    /**
     * Inner class for frequently used pack info
     */
    private class PackInfoHolder {
        boolean isActive;
        long lastModifyDate;

        public PackInfoHolder(boolean isActive, long lastModifyDate) {
            this.isActive = isActive;
            this.lastModifyDate = lastModifyDate;
        }
    }

    /**
     * Clear recent emoji table
     */
    public void clearRecentEmoji() {
        mContext.getContentResolver().delete(RecentlyEmojiColumns.CONTENT_URI, null, null);
    }
}
