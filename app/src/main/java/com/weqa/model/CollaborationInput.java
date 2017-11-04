package com.weqa.model;

/**
 * Created by Manish Ballav on 9/20/2017.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CollaborationInput {

    @SerializedName("Uuid")
    @Expose
    private String uuid;
    @SerializedName("OrgId")
    @Expose
    private Integer orgId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }
}
