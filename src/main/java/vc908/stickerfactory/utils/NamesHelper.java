package vc908.stickerfactory.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Common sticker and pack name generator/separator helper
 *
 * @author Dmitry Nezhydenko
 */
public class NamesHelper {
    private static final String STICKER_NAME_FORMAT = "[[%s]]";
    private static final Pattern STICKER_NAME_PATTERN = Pattern.compile("^\\[\\[[a-zA-Z0-9_]+\\]\\]$");
    private static final String PACK_TAB_ICON_SUFFIX = "_tab_icon";
    private static final String PACK_MAIN_ICON_SUFFIX = "_main_icon";


    /**
     * Create sticker's code by given content id
     *
     * @param contentID Content ID
     * @return Sticker code
     */
    public static String getStickerCode(String contentID) {
        return String.format(STICKER_NAME_FORMAT, contentID);
    }

    /**
     * Check, is given code is a sticker code
     *
     * @param code Input code
     * @return Result of check
     */
    public static boolean isSticker(String code) {
        return !TextUtils.isEmpty(code) && STICKER_NAME_PATTERN.matcher(code).matches();
    }

    /**
     * Create tab icon name for given pack
     *
     * @param packName Pack name
     * @return Tab icon name
     */
    public static String getTabIconName(String packName) {
        return packName + PACK_TAB_ICON_SUFFIX;
    }

    /**
     * Create main icon name for given pack
     *
     * @param packName Pack name
     * @return Main icon name
     */
    public static String getMainIconName(String packName) {
        return packName + PACK_MAIN_ICON_SUFFIX;
    }

    /**
     * Get content id from sticker code
     *
     * @param code Sticker code
     * @return Content ID
     */

    @NonNull
    public static String getContentIdFromCode(String code) {
        if (TextUtils.isEmpty(code)) {
            return "";
        } else {
            return code.replace("[", "").replace("]", "");
        }
    }
}
