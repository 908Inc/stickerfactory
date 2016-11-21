package vc908.stickerfactory.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class SwipeToggleViewPager extends ViewPager {
    private boolean isSwipeEnabled = true;

    public SwipeToggleViewPager(Context context) {
        super(context);
    }

    public SwipeToggleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwipeEnable(boolean isEnabled) {
        this.isSwipeEnabled = isEnabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isSwipeEnabled && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isSwipeEnabled && super.onTouchEvent(event);
    }
}
