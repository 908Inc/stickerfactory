package vc908.stickerfactory.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import vc908.stickerfactory.R;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class TextViewWithCurrencyIcon extends TextView {
    public TextViewWithCurrencyIcon(Context context) {
        super(context);
    }

    public TextViewWithCurrencyIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewWithCurrencyIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextViewWithCurrencyIcon(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setCurrencyIconEnabled(boolean isEnabled) {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, isEnabled ? R.drawable.sp_currency_icon : 0, 0);
    }
}
