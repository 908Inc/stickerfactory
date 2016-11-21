package vc908.stickerfactory.billing;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anjlab.android.iab.v3.BillingProcessor;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class BillingManager {
    private static BillingManager instance;
    private String licenseKey;

    private BillingManager(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public static BillingManager getInstance() {
        if (instance == null) {
            instance = new BillingManager(null);
        }
        return instance;
    }

    public static void init(String licenseKey) {
        instance = new BillingManager(licenseKey);
    }

    @Nullable
    public BillingProcessor getBillingProcessor(Context context, BillingProcessor.IBillingHandler handler) {
        if (TextUtils.isEmpty(licenseKey)) {
            return null;
        } else {
            return new BillingProcessor(context, licenseKey, handler);
        }
    }
}
