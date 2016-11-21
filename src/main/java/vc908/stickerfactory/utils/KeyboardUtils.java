package vc908.stickerfactory.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import vc908.stickerfactory.StorageManager;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class KeyboardUtils {
    public static final String TAG = KeyboardUtils.class.getSimpleName();

    public static int getKeyboardHeight(Context context) {
        return StorageManager.getInstance().getKeyboardHeight(Utils.getCurrentOrientation(context));
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}