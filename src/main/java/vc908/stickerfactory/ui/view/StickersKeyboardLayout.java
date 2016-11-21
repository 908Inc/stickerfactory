/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package vc908.stickerfactory.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import vc908.stickerfactory.StickersKeyboardController;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.utils.Utils;


public class StickersKeyboardLayout extends RelativeLayout {

    private static final String TAG = StickersKeyboardLayout.class.getSimpleName();
    private Rect rect = new Rect();
    private StickersKeyboardController.KeyboardVisibilityChangeListener keyboardVisibilityChangeListener;
    private boolean isKeyboardVisible;

    public interface KeyboardHideCallback {
        void onKeyboardHide();
    }

    public StickersKeyboardLayout(Context context) {
        super(context);
    }

    public StickersKeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickersKeyboardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            int keyboardHeight = StorageManager.getInstance().getKeyboardHeight(Utils.getCurrentOrientation(getContext()));
            View rootView = this.getRootView();
            int usableViewHeight = rootView.getHeight() - Utils.statusBarHeight - Utils.getViewInset(rootView);
            this.getWindowVisibleDisplayFrame(rect);
            int calculatedKeyboardHeight = usableViewHeight - (rect.bottom - rect.top);

            if (calculatedKeyboardHeight > Utils.dp(50, getContext())) {
                isKeyboardVisible = true;
                if (keyboardHeight != calculatedKeyboardHeight) {
                    keyboardHeight = calculatedKeyboardHeight;
                    StorageManager.getInstance().storeKeyboardHeight(Utils.getCurrentOrientation(getContext()), keyboardHeight);
                }
            } else {
                isKeyboardVisible = false;
            }
            if (keyboardVisibilityChangeListener != null) {
                keyboardVisibilityChangeListener.onTextKeyboardVisibilityChanged(isKeyboardVisible);
            }
        }
    }

    public void setKeyboardVisibilityChangeListener(StickersKeyboardController.KeyboardVisibilityChangeListener listener) {
        keyboardVisibilityChangeListener = listener;
    }

    public boolean isKeyboardVisible() {
        return isKeyboardVisible;
    }
}
