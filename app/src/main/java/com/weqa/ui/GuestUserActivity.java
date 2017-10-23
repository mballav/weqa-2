package com.weqa.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.weqa.R;
import com.weqa.adapter.GrantedListAdapter;
import com.weqa.adapter.OrgListAdapter;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.CodeConstants;
import com.weqa.model.adapterdata.GrantedListData;
import com.weqa.model.adapterdata.GrantedListItem;
import com.weqa.model.adapterdata.OrgListData;
import com.weqa.model.json.AddOrganizationInput;
import com.weqa.model.json.AddOrganizationResponse;
import com.weqa.model.json.CreateGuestInput;
import com.weqa.model.json.CreateGuestResponse;
import com.weqa.model.json.GetGuestInput;
import com.weqa.model.json.GetGuestResponse;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.UIHelper;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

public class GuestUserActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String LOG_TAG = "WEQA-LOG";
    DecoratedBarcodeView scanner;
    private BeepManager beepManager;

    private GrantedListData grantData;
    private RecyclerView grantList;
    private GrantedListAdapter grantAdapter;
    private GrantedListItem newGrantItem;

    private TextView nameText, mobileText, endDate;
    private Button grantButton;
    private int orgId;

    private RelativeLayout progressBarContainer;
    private ProgressBar progress1;
    private DatePickerDialog dpd;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
    private int mDay, mMonth, mYear;

    private BarcodeCallback callback = new BarcodeCallback() {

        @Override
        public void barcodeResult(BarcodeResult result) {
            String code = result.getText();
            if(code == null) {
                return;
            }

            if (!isValidUserCode(code)) {
                scanner.setStatusText("Invalid QR Code!");
                return;
            }

            /*
            String uuid = getUuid(code);

            if (isUserAlreadyGrantedAccess(uuid)) {
                scanner.setStatusText("User already granted access!");
                return;
            }*/

            newGrantItem = getGranteeDetails(code);
            nameText.setText(newGrantItem.getFirstName() + " " + newGrantItem.getLastName());
            mobileText.setText(newGrantItem.getMobile());
            endDate.setText(dateFormat.format(newGrantItem.getEndDate()));

            grantButton.setEnabled(true);

            beepManager.playBeepSoundAndVibrate();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_guest_user);

        Intent i = getIntent();
//        orgId = i.getIntExtra("ORG_ID", 0);
        orgId = 1;

        scanner = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        scanner.decodeContinuous(callback);

        beepManager = new BeepManager(this);

        nameText = (TextView) findViewById(R.id.nameText);
        mobileText = (TextView) findViewById(R.id.mobileText);
        endDate = (TextView) findViewById(R.id.endDate);

        nameText.setText("Name");
        mobileText.setText("Mobile");
        endDate.setText("Grant Date");

        grantButton = (Button) findViewById(R.id.grantButton);
        grantButton.setEnabled(false);

        grantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grantAccess();
            }
        });

        grantButton.setOnTouchListener(this);

        endDate.setOnClickListener(this);
        endDate.setOnTouchListener(this);

        grantList = (RecyclerView) findViewById(R.id.grantList);
        grantData = new GrantedListData();
        grantAdapter = new GrantedListAdapter(grantData, this);

        LinearLayoutManager layoutManagerNew = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        grantList.setLayoutManager(layoutManagerNew);

        grantList.setAdapter(grantAdapter);

        progressBarContainer = (RelativeLayout) findViewById(R.id.progressBarContainer);

        progress1 = (ProgressBar) findViewById(R.id.progress1);
        progress1.setVisibility(View.GONE);

        Button doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnTouchListener(this);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getGuests();
    }

    private void testQRCode() {

        String code = "0000,1DUMMYDUMMY,,Manish,Ballav,Developer,918084111803";

        if (!isValidUserCode(code)) {
            scanner.setStatusText("Invalid QR Code!");
            return;
        }

            /*
            String uuid = getUuid(code);

            if (isUserAlreadyGrantedAccess(uuid)) {
                scanner.setStatusText("User already granted access!");
                return;
            }*/

        newGrantItem = getGranteeDetails(code);
        nameText.setText(newGrantItem.getFirstName() + " " + newGrantItem.getLastName());
        mobileText.setText(newGrantItem.getMobile());
        endDate.setText(dateFormat.format(newGrantItem.getEndDate()));

        grantButton.setEnabled(true);

    }

    private void getGuests() {

        grantList.setVisibility(View.GONE);
        progressBarContainer.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to get Guests ...");

        GetGuestInput input = new GetGuestInput();
        input.setOrgId(orgId);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<List<GetGuestResponse>> call = service.getGuests(input);

        CustomCallback<List<GetGuestResponse>> customCallback = new CustomCallback<List<GetGuestResponse>>(call.getUrl(), this) {
            @Override
            public void success(final Response<List<GetGuestResponse>> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                GuestUserActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterGetGuests(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    private void afterGetGuests(List<GetGuestResponse> guestList) {
        for (GetGuestResponse r : guestList) {
            GrantedListItem item = new GrantedListItem();
            item.setFirstName(r.getFirstName());
            item.setLastName(r.getLastName());
            item.setMobile(r.getMobileNo());
            item.setOrgUserId(r.getOrgUserId());

            try {
                if (r.getEndDate() != null && r.getEndDate().length() >= 10) {
                    String  endDate = r.getEndDate().substring(0, 10);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    item.setEndDate(format.parse(endDate));
                }
            }
            catch (ParseException pe) {
                Log.d(LOG_TAG, "Error parsing date in GuestUserActivity: " + pe.getMessage());
            }
            grantData.getListData().add(item);
        }
        grantAdapter.notifyDataSetChanged();

        grantList.setVisibility(View.VISIBLE);
        progressBarContainer.setVisibility(View.GONE);

//        testQRCode();
    }

    private void grantAccess() {

        progress1.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to create guest access...");

        CreateGuestInput input = new CreateGuestInput();
        input.setOrgid(orgId);
        input.setFirstName(newGrantItem.getFirstName());
        input.setLastName(newGrantItem.getLastName());
        input.setMobileNo(newGrantItem.getMobile());
        input.setUserUUID(newGrantItem.getUuid());
        input.setAdminUUID(InstanceIdService.getAppInstanceId(this));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String endDate = format.format(newGrantItem.getEndDate());
        endDate += " 00:00:00.000";
        input.setEndDate(endDate);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<CreateGuestResponse> call = service.createGuestUser(input);

        CustomCallback<CreateGuestResponse> customCallback = new CustomCallback<CreateGuestResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<CreateGuestResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                GuestUserActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterCreateGuest(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    private void afterCreateGuest(CreateGuestResponse response) {

        boolean found = false;
        for (GrantedListItem i : grantData.getListData()) {
            if (i.getOrgUserId() == response.getOrgUserId()) {
                found = true;
                i.setEndDate(newGrantItem.getEndDate());
            }
        }

        if (!found) {
            GrantedListItem item = new GrantedListItem();
            item.setFirstName(newGrantItem.getFirstName());
            item.setLastName(newGrantItem.getLastName());
            item.setMobile(newGrantItem.getMobile());
            item.setOrgUserId(response.getOrgUserId());
            item.setEndDate(newGrantItem.getEndDate());
            item.setUuid(newGrantItem.getUuid());

            grantData.getListData().add(item);
        }
        grantAdapter.notifyDataSetChanged();

        grantButton.setEnabled(false);
        progress1.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.endDate) {
            TextView t = (TextView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                t.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                t.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtext));
            }
        }
        else if (v.getId() == R.id.grantButton) {
            Button b = (Button) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                b.setBackgroundResource(R.drawable.super_rounded_button_yellow);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                b.setBackgroundResource(R.drawable.super_rounded_button_darkgrey);
            }
        }
        else if (v.getId() == R.id.doneButton) {
            Button b = (Button) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTABtextSelected));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                b.setBackgroundColor(ContextCompat.getColor(this, R.color.colorMENU));
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        scanner.pauseAndWait();
    }

    @Override
    public void onResume() {
        super.onResume();
        scanner.resume();
    }

    public void pause(View view) {
        scanner.pause();
    }

    public void resume(View view) {
        scanner.resume();
    }

    public void triggerScan(View view) {
        scanner.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return scanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private boolean isValidUserCode(String code) {
        String[] tokens = code.split(",");
        return code.startsWith(CodeConstants.QR_CODE_MEMBER) && (tokens.length == 7);
    }

    private String getUuid(String code) {
        String[] tokens = code.split(",");
        return tokens[1];
    }

    /*
    private boolean isUserAlreadyGrantedAccess(String uuid) {
        boolean found = false;
        for (GrantedListItem item : grantData.getListData()) {
            if (item.getUuid().equals(uuid)) {
                found = true;
                break;
            }
        }
        return found;
    }*/

    private GrantedListItem getGranteeDetails(String code) {
        String[] tokens = code.split(",");
        GrantedListItem item = new GrantedListItem();
        item.setFirstName(tokens[3]);
        item.setLastName(tokens[4]);
        item.setMobile(tokens[6]);
        item.setUuid(tokens[1]);
        item.setEndDate(new Date());
        return item;
    }

    @Override
    public void onClick(View v) {

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        dpd = new DatePickerDialog(this, onDateSetListener, mYear, mMonth, mDay);
        dpd.getDatePicker().setMinDate(System.currentTimeMillis());

        UIHelper.hideDay(dpd.getDatePicker());

        dpd.show();
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener =  new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year,
        int monthOfYear, int dayOfMonth) {

            Calendar cal = Calendar.getInstance();
            cal.set(year, monthOfYear, dayOfMonth);
            endDate.setText(dateFormat.format(cal.getTime()));
            newGrantItem.setEndDate(cal.getTime());
        }
    };
}
