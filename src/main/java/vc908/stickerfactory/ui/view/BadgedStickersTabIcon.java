package vc908.stickerfactory.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.events.PackMarkStatusChangedEvent;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class BadgedStickersTabIcon extends BaseBadgedStickersButton {
    private String packName;
    private NotificationWatcher mWatcher;

    public BadgedStickersTabIcon(Context context) {
        super(context);
    }

    public BadgedStickersTabIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BadgedStickersTabIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BadgedStickersTabIcon(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getDrawableMarker() {
        return R.drawable.sp_tab_badge;
    }

    public void setPackName(String packName) {
        this.packName = packName;
        setIsMarked(StorageManager.getInstance().getIsPackMarked(packName));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWatcher = new NotificationWatcher(this, packName);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatcher != null) {
            mWatcher.unregister();
        }
    }

    private static class NotificationWatcher {

        WeakReference<BadgedStickersTabIcon> mWeakReference;
        private String mPackName;

        public NotificationWatcher(BadgedStickersTabIcon stickersButton, String packName) {
            mPackName = packName;
            mWeakReference = new WeakReference<>(stickersButton);
            EventBus.getDefault().register(this);
        }

        public void onEvent(PackMarkStatusChangedEvent event) {
            if (!TextUtils.isEmpty(event.getPackName())) {
                if (event.getPackName().equals(mPackName)) {
                    BadgedStickersTabIcon button = mWeakReference.get();
                    if (button == null) {
                        unregister();
                    } else {
                        button.setIsMarked(event.isMarked());
                    }
                }
            }
        }

        public void unregister() {
            EventBus.getDefault().unregister(this);
        }
    }
}
