package com.weqa.ui;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.weqa.R;
import com.weqa.adapter.OrgListAdapter;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.AuthInput;
import com.weqa.model.AuthResponse;
import com.weqa.model.CodeConstants;
import com.weqa.model.adapterdata.OrgListData;
import com.weqa.model.adapterdata.OrgListItem;
import com.weqa.model.json.ActivateOrgInput;
import com.weqa.model.json.ActivateOrgResponse;
import com.weqa.model.json.AddOrganizationInput;
import com.weqa.model.json.AddOrganizationResponse;
import com.weqa.model.json.ResendCodeOrgInput;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.SharedPreferencesUtil;
import com.weqa.util.ValidationUtil;
import com.weqa.util.ui.DialogUtil;
import com.weqa.util.ui.KeyboardUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import retrofit2.Response;
import retrofit2.Retrofit;

public class ConnectOrgActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String LOG_TAG = "WEQA-LOG";

    private RecyclerView orgList;
    private ImageView addOrgImage;
    private TextView infoText, resendCode;
    private Button addOrgButton, activateButton;
    private EditText orgMobile, activationCode;
    private RelativeLayout addOrgContainer;
    private LinearLayout codeContainer;
    private ProgressBar progress1, progress2, progress3;
    private Dialog dialog;

    private OrgListAdapter orgListAdapter;
    private OrgListData orgListData;
    private RelativeLayout progressBarContainer;

    private String orgMobileNo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_connect_org);

        orgList = (RecyclerView) findViewById(R.id.orgList);
        orgListData = new OrgListData(this);
        orgListAdapter = new OrgListAdapter(orgListData, this);

        LinearLayoutManager layoutManagerNew = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        orgList.setLayoutManager(layoutManagerNew);

        orgList.setAdapter(orgListAdapter);

        progressBarContainer = (RelativeLayout) findViewById(R.id.progressBarContainer);
        progressBarContainer.setVisibility(View.GONE);

        addOrgImage = (ImageView) findViewById(R.id.addOrgImage);

        infoText = (TextView) findViewById(R.id.info);

        resendCode = (TextView) findViewById(R.id.resendCode);
        activateButton = (Button) findViewById(R.id.activateButton);

        activateButton.setOnClickListener(this);
        resendCode.setOnClickListener(this);

        activationCode = (EditText) findViewById(R.id.activationCode);
        activationCode.clearFocus();

        codeContainer = (LinearLayout) findViewById(R.id.codeContainer);

        progress2 = (ProgressBar) findViewById(R.id.progress2);
        progress3 = (ProgressBar) findViewById(R.id.progress3);

        addOrgImage.setOnClickListener(this);

        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        container.setOnTouchListener(this);

        KeyboardUtil.hideSoftKeyboard(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.addOrgImage) {
            showAddOrganizationDialog();
        }
        else if (view.getId() == R.id.activateButton) {
            sendCode();
        }
        else if (view.getId() == R.id.resendCode) {
            resendCode();
        }
    }

    public void showProgressBar() {
        progressBarContainer.setVisibility(View.VISIBLE);
        orgList.setVisibility(View.GONE);
    }

    public void hideProgressBar() {
        progressBarContainer.setVisibility(View.GONE);
        orgList.setVisibility(View.VISIBLE);
    }

    private void showAddOrganizationDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.dialog_add_organization);

        orgMobile = (EditText) dialog.findViewById(R.id.orgMobile);

        Button connectButton = (Button) dialog.findViewById(R.id.addOrgButton);
        progress1 = (ProgressBar) dialog.findViewById(R.id.progress1);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = orgMobile.getText().toString();
                if (!ValidationUtil.isValidMobile(mobile)) {
                    orgMobile.setError("Invalid Mobile");
                }
                else {
                    addOrganization(mobile.replaceAll("[^\\d]", ""));
                }
            }
        });

        dialog.show();
    }

    private void addOrganization(String mobile) {

        progress1.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to add organization...");

        AddOrganizationInput input = new AddOrganizationInput();
        input.setMobileNo(mobile);
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<AddOrganizationResponse> addOrgCall = service.addOrganization(input);

        CustomCallback<AddOrganizationResponse> customCallback = new CustomCallback<AddOrganizationResponse>(addOrgCall.getUrl(), this) {
            @Override
            public void success(final Response<AddOrganizationResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                ConnectOrgActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterAddOrg(response.body());
                    }
                });
            }
        };

        addOrgCall.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void afterAddOrg(AddOrganizationResponse response) {

        orgMobileNo = orgMobile.getText().toString().replaceAll("[^\\d]", "");

        dialog.dismiss();

        if (response.getResponseCode().equals(CodeConstants.RC901)) {
            DialogUtil.showOkDialog(this, "Mobile number not found for any organization.", false);
        }
        else if (response.getResponseCode().equals(CodeConstants.RC903)) {

            DialogUtil.showOkDialog(this, this.getString(R.string.code_sent), false);

            infoText.setText(R.string.code_sent);

            infoText.setVisibility(View.VISIBLE);
            codeContainer.setVisibility(View.VISIBLE);
        }
        else if (response.getResponseCode().equals(CodeConstants.RC904)) {
            DialogUtil.showOkDialog(this, "Mobile number already connected with the user.", false);
        }
        else if (response.getResponseCode().equals(CodeConstants.RC906)) {
            DialogUtil.showOkDialog(this, "Another user is connected with the mobile number provided.", false);
        }
    }

    public void sendCode() {

        String code = activationCode.getText().toString();

        progress2.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to activate...");
        ActivateOrgInput input = new ActivateOrgInput();
        input.setActivationCode(code);
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<ActivateOrgResponse> call = service.activateOrganization(input);

        CustomCallback<ActivateOrgResponse> customCallback = new CustomCallback<ActivateOrgResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<ActivateOrgResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                ConnectOrgActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterActivate(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void afterActivate(ActivateOrgResponse response) {

        progress2.setVisibility(View.GONE);

        if (response.getResponseCode().equals(CodeConstants.RC03)) {

            if (orgListData.getListData().size() == 0) {
                authenticate();
                return;
            }

            // If the code sent was valid and organization has been activated
            OrgListItem item = new OrgListItem();
            item.setOrgName(response.getOrganizationName());
            item.setDefaultOrg(false);
            item.setOrganizationId(response.getOrganizationId());

            item.setPrivilege(response.getPrivilegeName());
            if (response.getPrivilegeName().equals("Guest")) {
                // TODO check date format of the output from activateOrganization and change format/length appropriately
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    if (response.getEndDate() != null && (response.getEndDate().length() >= 10))
                        item.setEndDate(dateFormat.parse(response.getEndDate().substring(0, 10)));
                }
                catch (ParseException pe) {
                    Log.d(LOG_TAG, "ConnectOrgActivity: Exception parsing date " + pe.getMessage());
                }
            }

            orgListData.addItem(item);
            orgListAdapter.notifyDataSetChanged();

            infoText.setVisibility(View.GONE);
            codeContainer.setVisibility(View.GONE);
        }
        else if (response.getResponseCode().equals(CodeConstants.RC07)) {
            // If the code sent has expired and new code has been sent by SMS and email.
            DialogUtil.showOkDialog(this, this.getString(R.string.new_code_sent), false);
            infoText.setText(R.string.new_code_sent);
        }
        else if (response.getResponseCode().equals(CodeConstants.RC08)) {
            // If the code entered was incorrect because of a typo or for whatever other reason
            DialogUtil.showOkDialog(this, this.getString(R.string.invalid_code), false);
            infoText.setText(R.string.invalid_code);
        }

    }

    public void resendCode() {

        progress3.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to resend activation code...");
        ResendCodeOrgInput input = new ResendCodeOrgInput();
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<Void> call = service.resendCodeOrg(input);

        CustomCallback<Void> customCallback = new CustomCallback<Void>(call.getUrl(), this) {
            @Override
            public void success(Response<Void> response) {
                Log.d(LOG_TAG, "OUTPUT: Void");
                ConnectOrgActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterResendCode();
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void afterResendCode() {
        progress3.setVisibility(View.GONE);
        infoText.setText(R.string.code_resent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.activateButton) {
            Button b = (Button) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                b.setBackgroundResource(R.drawable.super_rounded_button_yellow);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                b.setBackgroundResource(R.drawable.super_rounded_button_darkblue);
            }
            KeyboardUtil.hideSoftKeyboard(this);
        }
        else if (v.getId() == R.id.addOrgImage) {
            ImageView i = (ImageView) v;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                i.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorTABtext));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                i.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
            }
            KeyboardUtil.hideSoftKeyboard(this);
        }
        else {
            KeyboardUtil.hideSoftKeyboard(this);
        }
        return false;
    }

    private void authenticate() {

        showProgressBar();

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to authenticate...");
        AuthInput input = new AuthInput();
        input.setAuthenticationCode(CodeConstants.AC10);
        input.setAuthorizationCode(CodeConstants.AC20);
        input.setConfigurationCode(CodeConstants.AC30);
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        Gson gson = new Gson();
        String json = gson.toJson(input); // myObject - instance of MyObject
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<AuthResponse> authCall = service.auth(input);

        CustomCallback<AuthResponse> customCallback = new CustomCallback<AuthResponse>(authCall.getUrl(), this) {
            @Override
            public void success(final Response<AuthResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body()); // myObject - instance of MyObject
                Log.d(LOG_TAG, "OUTPUT: " + json);

                ConnectOrgActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(response.body());
                    }
                });
            }
        };

        authCall.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void updateUI(AuthResponse response) {

        hideProgressBar();

        SharedPreferencesUtil util = new SharedPreferencesUtil(this);

        util.addAuthTokens(response);

        Intent i = new Intent(this, LandingScreenActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(i);
    }


}
