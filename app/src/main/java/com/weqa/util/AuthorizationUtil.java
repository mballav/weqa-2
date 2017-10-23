package com.weqa.util;

import com.weqa.model.Authorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Manish Ballav on 8/31/2017.
 */

public class AuthorizationUtil {

    /**
     * This method iterates through the list of building returned by the API call and filters out
     * the buildings that do not belong to the default organization. It also removes duplicate
     * building entries.
     *
     * @param aList
     * @param defaultOrgId
     * @return
     */
    public static List<Authorization> filterBuildings(List<Authorization> aList, int defaultOrgId) {
        List<Authorization> bList = new ArrayList<Authorization>();
        List<String> dlist = new ArrayList<String>();

        Authorization[] authArray = aList.toArray(new Authorization[aList.size()]);
        Arrays.sort(authArray, Authorization.BuildingFloorNameComparator);

        for (Authorization a : authArray) {
            if (a.getOrganizationId() == defaultOrgId) {
                if (dlist.indexOf(a.getBuildingName() + ", " + a.getAddress()) == -1) {
                    bList.add(a);
                    dlist.add(a.getBuildingName() + ", " + a.getAddress());
                }
            }
        }
        return bList;
    }

    public static String getBuildingDisplayName(Authorization a) {
        String bName = a.getAddress();
        if (bName.length() > 27) bName = bName.substring(0, 27) + "...";
        return bName;
    }
}
