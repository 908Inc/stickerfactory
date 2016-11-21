package vc908.stickerfactory.model.response;

import com.google.gson.annotations.Expose;

/**
 * Network response POJO model
 *
 * @author Dmitry Nezhydenko
 */
public class NetworkResponseModel<T> {

    @Expose
    private String message;
    @Expose
    protected T data;

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "NetworkResponseModel{" +
                "message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
