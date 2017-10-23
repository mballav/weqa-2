package com.weqa.service;

import com.weqa.model.AuthInput;
import com.weqa.model.AuthResponse;
import com.weqa.model.Availability;
import com.weqa.model.AvailabilityInput;
import com.weqa.model.BookingInput;
import com.weqa.model.BookingReleaseInput;
import com.weqa.model.BookingResponse;
import com.weqa.model.CollaborationInput;
import com.weqa.model.CollaborationResponse;
import com.weqa.model.FloorplanImage;
import com.weqa.model.FloorplanImageInput;
import com.weqa.model.FloorplanInput;
import com.weqa.model.FloorplanInputV2;
import com.weqa.model.FloorplanResponse;
import com.weqa.model.FloorplanResponseV2;
import com.weqa.model.json.ActivateOrgInput;
import com.weqa.model.json.ActivateOrgResponse;
import com.weqa.model.json.ActivateUserInput;
import com.weqa.model.json.ActivateUserResponse;
import com.weqa.model.json.AddOrganizationInput;
import com.weqa.model.json.AddTeamMemberInput;
import com.weqa.model.json.CreateTeamInput;
import com.weqa.model.json.DeleteTeamInput;
import com.weqa.model.json.DeleteTeamMemberInput;
import com.weqa.model.json.GetRegistrationInput;
import com.weqa.model.json.GetRegistrationResponse;
import com.weqa.model.json.RegistrationInput;
import com.weqa.model.json.ResendCodeOrgInput;
import com.weqa.model.json.TeamDetailInput;
import com.weqa.model.json.TeamDetailResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Manish Ballav on 10/12/2017.
 */

public interface AuthService {

    @POST("api/security/AddOrganization")
    Call<Void> addOrganization(@Body AddOrganizationInput input);

    @POST("api/security/ActivateOrganization")
    Call<ActivateOrgResponse> activateOrganization(@Body ActivateOrgInput input);

    @POST("api/security/ActivateUser")
    Call<ActivateUserResponse> activateUser(@Body ActivateUserInput input);

    @POST("api/security/Register")
    Call<Void> register(@Body RegistrationInput input);

    @POST("api/security/GetRegistration")
    Call<GetRegistrationResponse> getRegistration(@Body GetRegistrationInput input);

    @POST("api/security/ResendCode")
    Call<Void> resendCode(@Body ResendCodeOrgInput input);

    @POST("api/security/DeactivateTeam")
    Call<Void> deleteTeam(@Body DeleteTeamInput input);

    @POST("api/security/DeactivateTeamMember")
    Call<Void> deleteTeamMember(@Body DeleteTeamMemberInput input);

    @POST("api/security/AuthenticateAuthorizeUser")
    Call<AuthResponse> auth(@Body AuthInput input);

    @POST("api/security/CollaborationDetail")
    Call<List<CollaborationResponse>> collaboration(@Body CollaborationInput input);

    @POST("api/security/GetTeamDetail")
    Call<List<TeamDetailResponse>> teamDetail(@Body TeamDetailInput input);

    @POST("api/security/AddMultipleTeamMember")
    Call<Void> addTeamMember(@Body AddTeamMemberInput input);

    @POST("api/security/CreateTeam")
    Call<Void> createTeam(@Body CreateTeamInput input);

    @POST("api/security/ItemType")
    Call<List<Availability>> availability(@Body AvailabilityInput input);

    @POST("api/security/GetFloorPlanImage")
    Call<FloorplanImage> floorplanImage(@Body FloorplanImageInput input);

    @POST("api/security/FloorInfo")
    Call<FloorplanResponse> floorplan(@Body FloorplanInput input);

    @POST("api/security/Item")
    Call<FloorplanResponseV2> floorplanV2(@Body FloorplanInputV2 input);

    @POST("api/security/BookRelease")
    Call<BookingResponse> book(@Body BookingInput input);

    @POST("api/security/BookRelease")
    Call<BookingResponse> bookRelease(@Body BookingReleaseInput input);

}
