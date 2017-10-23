package com.weqa.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 10/21/2017.
 */

public class UpdateGuestInput {

    @SerializedName("OrgUserId")
    @Expose
    private int orgUserId;
    @SerializedName("EndDate")
    @Expose
    private String endDate;

    public int getOrgUserId() {
        return orgUserId;
    }

    public void setOrgUserId(int orgUserId) {
        this.orgUserId = orgUserId;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
