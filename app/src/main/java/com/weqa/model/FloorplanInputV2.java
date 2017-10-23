package com.weqa.model;

/**
 * Created by Manish Ballav on 8/31/2017.
 */

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FloorplanInputV2 {

    @SerializedName("BuildingId")
    @Expose
    private String buildingId;

    @SerializedName("OrgId")
    @Expose
    private int organizationId;
    @SerializedName("PrivelageId")
    @Expose
    private int privilegeId;

    @SerializedName("Uuid")
    @Expose
    private String uuid;
    @SerializedName("ActionCodeItemType")
    @Expose
    private String actionCodeItemType;
    @SerializedName("ActionCodeFloorPlan")
    @Expose
    private String actionCodeFloorPlan;
    @SerializedName("FloorPlan")
    @Expose
    private List<FloorPlan> floorPlan = null;

    public int getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(int privilegeId) {
        this.privilegeId = privilegeId;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getActionCodeItemType() {
        return actionCodeItemType;
    }

    public void setActionCodeItemType(String actionCodeItemType) {
        this.actionCodeItemType = actionCodeItemType;
    }

    public String getActionCodeFloorPlan() {
        return actionCodeFloorPlan;
    }

    public void setActionCodeFloorPlan(String actionCodeFloorPlan) {
        this.actionCodeFloorPlan = actionCodeFloorPlan;
    }

    public List<FloorPlan> getFloorPlan() {
        return floorPlan;
    }

    public void setFloorPlan(List<FloorPlan> floorPlan) {
        this.floorPlan = floorPlan;
    }

}
