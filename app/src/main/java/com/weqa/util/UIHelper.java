package com.weqa.util;

import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by Manish Ballav on 8/31/2017.
 */

public class UIHelper {

    private static final String LOG_TAG = "WEQA-LOG";

    public static void changeTabsFont(TabLayout mTabLayout, Typeface tf) {
        ViewGroup vg = (ViewGroup) mTabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(tf);
                }
            }
        }
    }

    public static void hideDay(DatePicker mDatePicker) {
        LinearLayout pickerParentLayout =
                (LinearLayout) mDatePicker.getChildAt(0);
        LinearLayout pickerSpinnersHolder = (LinearLayout) pickerParentLayout.getChildAt(0);
//        NumberPicker picker = (TextView) pickerSpinnersHolder.getChildAt(0);
        pickerSpinnersHolder.setVisibility(View.GONE);
    }

    public static void hideDay2(DatePicker datePicker) {
        try {
            java.lang.reflect.Field[] f = datePicker.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : f) {
                if (field.getName().equals("mDayPicker") || field.getName().equals("mDaySpinner")) {
                    field.setAccessible(true);
                    Object dmPicker = new Object();
                    dmPicker = field.get(datePicker);
                    ((View) dmPicker).setVisibility(View.GONE);
                }
            }
        }
        catch (SecurityException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        catch (IllegalAccessException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }
}
