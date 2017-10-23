package com.weqa.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 9/27/2017.
 */

public class DeleteTeamMemberInput {

    @SerializedName("OrgId")
    @Expose
    private Integer orgId;
    @SerializedName("TeamId")
    @Expose
    private Integer teamId;
    @SerializedName("Uuid")
    @Expose
    private String uuid;

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
