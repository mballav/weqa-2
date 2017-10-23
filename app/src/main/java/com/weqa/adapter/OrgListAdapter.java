package com.weqa.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.weqa.R;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.AuthInput;
import com.weqa.model.AuthResponse;
import com.weqa.model.CodeConstants;
import com.weqa.model.adapterdata.OrgListData;
import com.weqa.model.adapterdata.OrgListItem;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.ui.ConnectOrgActivity;
import com.weqa.ui.GuestUserActivity;
import com.weqa.ui.LandingScreenActivity;
import com.weqa.ui.ProfileActivity;
import com.weqa.util.SharedPreferencesUtil;

import java.text.SimpleDateFormat;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Manish Ballav on 10/15/2017.
 */

public class OrgListAdapter  extends RecyclerView.Adapter<OrgListAdapter.OrgItemHolder> {

    private static final String LOG_TAG = "WEQA-LOG";

    class OrgItemHolder extends RecyclerView.ViewHolder {

        private TextView orgName, privilege;
        private Button guestAccess;
        private ImageView defaultOrg;

        public OrgItemHolder(View itemView) {
            super(itemView);
            orgName = (TextView)itemView.findViewById(R.id.orgName);
            privilege = (TextView)itemView.findViewById(R.id.privilege);
            guestAccess = (Button) itemView.findViewById(R.id.guestButton);
            defaultOrg = (ImageView) itemView.findViewById(R.id.defaultImage);
        }

    }

    protected LayoutInflater inflater;
    protected OrgListData itemData;
    protected Activity c;
    protected SharedPreferencesUtil util;
    private ImageView defaultOrgImage;

    public OrgListAdapter(OrgListData itemData, Activity c){
        inflater = LayoutInflater.from(c);
        this.itemData = itemData;
        this.c = c;
        util = new SharedPreferencesUtil(c);
    }

    public void setItemData(OrgListData itemData) {
        this.itemData = itemData;
    }

    @Override
    public OrgListAdapter.OrgItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.organization_list_item, parent, false);
        return new OrgListAdapter.OrgItemHolder(view);
    }

    @Override
    public void onBindViewHolder(OrgListAdapter.OrgItemHolder holder, int position) {
        final OrgListItem item = this.itemData.getListData().get(position);
        holder.orgName.setText(item.getOrgName());
        if (item.getPrivilege().equals("Guest")) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
            String gDate = formatter.format(item.getEndDate());
            holder.privilege.setText("Guest access ending " + gDate);
        }
        else {
            holder.privilege.setText(item.getPrivilege());
        }
        if (item.getPrivilege().equals("Admin")) {
            holder.guestAccess.setVisibility(View.VISIBLE);
            holder.guestAccess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(c, GuestUserActivity.class);
                    i.putExtra("ORG_ID", item.getOrganizationId());
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(i);
                }
            });
        }
        else {
            holder.guestAccess.setVisibility(View.GONE);
        }
        if (item.isDefaultOrg()) {
            defaultOrgImage = holder.defaultOrg;
            holder.defaultOrg.setVisibility(View.VISIBLE);
            holder.defaultOrg.setColorFilter(ContextCompat.getColor(c, R.color.colorGreen));
        }
        else {
            holder.defaultOrg.setColorFilter(ContextCompat.getColor(c, R.color.colorLightGrey));
        }
        holder.defaultOrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView i = (ImageView) view;
                if (defaultOrgImage != i) {
                    i.setColorFilter(ContextCompat.getColor(c, R.color.colorGreen));
                    if (defaultOrgImage != null) {
                        defaultOrgImage.setColorFilter(ContextCompat.getColor(c, R.color.colorLightGrey));
                    }
                    util.setDefaultOrganization(item.getOrganizationId());
                    defaultOrgImage = i;
                }
                Intent intent = new Intent(c, LandingScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.itemData.getListData().size();
    }

}
