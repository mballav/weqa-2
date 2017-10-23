package com.weqa.model;

/**
 * Created by Manish Ballav on 9/3/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

public class Org {

    @SerializedName("organizationName")
    @Expose
    private String organizationName;
    @SerializedName("organizationId")
    @Expose
    private Integer organizationId;
    @SerializedName("emailId")
    @Expose
    private String emailId;
    @SerializedName("designation")
    @Expose
    private String designation;
    @SerializedName("prievlageName")
    @Expose
    private String privilegeName;
    @SerializedName("prievlageId")
    @Expose
    private int privilegeId;
    @SerializedName("endDate")
    @Expose
    private String endDate;

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPrivilegeName() {
        return privilegeName;
    }

    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

    public int getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(int privilegeId) {
        this.privilegeId = privilegeId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public static Comparator<Org> OrgNameComparator
            = new Comparator<Org>() {

        public int compare(Org a1, Org a2) {

            String n1 = a1.getOrganizationName().toUpperCase();
            String n2 = a2.getOrganizationName().toUpperCase();

            //ascending order
            return n1.compareTo(n2);

            //descending order
            //return fruitName2.compareTo(fruitName1);
        }

    };
}

