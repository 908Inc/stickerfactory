package vc908.stickerfactory.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class SearchResultItem {

    @Expose
    private String pack;

    @Expose
    @SerializedName("content_id")
    private String contentId;

    @Expose
    private Map<String, String> image = new HashMap<>();

    @Expose
    private List<String> tags = new ArrayList<>();

    public String getPack() {
        return pack;
    }

    public String getContentId() {
        return contentId;
    }

    @NonNull
    public Map<String, String> getImage() {
        return image;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "SearchResultItem{" +
                "pack='" + pack + '\'' +
                ", contentId='" + contentId + '\'' +
                ", image=" + image +
                ", tags=" + tags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResultItem)) return false;

        SearchResultItem that = (SearchResultItem) o;

        if (pack != null ? !pack.equals(that.pack) : that.pack != null) return false;
        return contentId != null ? contentId.equals(that.contentId) : that.contentId == null;

    }

    @Override
    public int hashCode() {
        int result = pack != null ? pack.hashCode() : 0;
        result = 31 * result + (contentId != null ? contentId.hashCode() : 0);
        return result;
    }
}
