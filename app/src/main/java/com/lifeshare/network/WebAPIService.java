package com.lifeshare.network;

import com.google.gson.JsonElement;
import com.lifeshare.model.ChatMessage;
import com.lifeshare.network.request.AcceptInvitation;
import com.lifeshare.network.request.BlockUnblockRequest;
import com.lifeshare.network.request.ChangePasswordRequest;
import com.lifeshare.network.request.ChatHistoryRequest;
import com.lifeshare.network.request.CheckSocialMediaRequest;
import com.lifeshare.network.request.CheckVersionRequest;
import com.lifeshare.network.request.CityRequest;
import com.lifeshare.network.request.ContactInvitationRequest;
import com.lifeshare.network.request.ContactInvitationViaMobileRequest;
import com.lifeshare.network.request.CreateRoomWithUserRequest;
import com.lifeshare.network.request.DeleteArchivesRequest;
import com.lifeshare.network.request.DeleteConnectionRequest;
import com.lifeshare.network.request.DeleteStreamingRequest;
import com.lifeshare.network.request.DeleteStreamingTwilioRequest;
import com.lifeshare.network.request.DeleteUserRequest;
import com.lifeshare.network.request.ForgotPasswordRequest;
import com.lifeshare.network.request.GetArchiveListRequest;
import com.lifeshare.network.request.InvitationRequest;
import com.lifeshare.network.request.LoginRequest;
import com.lifeshare.network.request.NewTwilioTokenRequest;
import com.lifeshare.network.request.RejectInvitationRequest;
import com.lifeshare.network.request.ReportUserRequest;
import com.lifeshare.network.request.SaveChatRequest;
import com.lifeshare.network.request.SaveSubscriptionRequest;
import com.lifeshare.network.request.SearchUserRequest;
import com.lifeshare.network.request.SendNotificationRequest;
import com.lifeshare.network.request.StateRequest;
import com.lifeshare.network.request.UpdateDeviceTokenRequest;
import com.lifeshare.network.request.UpdateSaveChatFlag;
import com.lifeshare.network.request.UpdateViewerCountRequest;
import com.lifeshare.network.request.UserProfileRequest;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.network.response.CheckSubscriptionResponse;
import com.lifeshare.network.response.CheckVersionResponse;
import com.lifeshare.network.response.CityResponse;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.CountryResponse;
import com.lifeshare.network.response.CreateRoomResponse;
import com.lifeshare.network.response.CreateSessionResponse;
import com.lifeshare.network.response.InvitationListResponse;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.network.response.NewTwilioTokenResponse;
import com.lifeshare.network.response.ReportDetailListResponse;
import com.lifeshare.network.response.ReportListResponse;
import com.lifeshare.network.response.SearchUserResponse;
import com.lifeshare.network.response.StateResponse;
import com.lifeshare.network.response.StreamUserListResponse;
import com.lifeshare.network.response.StreamUserResponse;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
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

    @POST("twilio/create")
    Call<CreateRoomResponse> createRoom(@Body CreateRoomWithUserRequest request);

    @POST("twilio/get-new-token")
    Call<NewTwilioTokenResponse> getNewTwilioToken(@Body NewTwilioTokenRequest request);

    @POST("opentok/list")
    Call<ArrayList<StreamUserResponse>> getCurrentConnectionStreaming();

    @POST("twilio/list")
    Call<ArrayList<StreamUserListResponse>> getCurrentConnectionStreamingListTwilio();

    @POST("user/show")
    Call<LoginResponse> getUserProfile(@Body UserProfileRequest request);

    @POST("twilio/delete")
    Call<CommonResponse> deleteStreamingTwilio(@Body DeleteStreamingTwilioRequest request);


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

    @POST("twilio/send/notification")
    Call<CommonResponse> notifyOther(@Body SendNotificationRequest request);

    @POST("user/update-viewer-count")
    Call<CommonResponse> updateViewerCount(@Body UpdateViewerCountRequest request);

    @POST("channel/list")
    Call<ArrayList<ChannelArchiveResponse>> listChannelArchive(@Body GetArchiveListRequest request);

    @POST("user/storeSubscription")
    Call<CommonResponse> saveSubscription(@Body SaveSubscriptionRequest request);

    @Multipart
    @POST("channel/create")
    Call<CommonResponse> createChannelArchive(@PartMap Map<String, RequestBody> bodyMap, @Part MultipartBody.Part image);

    @POST("channel/delete")
    Call<CommonResponse> deleteChannelArchive(@Body DeleteArchivesRequest request);

    @POST("chat/create")
    Call<CommonResponse> saveChatMessage(@Body SaveChatRequest request);

    @POST("twilio/update-chat-flag")
    Call<CommonResponse> updateSaveChatFlag(@Body UpdateSaveChatFlag request);

    @POST("chat/list")
    Call<ArrayList<ChatMessage>> getSaveChatHistory(@Body ChatHistoryRequest request);

    @POST("user/checkSubscription")
    Call<CheckSubscriptionResponse> checkSubscription();

    @POST("user/contactInvitation")
    Call<CommonResponse> contactInvitation(@Body ContactInvitationRequest request);

    @POST("check-version")
    Call<CheckVersionResponse> checkVersion(@Body CheckVersionRequest request);

    @POST("check-social-account")
    Call<LoginResponse> checkSocialMedia(@Body CheckSocialMediaRequest request);

    @POST("user/contactInvitationViaMobile")
    Call<CommonResponse> contactInvitationViaMobile(@Body ContactInvitationViaMobileRequest request);


}
