package vc908.stickerfactory.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public final class AspectRatioImageView extends ImageView {
    private float mHeightRatio;

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeightRatio > 0.0) {
            // set the image views size
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width * mHeightRatio);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setRatio(float ratio) {
        mHeightRatio = ratio;
    }

}
