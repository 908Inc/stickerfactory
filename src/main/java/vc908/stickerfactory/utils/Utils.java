package vc908.stickerfactory.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import vc908.stickerfactory.R;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;

/**
 * Common utils
 *
 * @author Dmitry Nezhydenko
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private static Point size;
    public static int statusBarHeight;
    private static int primaryColor;
    private static int actionBarHeight;
    public static AtomicInteger atomicInteger = new AtomicInteger(1000);

    /**
     * Generate uniq device ID
     *
     * @param context Utils context
     * @return Device ID
     */
    public static String getDeviceId(Context context) {
        String deviceId = StorageManager.getInstance().getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            String tmDevice = "", tmSerial = "", androidId = "";
            try {
                tmSerial = "" + Build.SERIAL;
                tmDevice = "" + Build.DEVICE;
                androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            } catch (Exception ignored) {
            }
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            deviceId = deviceUuid.toString();
            StorageManager.getInstance().storeDeviceId(deviceId);
        }
        return deviceId;
    }

    /**
     * Get screen physical width in pixels according to orientation
     *
     * @return Screen width
     */
    public static int getScreenWidthInPx(Context context) {
        if (size == null) {
            calculateScreenSize(context);
        }
        switch (getCurrentOrientation(context)) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return size.y;
            case Configuration.ORIENTATION_PORTRAIT:
            default:
                return size.x;
        }
    }

    /**
     * Get screen physical height in pixels according to orientation
     *
     * @return Screen width
     */
    public static int getScreenHeightInPx(Context context) {
        if (size == null) {
            calculateScreenSize(context);
        }
        switch (getCurrentOrientation(context)) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return size.x;
            case Configuration.ORIENTATION_PORTRAIT:
            default:
                return size.y;
        }
    }

    /**
     * Calculate screen size
     */
    private static void calculateScreenSize(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        size = new Point();
        switch (getCurrentOrientation(context)) {
            case Configuration.ORIENTATION_LANDSCAPE:
                size.y = display.getWidth();
                size.x = display.getHeight();
                break;
            case Configuration.ORIENTATION_PORTRAIT:
            default:
                size.x = display.getWidth();
                size.y = display.getHeight();
        }
    }

    /**
     * Check, is any network connection available
     *
     * @param context Util context
     * @return Result of inspection
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    /**
     * Convert density independent pixels to pixels
     *
     * @param val     Value to convert
     * @param context Context
     * @return Size in pixels
     */
    public static int dp(int val, Context context) {
        return (int) (context.getResources().getDisplayMetrics().density * val);
    }

    /**
     * Return current screen orientation
     *
     * @return {@link Configuration#ORIENTATION_LANDSCAPE} or Configuration#ORIENTATION_LANDSCAPE
     */
    public static int getCurrentOrientation(Context context) {
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Activity.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    public static int getViewInset(View view) {
        if (view == null || Build.VERSION.SDK_INT < 21) {
            return 0;
        }
        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect) mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Failed to get view inset", e);
        }
        return 0;
    }

    /**
     * Get screen density
     *
     * @param context Util context
     * @return Screen density in PPI
     */
    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density * 160f;
    }

    /**
     * Get density sting representation
     *
     * @param context Context
     * @return Density name
     */
    public static String getDensityName(Context context) {
        if (StickersManager.useMaxImagesSize) {
            return "xxhdpi";
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            if (density >= 3.0) {
                return "xxhdpi";
            } else if (density >= 2.0) {
                return "xhdpi";
            } else if (density >= 1.5) {
                return "hdpi";
            } else {
                return "mdpi";
            }
        }
    }


    /**
     * Create selectable background according to theme
     *
     * @param context Drawable context
     * @return Selector drawable
     */
    public static Drawable createSelectableBackground(Context context) {
        int[] attrs = new int[]{android.R.attr.selectableItemBackground /* index 0 */};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        Drawable drawableFromTheme = ta.getDrawable(0 /* index */);
        ta.recycle();
        return drawableFromTheme;
    }

    /**
     * Get primary color from app theme
     *
     * @param context Current context
     * @return App Theme primary color
     */
    public static int getPrimaryColor(Context context) {
        if (primaryColor == 0) {
            try {
                TypedValue typedValue = new TypedValue();
                TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
                if (a != null) {
                    primaryColor = a.getColor(0, 0);
                    a.recycle();
                }
            } catch (Resources.NotFoundException e) {
                Logger.w(TAG, "Can't find R.attr.colorPrimary");
            }
        }
        return primaryColor == 0 ? context.getResources().getColor(R.color.sp_primary) : primaryColor;
    }

    /**
     * Calculate and return actionbar height
     *
     * @param context Currunt context
     * @return ActinBar height
     */
    public static int getActionBarHeight(Context context) {
        if (actionBarHeight == 0) {
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        }
        return actionBarHeight;
    }

    /**
     * Create md5 hashed string from input string
     *
     * @param str Input string
     * @return Hashed string
     */
    public static String md5(final String str) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Format float price to string
     *
     * @param price Input price
     * @return Formatted string
     */
    @NonNull
    public static String formatPrice(float price) {
        if (price > 0) {
            String priceStr = String.valueOf(price);
            if (priceStr.endsWith(".0")) {
                priceStr = priceStr.replace(".0", "");
            }
            return priceStr;
        } else {
            return "";
        }
    }

    /**
     * Returns device localization
     *
     * @return Current localization
     */
    public static String getLocalization() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * Get current version code
     *
     * @param context Context
     * @return App version code
     */
    public static int getVersionCode(Context context) {
        int v = 0;
        try {
            v = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "PackageManager.NameNotFoundException", e);
        }
        return v;
    }

    /**
     * Create selector with given colors
     *
     * @param normalState  Normal state color
     * @param pressedState Pressed state color
     * @return Drawable selector
     */
    public static StateListDrawable createColorsSelector(@ColorInt int normalState, @ColorInt int pressedState) {
        ColorDrawable clrBase = new ColorDrawable();
        clrBase.setColor(normalState);
        ColorDrawable clrPressed = new ColorDrawable();
        clrPressed.setColor(pressedState);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, clrPressed);
        states.addState(StateSet.WILD_CARD, clrBase);

        return states;
    }

    /**
     * Create blended color with given ratio
     *
     * @param color1 First color
     * @param color2 Second color
     * @param ratio  Blend ratio
     * @return Blended color
     */
    public static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    /**
     * Copy given text to clipboard
     *
     * @param context Context
     * @param text    Text to copy
     */
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("uid", text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Set color filter to ImageView
     *
     * @param context   Context
     * @param imageView ImageView for filtering
     * @param colorRes  Color for filter
     */
    public static void setColorFilter(Context context, ImageView imageView, @ColorRes int colorRes) {
        imageView.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN);
    }

    /**
     * Set color filter to drawable
     *
     * @param context  Context
     * @param drawable Drawable for filtering
     * @param colorRes Color for filter
     */
    public static void setColorFilter(Context context, Drawable drawable, @ColorRes int colorRes) {
        drawable.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN);
    }

    /**
     * Generate random boolean
     *
     * @return Random boolean
     */
    public static boolean randomBoolean() {
        return Math.random() < 0.5f;
    }
}
