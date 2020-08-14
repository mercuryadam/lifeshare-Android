package com.lifeshare.network;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.lifeshare.model.ChannelArchive;
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
import com.lifeshare.network.request.SignUpRequest;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class WebAPIManager {

    private static WebAPIManager INSTANCE;

    private final WebAPIService mService;

    private WebAPIManager() {
        mService = WebAPIServiceFactory.newInstance().makeServiceFactory();
    }

    public static WebAPIManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WebAPIManager();
        }
        return INSTANCE;
    }

    public void checkLogin(LoginRequest request, RemoteCallback<LoginResponse> callback) {
        mService.checkLogin(request).enqueue(callback);
    }

    public void signUp(SignUpRequest request, RemoteCallback<LoginResponse> callback) {

        HashMap<String, RequestBody> bodyMap = new HashMap<>();
        bodyMap.put("firstName", getResponseBody(request.getFirstName()));
        bodyMap.put("lastName", getResponseBody(request.getLastName()));
        bodyMap.put("username", getResponseBody(request.getUsername()));
        bodyMap.put("email", getResponseBody(request.getEmail()));
        bodyMap.put("password", getResponseBody(request.getPassword()));
        bodyMap.put("description", getResponseBody(request.getDescription()));
        bodyMap.put("confirmPassword", getResponseBody(request.getConfirmPassword()));
        bodyMap.put("city", getResponseBody(request.getCity()));
        bodyMap.put("state", getResponseBody(request.getState()));
        bodyMap.put("country", getResponseBody(request.getCountry()));
        bodyMap.put("mobile", getResponseBody(request.getMobile()));
        bodyMap.put("deviceId", getResponseBody(request.getDeviceId()));
        bodyMap.put("deviceToken", getResponseBody(request.getDeviceToken()));
        MultipartBody.Part bodyImage = prepareBody("avatar", request.getAvatar());

        mService.signUp(bodyMap, bodyImage).enqueue(callback);
    }

    public void updateProfile(SignUpRequest request, RemoteCallback<LoginResponse> callback) {

        HashMap<String, RequestBody> bodyMap = new HashMap<>();
        bodyMap.put("first_name", getResponseBody(request.getFirstName()));
        bodyMap.put("last_name", getResponseBody(request.getLastName()));
        bodyMap.put("email", getResponseBody(request.getEmail()));
        bodyMap.put("description", getResponseBody(request.getDescription()));
        bodyMap.put("city", getResponseBody(request.getCity()));
        bodyMap.put("state", getResponseBody(request.getState()));
        bodyMap.put("country", getResponseBody(request.getCountry()));
        bodyMap.put("mobile", getResponseBody(request.getMobile()));

        MultipartBody.Part bodyImage = prepareBody("avatar", request.getAvatar());

        mService.updateProfile(bodyMap, bodyImage).enqueue(callback);
    }

    public void forgotPassword(ForgotPasswordRequest request, RemoteCallback<CommonResponse> callback) {
        mService.forgotPassword(request).enqueue(callback);
    }

    public void changePassword(ChangePasswordRequest request, RemoteCallback<CommonResponse> callback) {
        mService.changePassword(request).enqueue(callback);
    }

    public void logout(RemoteCallback<CommonResponse> callback) {
        mService.logout().enqueue(callback);
    }

    public void getInvitationList(RemoteCallback<ArrayList<InvitationListResponse>> callback) {
        mService.getInvitationList().enqueue(callback);
    }

    public void getMyConnectionList(RemoteCallback<ArrayList<MyConnectionListResponse>> callback) {
        mService.getMyConnectionList().enqueue(callback);
    }

    public void searchUser(SearchUserRequest request, RemoteCallback<ArrayList<SearchUserResponse>> callback) {
        mService.searchUser(request).enqueue(callback);
    }

    public void updateDeviceToken(UpdateDeviceTokenRequest request, RemoteCallback<JsonElement> callback) {
        mService.updateDeviceToken(request).enqueue(callback);
    }

    public void updatePushNotificationStatus(UpdatePushNotificationRequest request, RemoteCallback<JsonElement> callback) {
        mService.updatePushNotificationStatus(request).enqueue(callback);
    }

    public void getAllRemainingPushNotification(RemoteCallback<JsonElement> callback) {
        mService.getAllRemainingPushNotification().enqueue(callback);
    }

    public void sendInvitaion(InvitationRequest request, RemoteCallback<CommonResponse> callback) {
        mService.sendInvitaion(request).enqueue(callback);
    }

    public void getCountry(RemoteCallback<ArrayList<CountryResponse>> callback) {
        mService.getCountry().enqueue(callback);
    }

    public void getStateList(StateRequest request, RemoteCallback<ArrayList<StateResponse>> callback) {
        mService.getStateList(request).enqueue(callback);
    }

    public void getCityList(CityRequest request, RemoteCallback<ArrayList<CityResponse>> callback) {
        mService.getCityList(request).enqueue(callback);
    }

    public void acceptInvitaion(AcceptInvitation request, RemoteCallback<CommonResponse> callback) {
        mService.acceptInvitaion(request).enqueue(callback);
    }

    public void blockUser(BlockUnblockRequest request, RemoteCallback<CommonResponse> callback) {
        mService.blockUser(request).enqueue(callback);
    }

    public void unblockUser(BlockUnblockRequest request, RemoteCallback<CommonResponse> callback) {
        mService.unblockUser(request).enqueue(callback);
    }

    public void deleteMyConnection(DeleteConnectionRequest request, RemoteCallback<CommonResponse> callback) {
        mService.deleteMyConnection(request).enqueue(callback);
    }

    public void rejectInvitation(RejectInvitationRequest request, RemoteCallback<CommonResponse> callback) {
        mService.rejectInvitation(request).enqueue(callback);
    }

    public void createSession(RemoteCallback<CreateSessionResponse> callback) {
        mService.createSession().enqueue(callback);
    }

    public void getCurrentConnectionStreaming(RemoteCallback<ArrayList<StreamUserResponse>> callback) {
        mService.getCurrentConnectionStreaming().enqueue(callback);
    }

    public void deleteStreaming(DeleteStreamingRequest request, RemoteCallback<CommonResponse> callback) {
        mService.deleteStreaming(request).enqueue(callback);
    }

    public void submitReportUser(ReportUserRequest request, RemoteCallback<CommonResponse> callback) {
        mService.submitReportUser(request).enqueue(callback);
    }

    public void deleteUser(DeleteUserRequest request, RemoteCallback<CommonResponse> callback) {
        mService.deleteUser(request).enqueue(callback);
    }

    public void notifyOther(String id,RemoteCallback<CommonResponse> callback) {
        mService.notifyOther(id).enqueue(callback);
    }

    public void listChannelArchive(RemoteCallback<ArrayList<ChannelArchiveResponse>> callback) {
        mService.listChannelArchive().enqueue(callback);
    }

    public void createChannelArchive(ChannelArchive request, RemoteCallback<CommonResponse> callback) {
        HashMap<String, RequestBody> bodyMap = new HashMap<>();
        bodyMap.put("link", getResponseBody(request.getLink()));
        bodyMap.put("title", getResponseBody(request.getTitle()));
        MultipartBody.Part bodyImage = prepareBody("image", request.getPicture());

        mService.createChannelArchive(bodyMap, bodyImage).enqueue(callback);
    }

    public void deleteChannelArchive(Integer id, RemoteCallback<CommonResponse> callback) {
        mService.deleteChannelArchive(id).enqueue(callback);
    }


    public void getAllReportForUser(DeleteUserRequest request, RemoteCallback<ArrayList<ReportDetailListResponse>> callback) {
        mService.getAllReportForUser(request).enqueue(callback);
    }

    public void getReportedUserList(RemoteCallback<ArrayList<ReportListResponse>> callback) {
        mService.getReportedUserList().enqueue(callback);
    }

    public void getUserProfile(UserProfileRequest request, RemoteCallback<LoginResponse> callback) {
        mService.getUserProfile(request).enqueue(callback);
    }


    private RequestBody getResponseBody(String string) {
        if (TextUtils.isEmpty(string)) {
            string = "";
        }
        return RequestBody.create(MediaType.parse("text/plain"), string);
    }

    public MultipartBody.Part prepareBody(String key, String uri) {
        MultipartBody.Part body = null;
        if (!TextUtils.isEmpty(uri) && !uri.startsWith("http")) {
            File file = new File(String.valueOf(uri));
            final RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            body = MultipartBody.Part.createFormData(key, file.getName(), reqFile);
        }
        return body;
    }
}
