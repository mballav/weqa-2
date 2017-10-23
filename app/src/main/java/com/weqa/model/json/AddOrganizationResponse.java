package com.weqa.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 10/15/2017.
 */

public class AddOrganizationResponse {

    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("organizationId")
    @Expose
    private Integer organizationId;
    @SerializedName("organizationName")
    @Expose
    private String organizationName;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
