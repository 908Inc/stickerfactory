package vc908.stickerfactory.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stickers pack POJO model
 *
 * @author Dmitry Nezhydenko
 */
public class StickersPack {

    public enum Type {
        PAID("paid"),
        FREE("free");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum PurchaseType {
        FREE("free"),
        SUBSCRIPTION("subscription"),
        ONEOFF("oneoff");

        private final String type;

        PurchaseType(String type) {
            this.type = type;
        }

        public String getValue() {
            return type;
        }
    }

    public enum UserStatus {
        @SerializedName("none")
        NONE,
        @SerializedName("active")
        ACTIVE,
        @SerializedName("hidden")
        HIDDEN
    }

    @Expose
    private String title;

    @Expose
    private String artist;

    @Expose
    private String description;

    @Expose
    @SerializedName("updated_at")
    private long lastModifyDate;

    @Expose
    @SerializedName("pack_name")
    private String name;
    @Expose
    List<Sticker> stickers = new ArrayList<>();

    @Expose
    @SerializedName("user_status")
    UserStatus userStatus;

    @Expose
    @SerializedName("main_icon")
    private Map<String, String> mainIconLinks;

    @Expose
    @SerializedName("tab_icon")
    private Map<String, String> tabIconLinks;

    public String getName() {
        return name;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public Type getType() {
        return Type.FREE;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDescription() {
        return description;
    }

    public long getLastModifyDate() {
        return lastModifyDate;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public Map<String, String> getMainIconLinks() {
        return mainIconLinks;
    }

    public Map<String, String> getTabIconLinks() {
        return tabIconLinks;
    }

}
