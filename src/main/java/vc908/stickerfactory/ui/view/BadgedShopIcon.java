package vc908.stickerfactory.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.events.ShopContentLastModifiedUpdatedEvent;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class BadgedShopIcon extends BaseBadgedStickersButton {
    protected NotificationWatcher mWatcher;

    public BadgedShopIcon(Context context) {
        super(context);
    }

    public BadgedShopIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BadgedShopIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BadgedShopIcon(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    @CheckResult
    protected int getDrawableMarker() {
        return R.drawable.sp_tab_badge;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWatcher = new NotificationWatcher(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatcher != null) {
            mWatcher.unregister();
        }
    }

    private static class NotificationWatcher {

        WeakReference<BadgedShopIcon> mWeakReference;

        public NotificationWatcher(BadgedShopIcon stickersButton) {
            mWeakReference = new WeakReference<>(stickersButton);
            EventBus.getDefault().register(this);
        }

        public void onEvent(ShopContentLastModifiedUpdatedEvent event) {
            BadgedShopIcon button = mWeakReference.get();
            if (button == null) {
                unregister();
            } else {
                button.updateBadgeStatus();
            }
        }

        public void unregister() {
            EventBus.getDefault().unregister(this);
        }
    }

    public void updateBadgeStatus() {
        setIsMarked(StorageManager.getInstance().isShopHasNewContent());
    }
}
