package vc908.stickerfactory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vc908.stickerfactory.events.ShopContentLastModifiedUpdatedEvent;
import vc908.stickerfactory.interceptors.NetworkHeaderInterceptor;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.model.response.ContentResponse;
import vc908.stickerfactory.model.response.NetworkResponseModel;
import vc908.stickerfactory.model.response.PackInfoResponse;
import vc908.stickerfactory.model.response.SearchResponse;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

/**
 * Class contains all network logic
 *
 * @author Dmitry Nezhydenko
 */
public final class NetworkManager {

    public static String BASE_URL_API = "https://api.stickerpipe.com";

    private static final String API_PATH = "/api/v2/";
    public static final String API_URL = BASE_URL_API + API_PATH;

    private static final String TAG = NetworkManager.class.getSimpleName();
    private static NetworkManager instance;
    private final Context mContext;
    private final NetworkService mNetworkService;
    private DevNetworkService mDevNetworkService;
    private final NetworkHeaderInterceptor headersInterceptor;
    private final OkHttpClient mOkHttpClient;
    private Gson gson;
    private boolean isUpdateRequestInProcess;

    public static Interceptor customInterceptor = null;

    /**
     * Private constructor to prevent creating new objects.
     *
     * @param context manager context
     * @param apiKey  requests api key
     */
    private NetworkManager(Context context, String apiKey) {
        mContext = context;
        OkHttpClient.Builder mOkHttpClientBuilder = new OkHttpClient.Builder();
        headersInterceptor = new NetworkHeaderInterceptor(
                apiKey,
                Utils.getDeviceId(context),
                context.getPackageName(),
                Utils.getDensityName(context),
                BuildConfig.VERSION_NAME);
        mOkHttpClientBuilder.networkInterceptors().add(headersInterceptor);
        mOkHttpClientBuilder.connectTimeout(60, TimeUnit.SECONDS);
        mOkHttpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
        mOkHttpClientBuilder.writeTimeout(60, TimeUnit.SECONDS);
        HttpLoggingInterceptor httpLoginInterceptors = new HttpLoggingInterceptor();
        httpLoginInterceptors.setLevel(HttpLoggingInterceptor.Level.BODY);
        mOkHttpClientBuilder.networkInterceptors().add(httpLoginInterceptors);

        if (customInterceptor != null) {
            mOkHttpClientBuilder.networkInterceptors().add(customInterceptor);
        }
        // cache
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        File cacheDirectory = new File(context.getCacheDir().getAbsolutePath(), "HttpCache");
        Cache cache = new Cache(cacheDirectory, cacheSize);
        mOkHttpClientBuilder.cache(cache);

        mOkHttpClient = mOkHttpClientBuilder.build();
        initGson();
        mNetworkService = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(API_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(NetworkService.class);
    }

    /**
     * Return singleton manager instance.
     *
     * @return manager instance
     * @throws RuntimeException Throws when try to get instance when Stickers manager not initialized
     */
    public static synchronized NetworkManager getInstance() throws RuntimeException {
        if (instance == null) {
            if (StickersManager.getApplicationContext() == null) {
                throw new RuntimeException("Stickers manager not initialized.");
            }
            if (TextUtils.isEmpty(StickersManager.getApiKey())) {
                throw new RuntimeException("Api key is empty");
            }
            instance = new NetworkManager(StickersManager.getApplicationContext(), StickersManager.getApiKey());
        }
        return instance;
    }

    /**
     * Initialize {@link Gson} parser
     */
    private void initGson() {
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    /**
     * Check last modified date of client packs
     */
    public void checkPackUpdates() {
        checkPackUpdates(false);
    }

    public void checkPackUpdates(boolean forceUpdate) {
        if (Utils.isNetworkAvailable(mContext)
                && (!isUpdateRequestInProcess || forceUpdate)
                && !TextUtils.isEmpty(StorageManager.getInstance().getUserID())) {
            isUpdateRequestInProcess = true;
            mNetworkService.getUserStickersList(StorageManager.getInstance().isUserSubscriber() ? 1 : 0)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.getData() != null) {
                                    StorageManager.getInstance().updatePacksInfo(response.getData());
                                } else {
                                    Logger.w(TAG, "Data is null for user packs");
                                }
                                if (response.getMetaInfo() != null) {
                                    long contentLastModified = response.getMetaInfo().getShopContentLastModified();
                                    if (contentLastModified > StorageManager.getInstance().getShopContentLastModified()) {
                                        StorageManager.getInstance().storeShopContentLastModified(contentLastModified);
                                        EventBus.getDefault().post(new ShopContentLastModifiedUpdatedEvent());
                                    }
                                }
                            },
                            this::onNetworkResponseFail,
                            () -> isUpdateRequestInProcess = false
                    );
        }

    }

    public void updateStamps() {
//        if (Utils.isNetworkAvailable(mContext)) {
//            mNetworkService.getStamps()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            response -> {
//                                if (response.getData() != null) {
//                                    Map<String, Filter> filters = StorageManager.getInstance().getFilters();
//                                    for (StickersPack pack : response.getData()) {
//                                        TasksManager.getInstance().addPackPurchaseTask(pack.getName(), StickersPack.PurchaseType.FREE, false);
//                                        filters.remove(pack.getName());
//                                    }
//                                    StorageManager.getInstance().removeFilters(filters.keySet());
//                                } else {
//                                    Logger.w(TAG, "Data is null for user packs");
//                                }
//                            },
//                            this::onNetworkResponseFail
//                    );
//        }
    }

    public Observable<NetworkResponseModel> requestSendToken(String token, String type) {
        return mNetworkService.sendToken(token, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * Send analytics data to server side
     *
     * @param data Data in JSON format
     */
    public void sendAnalyticsData(JSONArray data) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), data.toString());
        mNetworkService.sendAnalytics(requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        networkResponseModel -> StorageManager.getInstance().clearAnalytics(),
                        this::onNetworkResponseFail);
    }

    public Observable<PackInfoResponse> requestPackPurchase(String packName, StickersPack.PurchaseType purchaseType) {
        return mNetworkService.purchasePack(packName, purchaseType.getValue())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SearchResponse> requestSearch(String query, boolean topForEmpty, boolean wholeWordSearch) {
        return mNetworkService.getSearchResults(query, Constants.SEARCH_STICKERS_LIMIT, topForEmpty ? 1 : 0, wholeWordSearch ? 1 : 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SearchResponse> requestStampsSearch(String query) {
        return requestStampsSearch(query, Constants.SEARCH_STICKERS_LIMIT);
    }

    public Observable<SearchResponse> requestStampsSearch(String query, int limit) {
        return mNetworkService.getStampsSearchResults(query, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * Request hide pack
     *
     * @param packName Pack name
     * @return Hide pack request observable
     */
    public Observable<NetworkResponseModel> requestHidePack(String packName) {
        return mNetworkService.hidePack(packName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Request content data
     *
     * @param contentId Content id
     * @return Content response observable
     */
    public Observable<ContentResponse> requestContent(String contentId) {
        return mNetworkService.getContentById(contentId)
                .subscribeOn(Schedulers.io());
    }


    /**
     * Send user related data to serverside
     */
    public Observable<NetworkResponseModel> requestSendUserData(@NonNull Map<String, String> data) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(data));
        return mNetworkService.sendUserData(requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Handle network response error
     *
     * @param throwable Network response error
     */
    private void onNetworkResponseFail(Throwable throwable) {
        Logger.e(TAG, throwable);
    }

    /**
     * Get network interceptor with custom headers
     *
     * @return Interceptor
     */
    public NetworkHeaderInterceptor getHeaderInterceptor() {
        return headersInterceptor;
    }

    /**
     * Get file from network
     *
     * @param url Image url
     * @throws IOException
     */
    public byte[] getFile(@NonNull String url) throws IOException {
        Request.Builder requestBuilder = new Request.Builder();
        for (Map.Entry<String, String> entry : getHeaderInterceptor().getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        Response response = mOkHttpClient.newCall(requestBuilder.url(url).build()).execute();
        return response.body().bytes();
    }

    /**
     * Download tab image for given pack
     *
     * @param url Image url
     * @param key Key for caching
     */
    public Observable<Boolean> downloadImage(@NonNull String url, String key) {
        return Observable.<Boolean>create(subscriber -> {
            try {
                StorageManager.getInstance().storeFile(getFile(url), key);
                subscriber.onNext(true);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * Request content by id and cache file
     *
     * @param contentId Content ID
     * @return Observable for cached file
     */
    public Observable<File> downloadSticker(@NonNull String contentId) {
        return Observable.<File>create(subscriber -> {
            if (TextUtils.isEmpty(contentId)) {
                throw new RuntimeException("Content id may not be empty");
            } else {
                requestContent(contentId).subscribe(
                        response -> {
                            if (response.getData() != null
                                    && response.getData().getImageLinks() != null
                                    && response.getData().getImageLinks().size() > 0) {
                                String imageLink = response.getData().getImageLinks().get(Utils.getDensityName(mContext));
                                if (TextUtils.isEmpty(imageLink)) {
                                    subscriber.onError(new RuntimeException("Image link is empty: " + response.getData()));
                                } else {
                                    try {
                                        String responseContentId = response.getData().getContentId();
                                        if (TextUtils.isEmpty(responseContentId)) {
                                            subscriber.onError(new RuntimeException("Content id from response is empty: " + response.toString()));
                                        } else {
                                            if (StorageManager.getInstance().cacheImage(responseContentId, imageLink)) {
                                                StorageManager.getInstance().storeContentPackName(responseContentId, response.getData().getPack());
                                                subscriber.onNext(StorageManager.getInstance().getImageFile(responseContentId));
                                                subscriber.onCompleted();
                                            } else {
                                                subscriber.onError(new RuntimeException("Can't cache image: " + response.toString()));
                                            }
                                        }
                                    } catch (IOException e) {
                                        subscriber.onError(e);
                                    }
                                }
                            } else {
                                subscriber.onError(new RuntimeException("Bad response from server when try to get content by id: " + response.toString()));
                            }
                        },
                        subscriber::onError
                );
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Send developer info to serverside
     *
     * @param category Info category
     * @param param1   First param
     * @param param2   Second param
     * @return Request observable
     */
    public Observable<ResponseBody> sendDeveloperReport(@TasksManager.DevReportCategory String category, String param1, String param2) {
        return getDevNetworkService().sendDevReport(category, param1, param2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Create if need and return dev network service
     *
     * @return Network service for developers requests
     */
    private DevNetworkService getDevNetworkService() {
        if (mDevNetworkService == null) {
            mDevNetworkService = new Retrofit.Builder()
                    .client(mOkHttpClient)
                    .baseUrl(BASE_URL_API)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()
                    .create(DevNetworkService.class);
        }
        return mDevNetworkService;
    }

}
