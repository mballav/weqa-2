package com.weqa.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 11/5/2017.
 */

public class BookingReleaseInputV2 {

    @SerializedName("ActionCode")
    @Expose
    private String actionCode;
    @SerializedName("Uuid")
    @Expose
    private String uuid;
    @SerializedName("QrCode")
    @Expose
    private String qrCode;
    @SerializedName("PrivelageId")
    @Expose
    private Integer privilegeId;
    @SerializedName("OrgId")
    @Expose
    private Integer orgId;

    public Integer getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(Integer privilegeId) {
        this.privilegeId = privilegeId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

}
