package vc908.stickerfactory.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;

import vc908.stickerfactory.model.StickersPack;

/**
 * Network stickers response POJO model
 *
 * @author Dmitry Nezhydenko
 */
public class StickersResponse extends NetworkResponseModel<LinkedList<StickersPack>> {

    @Expose
    @SerializedName("meta")
    private ShopMetaInfo metaInfo;

    public static class ShopMetaInfo {
        @Expose
        @SerializedName("shop_last_modified")
        private long shopContentLastModified;

        public long getShopContentLastModified() {
            return shopContentLastModified;
        }
    }

    public ShopMetaInfo getMetaInfo() {
        return metaInfo;
    }
}
