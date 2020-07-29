package com.lifeshare.network;

import androidx.annotation.NonNull;

import com.lifeshare.BuildConfig;
import com.lifeshare.LifeShare;
import com.lifeshare.utils.PreferenceHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebAPIServiceFactory {
    private static final int HTTP_READ_TIMEOUT = 10000;
    private static final int HTTP_CONNECT_TIMEOUT = 6000;
    private static WebAPIServiceFactory INSTANCE;

    public static WebAPIServiceFactory newInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WebAPIServiceFactory();
        }
        return INSTANCE;
    }

    public WebAPIService makeServiceFactory() {
        return makeServiceFactory(makeOkHttpClient());
    }

    private WebAPIService makeServiceFactory(OkHttpClient okHttpClient) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return retrofit.create(WebAPIService.class);
    }

    private OkHttpClient makeOkHttpClient() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient().newBuilder();
        httpClientBuilder.connectTimeout(HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        httpClientBuilder.readTimeout(HTTP_READ_TIMEOUT, TimeUnit.SECONDS);
        /*if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release")) {
            httpClientBuilder.addInterceptor(new ChuckInterceptor(LifeShare.getInstance()));
        }*/
        httpClientBuilder.interceptors().add(new Interceptor() {
                                                 @Override
                                                 public Response intercept(@NonNull Chain chain) throws IOException {
                                                     Request original = chain.request();
// Customize the request
                                                     Request.Builder requestBuilder = original.newBuilder();

                                                     requestBuilder.addHeader("content-Type", "application/json");
                                                     requestBuilder.method(original.method(), original.body());

                                                     if (PreferenceHelper.getInstance().getUser() != null) {
                                                         requestBuilder.addHeader("Authorization", "Bearer " + PreferenceHelper.getInstance().getUser().getToken());
                                                     }

                                                     Response response = chain.proceed(requestBuilder.build());

// Customize or return the response
                                                     return response;
                                                 }
                                             }
        );
        httpClientBuilder.addInterceptor(loggingInterceptor());
        httpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request request = chain.request();
                Response response = chain.proceed(request);
                ResponseBody responseBody = response.body();
                String rawJson = responseBody.string();
                try {
                    if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        if (PreferenceHelper.getInstance().getUser() != null) {
                            LifeShare.getInstance().logout();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response.newBuilder()
                        .body(ResponseBody.create(responseBody.contentType(), rawJson)).build();

            }
        });
        return httpClientBuilder.build();
    }

    private HttpLoggingInterceptor loggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        return logging;
    }
}
