package com.weqa.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 10/20/2017.
 */

public class GetGuestInput {

    @SerializedName("orgid")
    @Expose
    private Integer orgId;

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }
}
