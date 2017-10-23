package com.weqa.model.adapterdata;

import java.util.Date;

/**
 * Created by Manish Ballav on 10/15/2017.
 */

public class OrgListItem {

    private String orgName;

    private String privilege;

    private Date endDate;

    private boolean defaultOrg;

    private int organizationId;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isDefaultOrg() {
        return defaultOrg;
    }

    public void setDefaultOrg(boolean defaultOrg) {
        this.defaultOrg = defaultOrg;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
}
