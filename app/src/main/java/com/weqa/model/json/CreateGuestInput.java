package com.weqa.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 10/20/2017.
 */

public class CreateGuestInput {

    @SerializedName("OrgId")
    @Expose
    private Integer orgid;
    @SerializedName("FirstName")
    @Expose
    private String firstName;
    @SerializedName("LastName")
    @Expose
    private String lastName;
    @SerializedName("MobileNo")
    @Expose
    private String mobileNo;
    @SerializedName("UserUUID")
    @Expose
    private String userUUID;
    @SerializedName("AdminUUID")
    @Expose
    private String adminUUID;
    @SerializedName("EndDate")
    @Expose
    private String endDate;

    public Integer getOrgid() {
        return orgid;
    }

    public void setOrgid(Integer orgid) {
        this.orgid = orgid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getAdminUUID() {
        return adminUUID;
    }

    public void setAdminUUID(String adminUUID) {
        this.adminUUID = adminUUID;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
