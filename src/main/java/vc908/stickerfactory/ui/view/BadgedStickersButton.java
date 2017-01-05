package vc908.stickerfactory.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import vc908.stickerfactory.R;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class BadgedStickersButton extends BadgedButton {

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


}
