package com.weqa.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 10/11/2017.
 */

public class ActivateOrgResponse {

    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("organizationName")
    @Expose
    private String organizationName;
    @SerializedName("organizationId")
    @Expose
    private Integer organizationId;

    @SerializedName("privelageName")
    @Expose
    private String privilegeName;
    @SerializedName("privelageId")
    @Expose
    private Integer privilegeId;
    @SerializedName("enddate")
    @Expose
    private String endDate;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getPrivilegeName() {
        return privilegeName;
    }

    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

    public Integer getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(Integer privilegeId) {
        this.privilegeId = privilegeId;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
