package vc908.stickerfactory;

import android.text.TextUtils;

import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.ui.fragment.StickersFragment;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class ManagerFacade {
    public static void checkPackUpdates() {
        NetworkManager.getInstance().checkPackUpdates();
    }

    public static void setOpenSearchTab() {
        StorageManager.getInstance().storePackToShowName(StickersFragment.SEARCH_TAB_KEY);
    }

    public static void setOpenTab(String packName) {
        if (!TextUtils.isEmpty(packName)) {
            StorageManager.getInstance().storePackToShowName(packName);
        }
    }

    public static void onAppOpenByPush(String pushId) {
        AnalyticsManager.getInstance().onAppOpenedByPush(pushId);
    }
}
