package com.weqa.util;

import android.content.Context;
import android.util.Log;

import com.weqa.model.Authorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Manish Ballav on 8/31/2017.
 */

public class BuildingUtil {

    private String logTag;
    private SharedPreferencesUtil util;
    private Context context;

    public BuildingUtil(String logTag, SharedPreferencesUtil util, Context context) {
        this.logTag = logTag;
        this.util = util;
        this.context = context;
    }

    public Authorization getBuildingForSearchBar(List<Authorization> authList) {
        Authorization nearestBuilding = findNearestBuilding(authList);
        if (nearestBuilding != null) {
            return nearestBuilding;
        }
        else {
            Log.d(logTag, "Nearest Building Search returned NULL!");
            Authorization latestBookedBuilding = util.getLatestBookedBuilding();
            if (latestBookedBuilding != null) {
                return latestBookedBuilding;
            }
            else {
                Log.d(logTag, "Latest Booked Building Search returned NULL!");
                Authorization firstBuilding = getFirstSortedBuilding(authList);
                if (firstBuilding != null) {
                    return firstBuilding;
                }
            }
        }
        return null;
    }

    private Authorization getFirstSortedBuilding(List<Authorization> authList) {
        if (authList.size() == 0)
            return null;
        else {
            Authorization[] authArray = authList.toArray(new Authorization[authList.size()]);
            Arrays.sort(authArray, Authorization.BuildingFloorNameComparator);
            Log.d(logTag, "" + authArray[0]);
            return authArray[0];
        }
    }

    private Authorization findNearestBuilding(List<Authorization> authList) {
        double geofenceRadius = util.getGeofenceRadius();
//        double geofenceRadius = 500;

        Log.d(logTag, "Geofence Radius: " + geofenceRadius);

        Authorization nearestBuilding = null;
        LocationTracker tracker = new LocationTracker(context);
        // check if location is available
        if (tracker.isLocationEnabled()) {
            double lat1 = tracker.getLatitude();
            double lon1 = tracker.getLongitude();
//            double lat1 = -33.8951098;
//            double lon1 = 151.1989009;

            double nearestDistance = geofenceRadius + 1;
            for (Authorization authInfo : authList) {
                double lat2 = Double.parseDouble(authInfo.getLatitude());
                double lon2 = Double.parseDouble(authInfo.getLongitude());
                double distance = LocationUtil.getDistance(lat1, lon1, lat2, lon2);
                Log.d(logTag, "Current Location(" + lat1 + "," + lon1 + "), Building (" + lat2 + "," + lon2 + ")");
                Log.d(logTag, "DISTANCE: " + distance);
//                Log.d(logTag, "Current Location(Go get Carshare pod) to Building (" + authInfo.getFloorName() + ")");
//                Log.d(logTag, "DISTANCE: " + distance);
                if (distance <= geofenceRadius) {
                    if (distance < nearestDistance) {
                        nearestBuilding = authInfo;
                        nearestDistance = distance;
                    }
                }
            }
        }
        else {
            // show dialog box to user to enable location
            tracker.askToOnLocation();
        }
        if (nearestBuilding != null)
            return nearestBuilding;
        else
            return null;
    }

    public void testNearestBuilding() {
        List<Authorization> authList = new ArrayList<Authorization>();
        Authorization b1 = new Authorization();

        b1.setAddress("136 Regent St, Redfern NSW 2016, Australia");
        b1.setFloorName("Basement Skate");
        b1.setLatitude("-33.8943723");
        b1.setLongitude("151.1991926");
        authList.add(b1);

        Authorization b2 = new Authorization();
        b2.setAddress("88 Rosehill St, Redfern NSW 2016, Australia");
        b2.setFloorName("Saint German Patisserie");
        b2.setLatitude("-33.8949006");
        b2.setLongitude("151.1981613");
        authList.add(b2);

        Authorization b3 = new Authorization();
        b3.setAddress("146 Regent St, Redfern NSW 2016, Australia");
        b3.setFloorName("The D E A Store");
        b3.setLatitude("-33.8946829");
        b3.setLongitude("151.1990411");
        authList.add(b3);

        /*
        Authorization b4 = new Authorization();
        b4.setAddress("Alexandria NSW 2015, Australia");
        b4.setLatitude("-33.8947323");
        b4.setLongitude("151.1976483");
        authList.add(b4);
        */

        /* Distance from Go get Carshare pod */

        Authorization n = findNearestBuilding(authList);

        Log.d(logTag, "\n\nNearest Building: " + n.getFloorName());
    }
}
