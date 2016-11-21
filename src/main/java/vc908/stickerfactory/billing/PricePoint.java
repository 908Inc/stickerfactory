package vc908.stickerfactory.billing;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.security.InvalidParameterException;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class PricePoint {

    public static final String PRICE_TYPE_B = "B";
    public static final String PRICE_TYPE_C = "C";

    private String label;
    private float value;
    private String type;

    public static PricePoint create(@NonNull String label, float value, @NonNull String type) {
        if (TextUtils.isEmpty(label)) {
            throw new InvalidParameterException("Label can non be empty");
        }
        return new PricePoint(label, value, type);
    }

    private PricePoint(String label, float value, String type) {
        this.label = label;
        this.value = value;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public float getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
