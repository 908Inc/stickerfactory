package vc908.stickerfactory.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;
import java.util.List;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.events.PackMarkStatusChangedEvent;
import vc908.stickerfactory.events.PacksLoadedEvent;
import vc908.stickerfactory.events.RecentStickersCountChangedEvent;
import vc908.stickerfactory.events.ShopContentLastModifiedUpdatedEvent;
import vc908.stickerfactory.events.UserShopContentVisitLastModifiedUpdatedEvent;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class BadgedStickersButton extends BaseBadgedStickersButton {

    NotificationWatcher notificationWatcher;

    public BadgedStickersButton(Context context) {
        super(context);
    }

    public BadgedStickersButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BadgedStickersButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BadgedStickersButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getDrawableMarker() {
        return R.drawable.sp_button_badge;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        notificationWatcher = new NotificationWatcher(this);
        updateBadgeVisibility();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        notificationWatcher.unregister();
    }


    protected void updateBadgeVisibility() {
        if (StorageManager.recentStickersCount < 0) {
            StorageManager.getInstance().updateRecentStickersCount();
        }
        setIsMarked(notificationWatcher.isMarkedPacksExists() || StorageManager.getInstance().isShopHasNewContent());
    }

    private static class NotificationWatcher {

        private static final int MARKED_PACKS_CHECK_COUNT = 3;

        public NotificationWatcher(BadgedStickersButton button) {
            mWeakReference = new WeakReference<>(button);
            EventBus.getDefault().register(this);
        }

        WeakReference<BadgedStickersButton> mWeakReference;

        public void onEvent(PackMarkStatusChangedEvent event) {
            updateBadgeVisibility();
        }

        public void onEvent(PacksLoadedEvent event) {
            updateBadgeVisibility();
        }

        public void onEvent(ShopContentLastModifiedUpdatedEvent event) {
            updateBadgeVisibility();
        }

        public void onEvent(UserShopContentVisitLastModifiedUpdatedEvent event) {
            updateBadgeVisibility();
        }

        public void onEvent(RecentStickersCountChangedEvent event) {
            updateBadgeVisibility();
        }

        private void updateBadgeVisibility() {
            BadgedStickersButton button = mWeakReference.get();
            if (button == null) {
                unregister();
            } else {
                button.updateBadgeVisibility();
            }
        }

        public void unregister() {
            EventBus.getDefault().unregister(this);
        }

        public boolean isMarkedPacksExists() {
            List<String> packs = StorageManager.getInstance().getPacksName(MARKED_PACKS_CHECK_COUNT);
            for (String pack : packs) {
                if (StorageManager.getInstance().getIsPackMarked(pack)) {
                    return true;
                }
            }
            return false;
        }
    }

}
