package com.weqa.model.adapterdata;

import android.content.Context;
import android.util.Log;

import com.weqa.model.Authentication;
import com.weqa.model.Org;
import com.weqa.util.SharedPreferencesUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Manish Ballav on 10/15/2017.
 */

public class OrgListData {

    private static final String LOG_TAG = "WEQA-LOG";

    SharedPreferencesUtil util;
    List<OrgListItem> orgList;

    public OrgListData(Context c) {
        util = new SharedPreferencesUtil(c);
        initialize();
/*      Data added for testing purposes
        OrgListItem item = new OrgListItem();
        item.setOrganizationId(2);
        item.setOrgName("WEQA");
        item.setPrivilege("Admin");
        item.setEndDate(new Date());
        item.setDefaultOrg(false);
        orgList.add(item);*/
    }

    public List<OrgListItem> getListData() {
        return orgList;
    }

    public void addItem(OrgListItem item) {
        orgList.add(item);
    }

    private void initialize() {
        Authentication auth = util.getAuthenticationInfo();
        int defaultOrgId = util.getDefaultOrganization();
        orgList = new ArrayList<OrgListItem>();

        if (auth != null) {
            for (Org o : auth.getOrganization()) {
                Log.d(LOG_TAG, "ORG: " + o.getOrganizationName() + ", Privilege: " + o.getPrivilegeName());
                OrgListItem item = new OrgListItem();
                item.setOrgName(o.getOrganizationName());
                item.setPrivilege(o.getPrivilegeName());
                item.setOrganizationId(o.getOrganizationId());
                if (defaultOrgId == o.getOrganizationId()) {
                    item.setDefaultOrg(true);
                }
                else {
                    item.setDefaultOrg(false);
                }
                if (o.getPrivilegeName().equals("Guest")) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        String endDate = o.getEndDate().substring(0, 10);
                        item.setEndDate(format.parse(endDate));
                    }
                    catch (ParseException pe) {
                        Log.d(LOG_TAG, "Error parsing date in OrgListData class: " + pe.getMessage());
                    }
                }
                orgList.add(item);
            }
        }
    }

}
