package com.weqa.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.weqa.R;
import com.weqa.adapter.TeamMember2ListAdapter;
import com.weqa.adapter.TeamMemberListAdapter;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.Authentication;
import com.weqa.model.Org;
import com.weqa.model.adapterdata.TeamMemberListData;
import com.weqa.model.adapterdata.TeamMemberListItem;
import com.weqa.model.json.AddTeamMemberInput;
import com.weqa.model.json.CreateTeamInput;
import com.weqa.model.json.DeleteTeamInput;
import com.weqa.model.json.TeamDetailInput;
import com.weqa.model.json.TeamDetailResponse;
import com.weqa.model.json.UuidList;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.DatetimeUtil;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.SharedPreferencesUtil;
import com.weqa.util.async.AddTeamMemberAsyncTask;
import com.weqa.util.async.CreateTeamAsyncTask;
import com.weqa.util.async.DeleteTeamAsyncTask;
import com.weqa.util.async.DeleteTeamMemberAsyncTask;
import com.weqa.util.async.GetTeamDetailAsyncTask;
import com.weqa.widget.CustomQRScannerActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

public class TeamDetailActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String LOG_TAG = "WEQA-LOG";

    private RecyclerView teamMemberList;
    private ProgressBar progressBar;
    private TeamMemberListData teamData;
    private TeamMember2ListAdapter adapter;
    SharedPreferencesUtil util;
    Authentication auth;

    private TextView teamName, orgName, teamPurpose, created, createdBy;
    private ImageView deleteTeamButton;
    private int teamId, orgId;
    private String creatorMobile = null;
    private String mobileNumber;

    private List<TeamMemberListItem> newMemberList = new ArrayList<TeamMemberListItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_team_detail);

        util = new SharedPreferencesUtil(this);
        auth = util.getAuthenticationInfo();
        mobileNumber = auth.getMobileNo();

        Intent intent = getIntent();
        teamId = intent.getIntExtra("TEAM_ID", 0);
        orgId = intent.getIntExtra("ORG_ID", 0);

        teamMemberList = (RecyclerView) findViewById(R.id.memberList);

        LinearLayoutManager layoutManagerNew = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        teamMemberList.setLayoutManager(layoutManagerNew);

        teamData = new TeamMemberListData();

        teamName = (TextView) findViewById(R.id.teamName);
        orgName = (TextView) findViewById(R.id.orgName);
        teamPurpose = (TextView) findViewById(R.id.teamPurpose);
        created = (TextView) findViewById(R.id.created);
        createdBy = (TextView) findViewById(R.id.createdBy);
        deleteTeamButton = (ImageView) findViewById(R.id.deleteTeamButton);

        ImageView backArrow = (ImageView) findViewById(R.id.backButton);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TeamDetailActivity.this.finish();
            }
        });

        ImageView addMember = (ImageView) findViewById(R.id.addMember);
        addMember.setOnClickListener(this);

        backArrow.setOnTouchListener(this);
        addMember.setOnTouchListener(this);

        deleteTeamButton.setVisibility(View.GONE);
        deleteTeamButton.setOnTouchListener(this);

        deleteTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTeam();
            }
        });

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newMemberList.size() > 0) {
                    addMembersToTeam();
                }
                else {
                    TeamDetailActivity.this.finish();
                }
            }
        });

        saveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Button b = (Button) v;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    b.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    b.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorMENU));
                }
                return false;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        fetchTeamDetail(teamId);
    }

    public void deleteTeam() {
        if (creatorMobile == null) return;

        progressBar.setVisibility(View.VISIBLE);
        teamMemberList.setVisibility(View.GONE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to delete team...");
        DeleteTeamAsyncTask runner = new DeleteTeamAsyncTask(retrofit, LOG_TAG, this);

        DeleteTeamInput input = new DeleteTeamInput();
        input.setTeamId(teamId);
        input.setOrgId(orgId);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<Void> call = service.deleteTeam(input);

        CustomCallback<Void> customCallback = new CustomCallback<Void>(call.getUrl(), this) {
            @Override
            public void success(Response<Void> response) {
                Log.d(LOG_TAG, "OUTPUT: Void");

                TeamDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUIAfterTeamDelete();
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ImageView i = (ImageView) v;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v.getId() == R.id.addMember) {
                i.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorTABtext));
            }
            else {
                i.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (v.getId() == R.id.addMember) {
                i.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
            }
            else {
                i.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorLightGrey));
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, TeamMemberScanActivity.class);

        intent.putExtra("ORG_ID", orgId);
        intent.putExtra("SCREEN_ID", 2);
        intent.putExtra("MOBILE", mobileNumber);
        intent.putParcelableArrayListExtra("EXISTING_USERS", teamData.getListData());

        startActivityForResult(intent, 2);// Activity is started with requestCode 2
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode == 2)
        {
            ArrayList<TeamMemberListItem> memberList = data.getParcelableArrayListExtra("NEW_USER_LIST");
            teamData.addMembers(memberList);
            newMemberList.addAll(memberList);
            adapter.notifyDataSetChanged();
        }
    }

    private void fetchTeamDetail(int teamId) {

        progressBar.setVisibility(View.VISIBLE);
        teamMemberList.setVisibility(View.GONE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to create new team...");
        GetTeamDetailAsyncTask runner = new GetTeamDetailAsyncTask(retrofit, LOG_TAG, this);

        TeamDetailInput input = new TeamDetailInput();
        input.setTeamId(teamId);
        input.setOrgId(orgId);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<List<TeamDetailResponse>> call = service.teamDetail(input);

        CustomCallback<List<TeamDetailResponse>> customCallback = new CustomCallback<List<TeamDetailResponse>>(call.getUrl(), this) {
            @Override
            public void success(final Response<List<TeamDetailResponse>> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                TeamDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUIWithTeamDetail(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void updateUIWithTeamDetail(List<TeamDetailResponse> responses) {
        if (responses == null || responses.size() == 0) {
            Toast.makeText(this, R.string.fatal_error2, Toast.LENGTH_SHORT).show();
            this.finish();
        }
        TeamDetailResponse r = responses.get(0);
        String createdOn = DatetimeUtil.getLocalDate(r.getCreationDate());
        created.setText(createdOn);

        if (r.getTeamDescription() == null) {
            teamPurpose.setVisibility(View.GONE);
        }
        else {
            teamPurpose.setText(r.getTeamDescription());
        }
        teamName.setText(r.getTeamName());
        createdBy.setText(getCreatedBy(responses));
        orgName.setText(getOrgName(r.getOrgId()));

        String creatorMobile = "";
        List<String> mobileList = new ArrayList<String>();
        List<TeamMemberListItem> memberList = new ArrayList<TeamMemberListItem>();

        for (TeamDetailResponse rr : responses) {
            if (rr.getTeamCreator() != null && rr.getTeamCreator()) {
                creatorMobile = rr.getMobileNo();
            }
            if (mobileList.indexOf(rr.getMobileNo()) == -1) {
                TeamMemberListItem item = new TeamMemberListItem();
                item.setFirstName(rr.getFirstName());
                item.setLastName(rr.getLastName());
                item.setDesignation(rr.getDesignation());
                item.setMobile(rr.getMobileNo());
                item.setUuid(rr.getUuid());
                item.setLocation(rr.getBuildingAddress());
                item.setOrgId(rr.getOrgId());
                item.setFloorLevel(rr.getFloorLevel());
                memberList.add(item);
                mobileList.add(rr.getMobileNo());
            }
        }

        if (mobileList.size() == 1 && mobileList.get(0).equals(mobileNumber)) {
            deleteTeamButton.setVisibility(View.VISIBLE);
        }

        teamData.addMembers(memberList);
        adapter = new TeamMember2ListAdapter(teamData, this, teamId, creatorMobile, mobileNumber);
        teamMemberList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        teamMemberList.setVisibility(View.VISIBLE);
    }

    private String getCreatedBy(List<TeamDetailResponse> responses) {
        for (TeamDetailResponse rr : responses) {
            if (rr.getTeamCreator() != null && rr.getTeamCreator()) {
                creatorMobile = rr.getMobileNo();
                return rr.getFirstName() + " " + rr.getLastName();
            }
        }
        return "";
    }

    private String getOrgName(int orgId) {
        List<Org> orgList = auth.getOrganization();
        for (Org o : orgList) {
            if (o.getOrganizationId() == orgId) {
                return o.getOrganizationName();
            }
        }
        return "";
    }

    private void addMembersToTeam() {

        progressBar.setVisibility(View.VISIBLE);
        teamMemberList.setVisibility(View.GONE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to add team members...");

        AddTeamMemberInput input = new AddTeamMemberInput();
        input.setTeamId(teamId);
        input.setOrgId(orgId);

        List<UuidList> uuidList = new ArrayList<UuidList>();
        for (TeamMemberListItem item : newMemberList) {
            UuidList uuid = new UuidList();
            uuid.setUUID(item.getUuid());
            uuidList.add(uuid);
        }

        input.setUuidList(uuidList);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<Void> call = service.addTeamMember(input);

        CustomCallback<Void> customCallback = new CustomCallback<Void>(call.getUrl(), this) {
            @Override
            public void success(Response<Void> response) {
                Log.d(LOG_TAG, "OUTPUT: Void");

                TeamDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        teamMembersAdded();
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");

    }

    public void teamMembersAdded() {
        Intent intent = new Intent();
        setResult(10, intent);
        this.finish();
    }

    public void updateUIAfterTeamDelete() {
        Intent intent = new Intent();
        setResult(10, intent);
        this.finish();
    }

    public void updateUIAfterTeamMemberDelete(int memberPosition) {

        teamData.getListData().remove(memberPosition);
        adapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
        teamMemberList.setVisibility(View.VISIBLE);
    }

    public void updateUIBeforeTeamMemberDelete() {
        progressBar.setVisibility(View.VISIBLE);
        teamMemberList.setVisibility(View.GONE);
    }
}
