package vc908.stickerfactory.billing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class Prices {
    private PricePoint pricePointB;
    private PricePoint pricePointC;

    public Prices setPricePointB(@NonNull String value, float label) {
        this.pricePointB = PricePoint.create(value, label, PricePoint.PRICE_TYPE_B);
        return this;
    }

    public Prices setPricePointC(@NonNull String value, float label) {
        this.pricePointC = PricePoint.create(value, label, PricePoint.PRICE_TYPE_C);
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

}
