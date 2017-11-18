package com.weqa.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.weqa.R;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.adapterdata.TeamMemberListData;
import com.weqa.model.adapterdata.TeamMemberListItem;
import com.weqa.model.json.DeleteTeamInput;
import com.weqa.model.json.DeleteTeamMemberInput;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.ui.TeamDetailActivity;
import com.weqa.util.async.DeleteTeamAsyncTask;
import com.weqa.util.async.DeleteTeamMemberAsyncTask;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Manish Ballav on 9/21/2017.
 */
public class TeamMember2ListAdapter extends RecyclerView.Adapter<TeamMember2ListAdapter.TeamMemberItemHolder> {

    private static final String LOG_TAG = "WEQA-LOG";

    class TeamMemberItemHolder extends RecyclerView.ViewHolder {

        private TextView name, mobile, designation, location;
        private Button removeButton;

        public TeamMemberItemHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.name);
            mobile = (TextView)itemView.findViewById(R.id.mobile);
            location = (TextView) itemView.findViewById(R.id.locationText);
            designation = (TextView) itemView.findViewById(R.id.designation);
            removeButton = (Button) itemView.findViewById(R.id.removeButton);
        }

    }

    protected LayoutInflater inflater;
    protected TeamMemberListData itemData;
    protected TeamDetailActivity activity;
    private long teamId;
    private String creatorMobile;
    private String userMobile;

    public TeamMember2ListAdapter(TeamMemberListData itemData, TeamDetailActivity activity, long teamId, String creatorMobile, String userMobile){
        inflater = LayoutInflater.from(activity);
        this.itemData = itemData;
        this.activity = activity;
        this.teamId = teamId;
        this.creatorMobile = creatorMobile;
        this.userMobile = userMobile;
    }

    public void setItemData(TeamMemberListData itemData) {
        this.itemData = itemData;
    }

    @Override
    public TeamMember2ListAdapter.TeamMemberItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.team_member_list_item2, parent, false);
        return new TeamMember2ListAdapter.TeamMemberItemHolder(view);
    }

    @Override
    public void onBindViewHolder(TeamMember2ListAdapter.TeamMemberItemHolder holder, final int position) {
        final TeamMemberListItem item = this.itemData.getListData().get(position);
        holder.name.setText(item.getFirstName() + " " + item.getLastName());
        holder.designation.setText(" - " + item.getDesignation());
        holder.mobile.setText(item.getMobile());
        if (item.getLocation() != null && !item.getLocation().isEmpty()) {
            holder.location.setText(item.getFloorLevel() + ", " + item.getLocation());
        }
        if (item.getMobile().equals(userMobile) && (!item.getMobile().equals(creatorMobile))) {
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteTeamMember(position, InstanceIdService.getAppInstanceId(activity), item.getOrgId(), teamId);
                }
            });
        }
        else {
            holder.removeButton.setVisibility(View.GONE);
        }
    }

    public void deleteTeamMember(final int position, String uuid, long orgId, long teamId) {

        activity.updateUIBeforeTeamMemberDelete();

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to delete team member...");
        DeleteTeamMemberAsyncTask runner = new DeleteTeamMemberAsyncTask(retrofit, LOG_TAG, activity, position);

        DeleteTeamMemberInput input = new DeleteTeamMemberInput();
        input.setTeamId((int) teamId);
        input.setOrgId((int) orgId);
        input.setUuid(uuid);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<Void> call = service.deleteTeamMember(input);

        CustomCallback<Void> customCallback = new CustomCallback<Void>(call.getUrl(), activity) {
            @Override
            public void success(Response<Void> response) {
                Log.d(LOG_TAG, "OUTPUT: Void");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.updateUIAfterTeamMemberDelete(position);
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    @Override
    public int getItemCount() {
        return this.itemData.getListData().size();
    }
}
