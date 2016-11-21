package vc908.stickerfactory;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public interface DevNetworkService {

    @GET("logs/{category}/{param1}/{param2}")
    Observable<ResponseBody> sendDevReport(@Path("category") @TasksManager.DevReportCategory String category, @Path("param1") String param1, @Path("param2") String param2);

}
