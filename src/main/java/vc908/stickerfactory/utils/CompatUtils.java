package vc908.stickerfactory.utils;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
@SuppressLint("NewApi")
public class CompatUtils {

    private static final int SDK_VERSION = android.os.Build.VERSION.SDK_INT;

    public static void setBackgroundDrawable(View view, Drawable drawable) {
        if (view != null && drawable != null) {
            if (SDK_VERSION < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundDrawable(drawable);
            } else {
                view.setBackground(drawable);
            }
        }

    }
}
