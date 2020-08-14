package com.lifeshare.network;

import com.google.gson.JsonElement;
import com.lifeshare.network.request.AcceptInvitation;
import com.lifeshare.network.request.BlockUnblockRequest;
import com.lifeshare.network.request.ChangePasswordRequest;
import com.lifeshare.network.request.CityRequest;
import com.lifeshare.network.request.DeleteConnectionRequest;
import com.lifeshare.network.request.DeleteStreamingRequest;
import com.lifeshare.network.request.DeleteUserRequest;
import com.lifeshare.network.request.ForgotPasswordRequest;
import com.lifeshare.network.request.InvitationRequest;
import com.lifeshare.network.request.LoginRequest;
import com.lifeshare.network.request.RejectInvitationRequest;
import com.lifeshare.network.request.ReportUserRequest;
import com.lifeshare.network.request.SearchUserRequest;
import com.lifeshare.network.request.StateRequest;
import com.lifeshare.network.request.UpdateDeviceTokenRequest;
import com.lifeshare.network.request.UpdatePushNotificationRequest;
import com.lifeshare.network.request.UserProfileRequest;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.network.response.CityResponse;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.CountryResponse;
import com.lifeshare.network.response.CreateSessionResponse;
import com.lifeshare.network.response.InvitationListResponse;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.network.response.ReportDetailListResponse;
import com.lifeshare.network.response.ReportListResponse;
import com.lifeshare.network.response.SearchUserResponse;
import com.lifeshare.network.response.StateResponse;
import com.lifeshare.network.response.StreamUserResponse;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

/**
 * Created by chirag.patel on 17/12/18.
 */

public interface WebAPIService {

    @POST("login")
    Call<LoginResponse> checkLogin(@Body LoginRequest request);

    @Multipart
    @POST("signup")
    Call<LoginResponse> signUp(@PartMap Map<String, RequestBody> bodyMap,
                               @Part MultipartBody.Part profile_photo);

    @Multipart
    @POST("user/profile")
    Call<LoginResponse> updateProfile(@PartMap Map<String, RequestBody> bodyMap,
                                      @Part MultipartBody.Part profile_photo);

    @POST("forgot-password")
    Call<CommonResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("user/change-password")
    Call<CommonResponse> changePassword(@Body ChangePasswordRequest request);

    @POST("logout")
    Call<CommonResponse> logout();

    @POST("user/connection/list")
    Call<ArrayList<MyConnectionListResponse>> getMyConnectionList();

    @POST("user/connection/invitation-list")
    Call<ArrayList<InvitationListResponse>> getInvitationList();

    @POST("user/device-token/update")
    Call<JsonElement> updateDeviceToken(@Body UpdateDeviceTokenRequest request);

    @POST("")
    Call<JsonElement> updatePushNotificationStatus(@Body UpdatePushNotificationRequest request);

    @POST("")
    Call<JsonElement> getAllRemainingPushNotification();

    @POST("user/list")
    Call<ArrayList<SearchUserResponse>> searchUser(@Body SearchUserRequest request);

    @POST("countries")
    Call<ArrayList<CountryResponse>> getCountry();

    @POST("states")
    Call<ArrayList<StateResponse>> getStateList(@Body StateRequest request);

    @POST("cities")
    Call<ArrayList<CityResponse>> getCityList(@Body CityRequest request);

    @POST("user/connection/invite")
    Call<CommonResponse> sendInvitaion(@Body InvitationRequest request);

    @POST("user/block")
    Call<CommonResponse> blockUser(@Body BlockUnblockRequest request);

    @POST("user/unblock")
    Call<CommonResponse> unblockUser(@Body BlockUnblockRequest request);

    @POST("user/connection/accept")
    Call<CommonResponse> acceptInvitaion(@Body AcceptInvitation request);

    @POST("user/connection/delete")
    Call<CommonResponse> deleteMyConnection(@Body DeleteConnectionRequest request);

    @POST("user/connection/reject")
    Call<CommonResponse> rejectInvitation(@Body RejectInvitationRequest request);

    @POST("opentok/create")
    Call<CreateSessionResponse> createSession();

    @POST("opentok/list")
    Call<ArrayList<StreamUserResponse>> getCurrentConnectionStreaming();

    @POST("user/show")
    Call<LoginResponse> getUserProfile(@Body UserProfileRequest request);

    @POST("opentok/delete")
    Call<CommonResponse> deleteStreaming(@Body DeleteStreamingRequest request);

    @POST("opentok/report")
    Call<CommonResponse> submitReportUser(@Body ReportUserRequest request);

    @POST("opentok/report/users")
    Call<ArrayList<ReportListResponse>> getReportedUserList();

    @POST("opentok/report/users/list")
    Call<ArrayList<ReportDetailListResponse>> getAllReportForUser(@Body DeleteUserRequest request);

    @POST("user/delete")
    Call<CommonResponse> deleteUser(@Body DeleteUserRequest request);

    @FormUrlEncoded
    @POST("opentok/send/notification")
    Call<CommonResponse> notifyOther(@Field("id") String id);

    @POST("channel/list")
    Call<ArrayList<ChannelArchiveResponse>> listChannelArchive();

    @Multipart
    @POST("channel/create")
    Call<CommonResponse> createChannelArchive(@PartMap Map<String, RequestBody> bodyMap, @Part MultipartBody.Part image);


    @POST("channel/delete")
    Call<CommonResponse> deleteChannelArchive(@Field("id") Integer id);

}
