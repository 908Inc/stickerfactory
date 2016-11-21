package vc908.stickerfactory.billing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class Prices {
    private PricePoint pricePointB;
    private PricePoint pricePointC;
    private String skuB;
    private String skuC;

    public Prices setPricePointB(@NonNull String value, float label) {
        this.pricePointB = PricePoint.create(value, label, PricePoint.PRICE_TYPE_B);
        return this;
    }

    public Prices setPricePointC(@NonNull String value, float label) {
        this.pricePointC = PricePoint.create(value, label, PricePoint.PRICE_TYPE_C);
        return this;
    }


    public Prices setSkuC(String skuC) {
        this.skuC = skuC;
        return this;
    }

    public Prices setSkuB(String skuB) {
        this.skuB = skuB;
        return this;
    }

    @Nullable
    public PricePoint getPricePointC() {
        return pricePointC;
    }

    @Nullable
    public PricePoint getPricePointB() {
        return pricePointB;
    }

    @Nullable
    public String getSkuC() {
        return skuC;
    }

    @Nullable
    public String getSkuB() {
        return skuB;
    }


}
