package com.lifeshare.network;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public abstract class RemoteCallback<T> implements Callback<T> {
    private static final String TAG = "RemoteCallback";
    // Default error message
    private static final String DEFAULT_ERROR_MSG = "Sorry we are unable to reach server at this time.";
    private BaseRemoteCallback baseRemoteCallback;

    public RemoteCallback(BaseRemoteCallback baseRemoteCallback) {
        this.baseRemoteCallback = baseRemoteCallback;
    }

    public RemoteCallback() {

    }

    /**
     * Overrides onReponse method and handles response of servers and reacts accordingly.
     *
     * @param call
     * @param response
     */
    @Override
    public final void onResponse(@NonNull Call<T> call, Response<T> response) {
        switch (response.code()) {
            case HttpsURLConnection.HTTP_OK:
            case HttpsURLConnection.HTTP_CREATED:
            case HttpsURLConnection.HTTP_ACCEPTED:
            case HttpsURLConnection.HTTP_NOT_AUTHORITATIVE:
                if (response.body() != null) {
                    onSuccess(response.body());
                } else {
                    onEmptyResponse(getErrorMessage(response));
                }
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_NO_CONTENT:
                onEmptyResponse("");
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                onUnauthorized(new Throwable(getErrorMessage(response)));
                break;
            default:
                onFailed(new Throwable(getErrorMessage(response)));
                break;
        }
    }

    private String getErrorMessage(Response<T> response) {
        if (response == null || response.headers() == null) {
            return DEFAULT_ERROR_MSG;
        }
        try {
            JSONObject jObjError = new JSONObject(response.errorBody().string());
            return jObjError.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";


    }

    /**
     * Overriding default onFailure method
     * this method will trigger onInternetFailed()
     *
     * @param call
     * @param t
     */
    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        if (t instanceof NoConnectivityException) {
            //Add your code for displaying no network connection error
            onInternetFailed();
        } else {
            onFailed(new Throwable(DEFAULT_ERROR_MSG));
        }

    }

    /**
     * onSuccess will be called when response contains body
     *
     * @param response
     */
    public abstract void onSuccess(T response);

    /**
     * onUnauthorized will be called when token miss matches with server
     */
    public void onUnauthorized(Throwable throwable) {
        baseRemoteCallback.onUnauthorized(throwable);
    }

    /**
     * onFailed will be called when error generated from server
     *
     * @param throwable message value will be dependend on servers error message
     *                  if message is not available from server than default error message will
     *                  be displayed.
     */
    public void onFailed(Throwable throwable) {
        baseRemoteCallback.onFailed(throwable);
    }

    /**
     * onInternetFailed() method will be called when
     * network connection is not available in device.
     */
    public void onInternetFailed() {
        baseRemoteCallback.onInternetFailed();
    }

    /**
     * onEmptyResponse() method will be called when response from server is blank or
     * error code is 404 generated.
     *
     * @param message
     */
    public void onEmptyResponse(String message) {
        baseRemoteCallback.onEmptyResponse(message);
    }

}
