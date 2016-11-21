package vc908.stickerfactory.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stickers POJO model
 *
 * @author Dmitry Nezhydenko
 */
public class Sticker {
    @Expose
    private String name;

    @Expose
    private String pack;

    @Expose
    @SerializedName("content_id")
    private String contentId;

    @Expose
    @SerializedName("image")
    private Map<String, String> imageLinks;

    @Expose
    private List<String> tags = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getContentId() {

        return contentId;
    }

    public Map<String, String> getImageLinks() {
        return imageLinks;
    }

    public String getPack() {
        return pack;
    }

    public List<String> getTags() {
        return tags;
    }
}
