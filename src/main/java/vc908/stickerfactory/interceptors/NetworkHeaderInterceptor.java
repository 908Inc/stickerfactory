package vc908.stickerfactory.interceptors;

import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import vc908.stickerfactory.Constants;
import vc908.stickerfactory.StorageManager;

/**
 * Interceptor for network requests.
 * Use for add custom headers to requests
 *
 * @author Dmitry Nezhydenko
 * @see Interceptor
 */
public class NetworkHeaderInterceptor implements Interceptor {

    private Map<String, String> headers = new HashMap<>();

    public NetworkHeaderInterceptor(String apiKey, String deviceId, String packageName, String density, String sdkVersion) {
        headers.put("Platform", Constants.PLATFORM);
        headers.put("ApiKey", apiKey);
        headers.put("Package", packageName);
        headers.put("DeviceId", deviceId);
        headers.put("Density", density);
        headers.put("SdkVersion", sdkVersion);
    }

    /**
     * Intercept network request chain and add custom headers
     *
     * @param chain Network request chain
     * @return Intercepted request
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        Map<String, String> currentHeader = getHeaders();
        checkForCustomContentLocalization(chain, currentHeader);
        for (Map.Entry<String, String> entry : currentHeader.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        return chain.proceed(builder.build());
    }

    public Map<String, String> getHeaders() {
        HashMap<String, String> headersCopy = new HashMap<>(headers);
        String uid = StorageManager.getInstance().getUserID();
        if (!TextUtils.isEmpty(uid)) {
            headersCopy.put("UserID", uid);
        }
        headersCopy.put("Localization", StorageManager.getInstance().getCurrentLocalization());
        return headersCopy;
    }

    private void checkForCustomContentLocalization(Chain chain, Map<String, String> currentHeaders) {
        String customPurchaseLocalization = StorageManager.getInstance().getCustomContentLocalization();
        if (!TextUtils.isEmpty(customPurchaseLocalization)
                && chain != null
                && chain.request() != null
                && "POST".equals(chain.request().method())
                && chain.request().url() != null
                && chain.request().url().toString().contains("/packs")) {
            currentHeaders.put("Localization", customPurchaseLocalization);
        }
    }
}