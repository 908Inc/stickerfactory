package vc908.stickerfactory.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageButton;

import vc908.stickerfactory.R;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public abstract class BaseBadgedStickersButton extends ImageButton {

    private Bitmap markerBitmap;
    private boolean isMarked;
    private int padding;
    private boolean isPaddingRecalculated;

    public BaseBadgedStickersButton(Context context) {
        super(context);
        init();
    }

    public BaseBadgedStickersButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseBadgedStickersButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseBadgedStickersButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        padding = (int) getContext().getResources().getDimension(R.dimen.material_8);
        Drawable markerLayers = ContextCompat.getDrawable(getContext(), getDrawableMarker());
        int markerSize = (int) getContext().getResources().getDimension(R.dimen.sp_tab_indicator_size);
        markerBitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(markerBitmap);
        markerLayers.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        markerLayers.draw(new Canvas(markerBitmap));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isPaddingRecalculated) {
            if (getMeasuredWidth() > 0) {
                if (getMeasuredWidth() >= Utils.dp(48, getContext())) {
                    padding = Utils.dp(8, getContext());
                } else if (getMeasuredWidth() >= Utils.dp(40, getContext())) {
                    padding = Utils.dp(4, getContext());
                }else{
                    padding = 0;
                }
                isPaddingRecalculated = true;
            }
        }
    }

    @DrawableRes
    protected abstract int getDrawableMarker();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (markerBitmap != null && isMarked) {
            canvas.drawBitmap(markerBitmap, getWidth() - markerBitmap.getWidth() - padding, padding, null);
        }
    }

    public void setIsMarked(boolean isMarked) {
        if (this.isMarked != isMarked) {
            this.isMarked = isMarked;
            invalidate();
        }
    }
}