package com.weqa.model;

/**
 * Created by Manish Ballav on 8/10/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Authentication {

    public static final String EMPLOYEE_NAME = "EN";
    public static final String EMPLOYEE_MOBILE = "EM";
    public static final String EMPLOYEE_EMAIL = "EE";
    public static final String ORG_INFO = "O";
    public static final String AUTH_TIME = "OT";
    public static final String ACTIVATION_CODES = "AC";

    @SerializedName("employeeName")
    @Expose
    private String employeeName;
    @SerializedName("org")
    @Expose
    private List<Org> org = null;
    @SerializedName("mobileNo")
    @Expose
    private String mobileNo;
    @SerializedName("emailId")
    @Expose
    private String emailId;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public List<Org> getOrganization() {
        return org;
    }

    public void setOrganization(List<Org> organization) {
        this.org = organization;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

}
