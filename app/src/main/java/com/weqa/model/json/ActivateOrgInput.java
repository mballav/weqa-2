package com.weqa.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish Ballav on 10/11/2017.
 */

public class ActivateOrgInput {

    @SerializedName("UUID")
    @Expose
    private String uuid;
    @SerializedName("ActivationCode")
    @Expose
    private String activationCode = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}
