package com.weqa.model.adapterdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish Ballav on 10/13/2017.
 */

public class GrantedListData {

    List<GrantedListItem> grantedListItems;

    public GrantedListData() {
        grantedListItems = new ArrayList<GrantedListItem>();
    }

    public List<GrantedListItem> getListData() {
        return grantedListItems;
    }

}
