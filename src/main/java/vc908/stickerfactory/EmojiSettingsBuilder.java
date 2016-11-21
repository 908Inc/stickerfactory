package vc908.stickerfactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class EmojiSettingsBuilder {
    public enum EmojiResourceLocation {
        ASSETS,
        DRAWABLE
    }

    private Map<String, String> customEmojiMap = new HashMap<>();
    private EmojiResourceLocation resourceLocation = EmojiResourceLocation.DRAWABLE;
    private String assetsFolder = "";

    public EmojiSettingsBuilder setCustomEmojiMap(LinkedHashMap<String, String> map) {
        customEmojiMap = map;
        return this;
    }

    public EmojiSettingsBuilder setResourceLocation(EmojiResourceLocation location) {
        resourceLocation = location;
        return this;
    }

    public EmojiSettingsBuilder setAssetsFolder(String folder) {
        assetsFolder = folder;
        return this;
    }

    public Map<String, String> getCustomEmojiMap() {
        return customEmojiMap;
    }

    public EmojiResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public String getAssetsFolder() {
        return assetsFolder;
    }
}
