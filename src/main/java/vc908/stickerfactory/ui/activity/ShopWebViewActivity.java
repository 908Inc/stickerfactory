package vc908.stickerfactory.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.NetworkManager;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.TasksManager;
import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.billing.BillingManager;
import vc908.stickerfactory.billing.PricePoint;
import vc908.stickerfactory.billing.Prices;
import vc908.stickerfactory.events.TaskExecutedEvent;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class ShopWebViewActivity extends BaseActivity implements BillingProcessor.IBillingHandler {

    public static final String ARG_PACK_NAME = "arg_sticker_code";
    private static final String TAG = ShopWebViewActivity.class.getSimpleName();
    private static final String BUNDLE_KEY_SAVED_URL = "bundle_key_saved_url";
    private static final int RGB_HEX_LENGTH = 6;

    private static final String JS_METHOD_PURCHASE_SUCCESS = "onPackPurchaseSuccess()";
    private static final String JS_METHOD_PURCHASE_FAIL = "onPackPurchaseFail()";
    private static final String JS_METHOD_REMOVE_SUCCESS = "onPackRemoveSuccess()";
    private static final String JS_METHOD_REMOVE_FAIL = "onPackRemoveFail()";

    @StringDef({
            JS_METHOD_PURCHASE_SUCCESS,
            JS_METHOD_PURCHASE_FAIL,
            JS_METHOD_REMOVE_SUCCESS,
            JS_METHOD_REMOVE_FAIL
    })
    @interface JsMethod {
    }

    private String packName;
    private WebView mWebView;
    private View mProgressView;
    private BillingProcessor mBillingProcessor;
    private Handler mHandler;
    private String priceBLabel;
    private String priceCLabel;
    private TextView mMessageView;
    private View mErrorContainer;
    private String savedUrl;
    private String packForPurchase;

    @Override
    public String getScreenName() {
        return "PackInfoWeb";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_collections);
        if (menuItem != null) {
            Drawable iconDrawable = menuItem.getIcon();
            if (iconDrawable != null) {
                iconDrawable.setColorFilter(ContextCompat.getColor(this, R.color.sp_stickers_tab_icons_filter), PorterDuff.Mode.SRC_IN);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_collections) {
            showCollections();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sp_activity_web);
        mHandler = new Handler(getMainLooper());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        processIntent(getIntent());
        if (savedInstanceState != null) {
            savedUrl = savedInstanceState.getString(BUNDLE_KEY_SAVED_URL);
        }
        mWebView = (WebView) findViewById(R.id.web_view);
        mProgressView = findViewById(R.id.progress);
        mProgressView.setOnTouchListener((v, event) -> true);
        mMessageView = (TextView) findViewById(R.id.message);
        View mBtnReload = findViewById(R.id.btn_reload);
        mBtnReload.setOnClickListener(v -> startLoading());
        mErrorContainer = findViewById(R.id.error_container);
        AndroidJsInterface jsInterface = new AndroidJsInterface(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.addJavascriptInterface(jsInterface, "AndroidJsInterface");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Logger.e(TAG, "Can't load page: " + failingUrl + ". Description: " + description);
                clearWebView();
                showError();
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());
        startLoading();
    }

    private void processIntent(Intent intent) {
        packName = intent.getStringExtra(ARG_PACK_NAME);
    }

    private void clearWebView() {
        if (mWebView != null) {
            mWebView.clearView();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
        clearWebView();
        loadShop();
    }

    private void openPackOnTab(@NonNull String packName) {
        StorageManager.getInstance().storePackToShowName(packName);
        finish();
    }

    private void removePack(String packName) {
        if (!TextUtils.isEmpty(packName)) {
            StorageManager.getInstance().deactivatePack(packName);
            TasksManager.getInstance().addRemovePackTask(packName);
            AnalyticsManager.getInstance().onPackDeleted(packName);
            executeJs(JS_METHOD_REMOVE_SUCCESS);
        } else {
            Logger.w(TAG, "Trying to remove pack with empty name");
            executeJs(JS_METHOD_REMOVE_FAIL);
        }
    }

    private void startLoading() {
        if (Utils.isNetworkAvailable(this)) {
            Prices prices = StickersManager.getPrices();
            if (prices != null) {
                if (!TextUtils.isEmpty(prices.getSkuB()) && !TextUtils.isEmpty(prices.getSkuC())) {
                    mBillingProcessor = BillingManager.getInstance().getBillingProcessor(this, this);
                    setInProgress(true);
                } else {
                    if (prices.getPricePointB() != null && !TextUtils.isEmpty(prices.getPricePointB().getLabel())) {
                        priceBLabel = prices.getPricePointB().getLabel();
                    }
                    if (prices.getPricePointC() != null && !TextUtils.isEmpty(prices.getPricePointC().getLabel())) {
                        priceCLabel = prices.getPricePointC().getLabel();
                    }
                    loadShop();
                }
            } else {
                loadShop();
            }
        } else {
            showError(R.string.sp_no_internet_connection);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        packForPurchase = "";
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void loadShop() {
        setInProgress(true);
        if (TextUtils.isEmpty(savedUrl)) {
            try {
                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(priceBLabel)) {
                    params.put("priceB", priceBLabel);
                }
                if (!TextUtils.isEmpty(priceCLabel)) {
                    params.put("priceC", priceCLabel);
                }
                if (StorageManager.getInstance().isUserSubscriber()) {
                    params.put("is_subscriber", "1");
                }
                params.put("primaryColor", getPrimaryColor());
                loadUrl(buildUrl(params), buildHeaders());
            } catch (Exception e) {
                e.printStackTrace();
                showError();
            }
        } else {
            loadUrl(savedUrl, buildHeaders());
            savedUrl = null;
        }
    }

    private String getPrimaryColor() {
        String hexString = Integer.toHexString(ContextCompat.getColor(this, R.color.sp_primary));
        if (hexString.length() > RGB_HEX_LENGTH) {
            hexString = hexString.substring(hexString.length() - RGB_HEX_LENGTH, hexString.length());
        }
        return hexString;
    }

    private void loadUrl(String url, Map<String, String> headers) {
        mErrorContainer.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url, headers);
    }

    private Map<String, String> buildHeaders() {
        return NetworkManager.getInstance().getHeaderInterceptor().getHeaders();
    }

    private String buildUrl(Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(prepareBaseUrl(NetworkManager.API_URL));
        urlBuilder.append("web?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(entry.getKey());
            urlBuilder.append("=");
            urlBuilder.append(entry.getValue());
            urlBuilder.append("&");
        }
        if (TextUtils.isEmpty(packName)) {
            urlBuilder.append("#/store");
        } else {
            urlBuilder.append("#/packs/");
            urlBuilder.append(packName);
        }
        return urlBuilder.toString();
    }

    private String prepareBaseUrl(@NonNull String url) {
        return url.replace("https", "http");
    }

    private void showError() {
        showError(R.string.sp_cant_process_request);
    }

    private void showError(@StringRes int messageId) {
        setInProgress(false);
        mWebView.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        mMessageView.setText(messageId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBillingProcessor != null) {
            mBillingProcessor.release();
        }
    }

    @Override
    public void onProductPurchased(String sku, TransactionDetails transactionDetails) {
        purchasePack(packName, StickersPack.PurchaseType.ONEOFF);
        mBillingProcessor.consumePurchase(sku);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int i, Throwable throwable) {
        Toast.makeText(this, R.string.sp_cant_process_request, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {
        setInProgress(false);
        Prices prices = StickersManager.getPrices();
        if (prices != null) {
            priceBLabel = getProductPrice(prices.getSkuB());
            priceCLabel = getProductPrice(prices.getSkuC());
            loadShop();
        } else {
            showError();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_KEY_SAVED_URL, mWebView.getUrl());
        super.onSaveInstanceState(outState);
    }

    public static class AndroidJsInterface {

        private final WeakReference<ShopWebViewActivity> mContextReference;

        public AndroidJsInterface(@NonNull ShopWebViewActivity context) {
            mContextReference = new WeakReference<>(context);
        }

        @JavascriptInterface
        public void showCollections() {
            if (mContextReference.get() != null) {
                mContextReference.get().showCollections();
            }
        }

        @JavascriptInterface
        public void showHTML(String html) {
            if (mContextReference.get() != null) {
                new AlertDialog.Builder(mContextReference.get()).setTitle("HTML").setMessage(html)
                        .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
            }
        }

        @JavascriptInterface
        public void purchasePack(String packTitle, String packName, String pricePoint) {
            if (mContextReference.get() != null) {
                mContextReference.get().mHandler.post(() -> mContextReference.get().purchase(packTitle, packName, pricePoint));
            }
        }

        @JavascriptInterface
        public void setInProgress(boolean inProgress) {
            if (mContextReference.get() != null) {
                mContextReference.get().mHandler.post(() -> mContextReference.get().setInProgress(inProgress));
            }
        }

        @JavascriptInterface
        public void removePack(String packName) {
            if (mContextReference.get() != null) {
                mContextReference.get().mHandler.post(() -> mContextReference.get().removePack(packName));
            }
        }

        @JavascriptInterface
        public void showPack(String packName) {
            if (mContextReference.get() != null) {
                mContextReference.get().mHandler.post(() -> mContextReference.get().openPackOnTab(packName));
            }
        }
    }

    private void purchase(String packTitle, String packName, String pricePoint) {
        packForPurchase = packName;
        StickersPack.UserStatus packUserStatus = StorageManager.getInstance().getPackStatus(packName);
        if (packUserStatus != null && packUserStatus == StickersPack.UserStatus.ACTIVE) {
            Logger.w(TAG, "Trying to purchase already active pack");
        } else if ((packUserStatus != null && packUserStatus == StickersPack.UserStatus.HIDDEN)
                || "A".equals(pricePoint)) {
            purchasePack(packName, StickersPack.PurchaseType.FREE);
        } else if ("B".equals(pricePoint) && StorageManager.getInstance().isUserSubscriber()) {
            purchasePack(packName, StickersPack.PurchaseType.SUBSCRIPTION);
        } else {
            Prices prices = StickersManager.getPrices();
            if (prices != null) {
                if (mBillingProcessor != null
                        && !TextUtils.isEmpty(prices.getSkuB())
                        && "B".equals(pricePoint)) {
                    mBillingProcessor.purchase(this, prices.getSkuB());
                } else if (mBillingProcessor != null
                        && !TextUtils.isEmpty(prices.getSkuC())
                        && "C".equals(pricePoint)) {
                    mBillingProcessor.purchase(this, prices.getSkuC());
                } else if (prices.getPricePointB() != null
                        && "B".equals(pricePoint)) {
                    onPurchase(packTitle, packName, prices.getPricePointB());
                } else if (prices.getPricePointC() != null
                        && "C".equals(pricePoint)) {
                    onPurchase(packTitle, packName, prices.getPricePointC());
                } else {
                    executeJs(JS_METHOD_PURCHASE_FAIL);
                }
            } else {
                executeJs(JS_METHOD_PURCHASE_FAIL);
            }

        }
    }

    private void showCollections() {
        startActivity(new Intent(this, CollectionsActivity.class));
    }

    private void purchasePack(String packName, StickersPack.PurchaseType type) {
        TasksManager.getInstance().addPackPurchaseTask(packName, type, true);
    }

    public void onEventMainThread(TaskExecutedEvent event) {
        switch (event.getPendingTask().getCategory()) {
            case TasksManager.TASK_CATEGORY_PURCHASE_PACK:
                if (event.getPendingTask().getAction().equals(packForPurchase)) {
                    if (event.isSuccess()) {
                        executeJs(JS_METHOD_PURCHASE_SUCCESS);
                    } else {
                        executeJs(JS_METHOD_PURCHASE_FAIL);
                        showCantProcessToast();
                    }
                }
                break;
            default:
                // nothing to do
        }
    }

    private void showCantProcessToast() {
        Toast.makeText(this, R.string.sp_cant_process_request, Toast.LENGTH_SHORT).show();
    }

    private void executeJs(@JsMethod String method) {
        mWebView.loadUrl("javascript:window.JsInterface." + method);
    }

    private void setInProgress(boolean inProgress) {
        mProgressView.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    @Nullable
    public String getProductPrice(String sku) {
        if (TextUtils.isEmpty(sku) || mBillingProcessor == null) {
            return null;
        } else {
            SkuDetails productInfo = mBillingProcessor.getPurchaseListingDetails(sku);
            if (productInfo != null) {
                return productInfo.priceText;
            } else {
                return null;
            }
        }
    }

    // Override by client
    protected void onPurchase(String packTitle, String packName, PricePoint pricePoint) {
        executeJs(JS_METHOD_PURCHASE_FAIL);
    }

    // call from the client when fail to complete purchase
    protected void onPurchaseFail() {
        executeJs(JS_METHOD_PURCHASE_FAIL);
    }

}
