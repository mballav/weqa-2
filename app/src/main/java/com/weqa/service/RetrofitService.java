package com.weqa.service;

import com.weqa.framework.MyCall;
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
import com.weqa.model.json.AddOrganizationResponse;
import com.weqa.model.json.AddTeamMemberInput;
import com.weqa.model.json.CreateGuestInput;
import com.weqa.model.json.CreateGuestResponse;
import com.weqa.model.json.CreateTeamInput;
import com.weqa.model.json.DeleteTeamInput;
import com.weqa.model.json.DeleteTeamMemberInput;
import com.weqa.model.json.ExistingRegistrationInput;
import com.weqa.model.json.ExistingRegistrationResponse;
import com.weqa.model.json.GetGuestInput;
import com.weqa.model.json.GetGuestResponse;
import com.weqa.model.json.GetRegistrationInput;
import com.weqa.model.json.GetRegistrationResponse;
import com.weqa.model.json.RegistrationInput;
import com.weqa.model.json.RegistrationResponse;
import com.weqa.model.json.ResendCodeOrgInput;
import com.weqa.model.json.ResendCodeUserInput;
import com.weqa.model.json.TeamDetailInput;
import com.weqa.model.json.TeamDetailResponse;
import com.weqa.model.json.UpdateGuestInput;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Manish Ballav on 8/5/2017.
 */

public interface RetrofitService {

    @POST("api/GuestAccess/CreateGuestUser")
    MyCall<CreateGuestResponse> createGuestUser(@Body CreateGuestInput input);

    @POST("api/GuestAccess/UpdateGuestUser")
    MyCall<Void> updateGuestUser(@Body UpdateGuestInput input);

    @POST("api/GuestAccess/GetGuest")
    MyCall<List<GetGuestResponse>> getGuests(@Body GetGuestInput input);

    @POST("api/security/AddOrganization")
    MyCall<AddOrganizationResponse> addOrganization(@Body AddOrganizationInput input);

    @POST("api/security/ActivateOrganization")
    MyCall<ActivateOrgResponse> activateOrganization(@Body ActivateOrgInput input);

    @POST("api/security/ActivateUser")
    MyCall<ActivateUserResponse> activateUser(@Body ActivateUserInput input);

    @POST("api/security/Register")
    MyCall<RegistrationResponse> register(@Body RegistrationInput input);

    @POST("api/security/ExistingUser")
    MyCall<ExistingRegistrationResponse> existingRegister(@Body ExistingRegistrationInput input);

    @POST("api/security/GetRegistration")
    MyCall<GetRegistrationResponse> getRegistration(@Body GetRegistrationInput input);

    @POST("api/security/ResendCodeUser")
    MyCall<Void> resendCodeUser(@Body ResendCodeUserInput input);

    @POST("api/security/ResendCodeOrganization")
    MyCall<Void> resendCodeOrg(@Body ResendCodeOrgInput input);

    @POST("api/security/DeactivateTeam")
    MyCall<Void> deleteTeam(@Body DeleteTeamInput input);

    @POST("api/security/DeactivateTeamMember")
    MyCall<Void> deleteTeamMember(@Body DeleteTeamMemberInput input);

    @POST("api/security/AuthenticateAuthorizeUser")
    MyCall<AuthResponse> auth(@Body AuthInput input);

    @POST("api/security/CollaborationDetail")
    MyCall<List<CollaborationResponse>> collaboration(@Body CollaborationInput input);

    @POST("api/security/GetTeamDetail")
    MyCall<List<TeamDetailResponse>> teamDetail(@Body TeamDetailInput input);

    @POST("api/security/AddMultipleTeamMember")
    MyCall<Void> addTeamMember(@Body AddTeamMemberInput input);

    @POST("api/security/CreateTeam")
    MyCall<Void> createTeam(@Body CreateTeamInput input);

    @POST("api/security/ItemType")
    MyCall<List<Availability>> availability(@Body AvailabilityInput input);

    @POST("api/security/GetFloorPlanImage")
    MyCall<FloorplanImage> floorplanImage(@Body FloorplanImageInput input);

    @POST("api/security/FloorInfo")
    MyCall<FloorplanResponse> floorplan(@Body FloorplanInput input);

    @POST("api/security/Item")
    MyCall<FloorplanResponseV2> floorplanV2(@Body FloorplanInputV2 input);

    @POST("api/security/BookRelease")
    MyCall<BookingResponse> book(@Body BookingInput input);

    @POST("api/security/BookRelease")
    MyCall<BookingResponse> bookRelease(@Body BookingReleaseInput input);
}
