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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.weqa.R;
import com.weqa.adapter.TeamSummaryListAdapter;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.BookingInput;
import com.weqa.model.BookingReleaseInput;
import com.weqa.model.BookingResponse;
import com.weqa.model.CodeConstants;
import com.weqa.model.CollaborationInput;
import com.weqa.model.CollaborationResponse;
import com.weqa.model.adapterdata.TeamSummaryListData;
import com.weqa.model.adapterdata.TeamSummaryListItem;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.async.BookingAsyncTask;
import com.weqa.util.async.BookingReleaseAsyncTask;
import com.weqa.util.async.BookingRenewAsyncTask;
import com.weqa.util.async.CollaborationAsyncTask;
import com.weqa.util.DatetimeUtil;
import com.weqa.util.ui.DialogUtil;
import com.weqa.util.QRCodeUtil;
import com.weqa.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TeamSummaryActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String LOG_TAG = "WEQA-LOG";

    private RecyclerView teamSummaryList;
    private CircleImageView profilePicture;
    private ProgressBar progressBar;
    private TextView noteams;

    private TeamSummaryListData teamData;
    private TeamSummaryListAdapter adapter;

    private SharedPreferencesUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_team_summary);

        teamSummaryList = (RecyclerView) findViewById(R.id.teamSummaryList);

        LinearLayoutManager layoutManagerNew = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        teamSummaryList.setLayoutManager(layoutManagerNew);

        teamData = new TeamSummaryListData();
        adapter = new TeamSummaryListAdapter(teamData, this);
        teamSummaryList.setAdapter(adapter);

        profilePicture = (CircleImageView) findViewById(R.id.profilepicture);

        ImageView createTeamImage = (ImageView) findViewById(R.id.createTeamImage);
        TextView createTeamText = (TextView) findViewById(R.id.createTeamText);

        createTeamImage.setOnClickListener(this);
        createTeamText.setOnClickListener(this);

        createTeamImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                ImageView i = (ImageView) view;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    i.setColorFilter(ContextCompat.getColor(view.getContext(), R.color.colorTABtext));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    i.setColorFilter(ContextCompat.getColor(view.getContext(), R.color.colorTABtextSelected));
                }
                return false;
            }
        });

        createTeamText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                TextView i = (TextView) view;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    i.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorTABtextSelected));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    i.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorTABtext));
                }
                return false;
            }
        });

        LinearLayout menu1 = (LinearLayout) findViewById(R.id.menu1);
        LinearLayout menu2 = (LinearLayout) findViewById(R.id.menu2);
        LinearLayout menu3 = (LinearLayout) findViewById(R.id.menu3);
        LinearLayout menu4 = (LinearLayout) findViewById(R.id.menu4);
        LinearLayout menu5 = (LinearLayout) findViewById(R.id.menu5);

        menu1.setOnClickListener(this);
        menu2.setOnClickListener(this);
        menu3.setOnClickListener(this);
        menu4.setOnClickListener(this);
        menu5.setOnClickListener(this);

        menu1.setOnTouchListener(this);
        menu2.setOnTouchListener(this);
        menu3.setOnTouchListener(this);
        menu4.setOnTouchListener(this);
        menu5.setOnTouchListener(this);

        CircleImageView profilePicture = (CircleImageView) findViewById(R.id.profilepicture);
        profilePicture.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        util = new SharedPreferencesUtil(this);

        noteams = (TextView) findViewById(R.id.noteams);

        fetchTeamData();
    }

    private void fetchTeamData() {

        progressBar.setVisibility(View.VISIBLE);
        teamSummaryList.setVisibility(View.GONE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to get collaboration data...");
        CollaborationInput input = new CollaborationInput();
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<List<CollaborationResponse>> call = service.collaboration(input);

        CustomCallback<List<CollaborationResponse>> customCallback = new CustomCallback<List<CollaborationResponse>>(call.getUrl(), this) {
            @Override
            public void success(final Response<List<CollaborationResponse>> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                TeamSummaryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTeamList(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        LinearLayout l = (LinearLayout) v;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            l.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            l.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorMENU));
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.createTeamImage || v.getId() == R.id.createTeamText) {
            Intent i = new Intent(v.getContext(), CreateTeamActivity.class);
            i.setFlags(0);
            startActivityForResult(i, 5);
        }
        else if (v.getId() == R.id.menu3) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        } else if (v.getId() == R.id.profilepicture) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("SCREEN_FROM", "TeamSummary");
            i.setFlags(0);
            this.startActivityForResult(i, 20);
        }
        else if (v.getId() == R.id.menu1) {
            this.finish();
        }
        else if (v.getId() != R.id.menu4){
            Toast.makeText(v.getContext(), "Under Development", Toast.LENGTH_SHORT).show();
        }
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(LOG_TAG, "Request Code inside onActivityResult is " + requestCode);

        if (requestCode == 5 || requestCode == 10 || requestCode == 20) {
            fetchTeamData();
        }
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();QRCodeUtil qrCodeUtil = new QRCodeUtil(util, this);
                } else {
                    String qrCode = result.getContents();
                    QRCodeUtil qrCodeUtil = new QRCodeUtil(util, this);
                    if (qrCodeUtil.isQRItemCodeValid(qrCode)) {
                        bookQRCodeItem(qrCode);
                    }
                    else {
                        DialogUtil.showOkDialog(this, "Invalid WEQA Code", false);
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void bookQRCodeItem(final String qrCode) {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();
        RetrofitService service = retrofit.create(RetrofitService.class);

        String qrCodeBooked = util.getBookingQRCode();
        // Case of new booking or Re-Booking
        if (qrCodeBooked == null || qrCodeBooked.equals(qrCode)) {

            Log.d(LOG_TAG, "Calling the API to book...");

            BookingInput input = new BookingInput(InstanceIdService.getAppInstanceId(this), qrCode);
            Gson gson = new Gson();
            String json = gson.toJson(input); // myObject - instance of MyObject
            Log.d(LOG_TAG, "INPUT: " + json);

            MyCall<BookingResponse> call = service.book(input);

            CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
                @Override
                public void success(final Response<BookingResponse> response) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.d(LOG_TAG, "OUTPUT: " + json);

                    TeamSummaryActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showBookingResponse(response.body(), qrCode);
                        }
                    });
                }
            };
            call.enqueue(customCallback);
            Log.d(LOG_TAG, "Waiting for response...");
        }
        // Case of double booking
        else if (!qrCodeBooked.equals(qrCode)) {
            BookingReleaseInput input = new BookingReleaseInput(CodeConstants.AC601, InstanceIdService.getAppInstanceId(this), qrCode);

            Log.d(LOG_TAG, "Calling the API to release...");

            Gson gson = new Gson();
            String json = gson.toJson(input); // myObject - instance of MyObject
            Log.d(LOG_TAG, "INPUT: " + json);

            MyCall<BookingResponse> call = service.bookRelease(input);

            CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
                @Override
                public void success(final Response<BookingResponse> response) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.d(LOG_TAG, "OUTPUT: " + json);

                    TeamSummaryActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showBookingReleaseResponse(response.body(), qrCode, true, false);
                        }
                    });
                }
            };
            call.enqueue(customCallback);
            Log.d(LOG_TAG, "Waiting for response...");
        }
    }

    public void releaseQRCodeItem(final String qrCode, final boolean showConfirmation, final boolean removeLocalBooking) {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();
        RetrofitService service = retrofit.create(RetrofitService.class);

        BookingReleaseInput input = new BookingReleaseInput(CodeConstants.AC301, InstanceIdService.getAppInstanceId(this), qrCode);
        Log.d(LOG_TAG, "Calling the API to release...");
        Gson gson = new Gson();
        String json = gson.toJson(input); // myObject - instance of MyObject
        Log.d(LOG_TAG, "INPUT: " + json);

        MyCall<BookingResponse> call = service.bookRelease(input);

        CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<BookingResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                TeamSummaryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBookingReleaseResponse(response.body(), qrCode, showConfirmation, removeLocalBooking);
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void renewQRCodeItem(final String qrCode) {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to renew...");
        BookingReleaseInput input = new BookingReleaseInput(CodeConstants.AC302, InstanceIdService.getAppInstanceId(this), qrCode);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<BookingResponse> call = service.bookRelease(input);

        CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<BookingResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                TeamSummaryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBookingRenewResponse(response.body(), qrCode);
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void showBookingRenewResponse(BookingResponse br, String qrCode) {
        if (br.getActionCode().equals(CodeConstants.RC302)) {
            String message = "Desk is successfully booked for next " + DatetimeUtil.getTimeDifference(br.getBookedTime());
            DialogUtil.showOkDialog(this, message, true);
            util.addBooking(qrCode, br.getBookedTime());
        }
        fetchTeamData();
    }

    public void showBookingResponse(BookingResponse br, String qrCode) {

        // set the custom dialog components - text, image and button
        if (br.getActionCode().equals(CodeConstants.RC301)) {
            DialogUtil.showOkDialogWithCancel(this,
                    "Desk is successfully booked for next " + DatetimeUtil.getTimeDifference(br.getBookedTime()),
                    qrCode);
            util.addBooking(qrCode, br.getBookedTime());
        } else if (br.getActionCode().equals(CodeConstants.RC401)) {
            String message = "Desk is not available - " + DatetimeUtil.getTimeDifference(br.getBookedTime())
                    + " remaining on current booking.";
            DialogUtil.showOkDialog(this, message, true);
        }
        // If user has booked the same qrCode before and has scanned the same qrCode again.
        else if (br.getActionCode().equals(CodeConstants.RC501)) {
            String message = "You still have " + DatetimeUtil.getTimeDifference(br.getBookedTime()) + " remaining on this booking";
            DialogUtil.showDialogWithThreeButtons(this, message, qrCode);
        }
        fetchTeamData();
    }

    public void showBookingReleaseResponse(BookingResponse br, String qrCode,
                                           boolean showConfirmation, boolean removeLocalBooking) {
        if (br.getActionCode().equals(CodeConstants.RC601)) {
            if (showConfirmation)
                DialogUtil.showOkDialog(this, "Desk is now released!", false);
            if (removeLocalBooking)
                util.removeBooking();
        } else if (br.getActionCode().equals(CodeConstants.RC401)) {
            String message = "Desk is not available - " + DatetimeUtil.getTimeDifference(br.getBookedTime())
                    + " remaining on current booking.";
            DialogUtil.showOkDialog(this, message, true);
        }
        // If user has already booked one qrCode and this is the second one booked
        else if (br.getActionCode().equals(CodeConstants.RC701)) {
            String qrCodeOld = util.getBookingQRCode();
            String message = "Desk is successfully booked for next " + DatetimeUtil.getTimeDifference(br.getBookedTime());

            DialogUtil.showOkDialogWithCancelForSecondBooking(this, message, qrCodeOld, qrCode, br.getBookedTime());
        }
        // If user has already booked two desks, new booking is not allowed
        else if (br.getActionCode().equals(CodeConstants.RC801)) {
        }
        fetchTeamData();
    }

    public void updateTeamList(List<CollaborationResponse> responses) {
        teamData.getListData().removeAll(teamData.getListData());
        List<TeamSummaryListItem> itemList = new ArrayList<TeamSummaryListItem>();
        String previousOrg = "";

        CollaborationResponse[] responsesArray = responses.toArray(new CollaborationResponse[responses.size()]);
        Arrays.sort(responsesArray, CollaborationResponse.OrgNameComparator);

        for (CollaborationResponse r : responsesArray) {
            if (!previousOrg.equals(r.getOrgName())) {
                TeamSummaryListItem item = new TeamSummaryListItem();
                item.setOrg(true);
                item.setOrgName(r.getOrgName());
                itemList.add(item);
                previousOrg = r.getOrgName();
            }
            TeamSummaryListItem item = new TeamSummaryListItem();
            item.setTeamName(r.getTeamName());
            item.setNumberOfMembers(r.getTotalMember());
            item.setCreatedDate(DatetimeUtil.getDateFromGMT(r.getCreationDate()));
            item.setColocated(r.getCoLocated());
            item.setOrg(false);
            item.setTeamId(r.getTeamId());
            item.setOrgName(previousOrg);
            item.setOrgId(r.getOrgId());
            itemList.add(item);
        }
        teamData.addItems(itemList);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        if (teamData.getListData().isEmpty()) {
            noteams.setVisibility(View.VISIBLE);
            teamSummaryList.setVisibility(View.GONE);
        }
        else {
            noteams.setVisibility(View.GONE);
            teamSummaryList.setVisibility(View.VISIBLE);
        }
    }

}
