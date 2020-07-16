package com.lifeshare.network;

public interface BaseRemoteCallback {
    /**
     * onUnauthorized will be called when token miss matches with server
     */
    void onUnauthorized(Throwable throwable);

    /**
     * onFailed will be called when error generated from server
     *
     * @param throwable message value will be dependend on servers error message
     *                  if message is not available from server than default error message will
     *                  be displayed.
     */
    void onFailed(Throwable throwable);

    /**
     * onInternetFailed() method will be called when
     * network connection is not available in device.
     */
    void onInternetFailed();

    /**
     * onEmptyResponse() method will be called when response from server is blank or
     * error code is 404 generated.
     *
     * @param message
     */
    void onEmptyResponse(String message);
}
