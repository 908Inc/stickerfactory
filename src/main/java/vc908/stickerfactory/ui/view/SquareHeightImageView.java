package vc908.stickerfactory.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class SquareHeightImageView extends ImageView {
    public SquareHeightImageView(Context context) {
        super(context);
    }

    public SquareHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareHeightImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareHeightImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
