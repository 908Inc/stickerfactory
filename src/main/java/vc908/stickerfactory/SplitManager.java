package vc908.stickerfactory;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */

public class SplitManager {
    public static final String SPLIT_GROUP_STICKERS_LAYOUT = "split_group_stickers_layout";
    public static final String SPLIT_STATUS_STICKERS_LAYOUT = "split_status_stickers_layout";
    public static final String SPLIT_GROUP_A = "A";
    public static final String SPLIT_GROUP_B = "B";
    public static final String SPLIT_STATUS_ON = "ON";
    public static final String SPLIT_STATUS_OFF = "OFF";

    // split test values
    private static boolean stickerCellSmallSize = false;

    // split test running flags
    private static SplitType stickersLayoutTest;

    public enum SplitType {
        AA,
        AB,
    }

    /**
     * Add split specific data to input user related data
     *
     * @param data Input user related data
     * @return User data with split information
     */
    public static Map<String, String> addSplitData(Map<String, String> data) {
        if (data == null) {
            data = new HashMap<>();
        }
        if (stickersLayoutTest != null) {
            String splitGroup = StorageManager.getInstance().getUserSplitGroup();
            data.put(SplitManager.SPLIT_GROUP_STICKERS_LAYOUT, splitGroup);
            data.put(SplitManager.SPLIT_STATUS_STICKERS_LAYOUT,
                    stickersLayoutTest == SplitType.AA ? SplitManager.SPLIT_STATUS_OFF : SPLIT_STATUS_ON);
        }
        return data;
    }

    public static void enableStickersLayoutCellTest(SplitType splitType) {
        stickersLayoutTest = splitType;
        checkUserSplitGroup();
    }

    private static void checkUserSplitGroup() {
        String storedSplitGroup = StorageManager.getInstance().getUserSplitGroup();
        if (TextUtils.isEmpty(storedSplitGroup)) {
            storedSplitGroup = Utils.randomBoolean() ? SplitManager.SPLIT_GROUP_A : SplitManager.SPLIT_GROUP_B;
            StorageManager.getInstance().storeUserSplitGroup(storedSplitGroup);
        }
    }

    public static boolean isStickerCellSmallSize() {
        if (stickersLayoutTest == null) {
            return stickerCellSmallSize;
        } else {
            switch (stickersLayoutTest) {
                case AA:
                default:
                    return false;
                case AB:
                    String splitGroup = StorageManager.getInstance().getUserSplitGroup();
                    return SPLIT_GROUP_B.equals(splitGroup);
            }
        }
    }

    // Manually setup
    public static void setStickerCellSmallSize(boolean isSmallSizeEnabled) {
        stickerCellSmallSize = isSmallSizeEnabled;
    }

}
