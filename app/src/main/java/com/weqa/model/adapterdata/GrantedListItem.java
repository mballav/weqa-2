package com.weqa.model.adapterdata;

import java.util.Date;

/**
 * Created by Manish Ballav on 10/13/2017.
 */

public class GrantedListItem {

    private String uuid;

    private String firstName;

    private String lastName;

    private String mobile;

    private Date endDate;

    private int orgUserId;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getOrgUserId() {
        return orgUserId;
    }

    public void setOrgUserId(int orgUserId) {
        this.orgUserId = orgUserId;
    }
}
