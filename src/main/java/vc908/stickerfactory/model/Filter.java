package vc908.stickerfactory.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */

public class Filter {
    private String packName;
    private List<Item> items = new ArrayList<>();

    public Filter(String packName) {
        this.packName = packName;
    }

    public static class Item {
        public Item(String contentId) {
            this.contentId = contentId;
        }

        private String contentId;
        private List<String> tags = new ArrayList<>();

        public String getContentId() {
            return contentId;
        }

        public List<String> getTags() {
            return tags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Item)) return false;

            Item item = (Item) o;

            if (contentId != null ? !contentId.equals(item.contentId) : item.contentId != null)
                return false;
            return tags != null ? tags.equals(item.tags) : item.tags == null;

        }

        @Override
        public int hashCode() {
            int result = contentId != null ? contentId.hashCode() : 0;
            result = 31 * result + (tags != null ? tags.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filter)) return false;

        Filter filter = (Filter) o;

        if (packName != null ? !packName.equals(filter.packName) : filter.packName != null)
            return false;
        return items != null ? items.equals(filter.items) : filter.items == null;

    }

    @Override
    public int hashCode() {
        int result = packName != null ? packName.hashCode() : 0;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }

    public String getPackName() {
        return packName;
    }

    public List<Item> getItems() {
        return items;
    }
}
