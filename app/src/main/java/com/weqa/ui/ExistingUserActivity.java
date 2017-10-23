package com.weqa.ui;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.weqa.R;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.AuthInput;
import com.weqa.model.AuthResponse;
import com.weqa.model.CodeConstants;
import com.weqa.model.json.ExistingRegistrationInput;
import com.weqa.model.json.ExistingRegistrationResponse;
import com.weqa.model.json.RegistrationInput;
import com.weqa.model.json.RegistrationResponse;
import com.weqa.model.json.ResendCodeUserInput;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.SharedPreferencesUtil;
import com.weqa.util.ValidationUtil;
import com.weqa.util.ui.DialogUtil;
import com.weqa.util.ui.KeyboardUtil;

import retrofit2.Response;
import retrofit2.Retrofit;

public class ExistingUserActivity extends AppCompatActivity  implements View.OnTouchListener, View.OnClickListener {

    private static final String LOG_TAG = "WEQA-LOG";

    private Button register, activateButton;
    private TextView infoText, resendCode;
    private EditText code;
    private LinearLayout codeContainer;
    private ProgressBar progress1, progress2, progress3;
    private EditText mobile, deviceName;

    private String mobileNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_existing_user);

        mobile = (EditText) findViewById(R.id.mobile);
        deviceName = (EditText) findViewById(R.id.deviceName);

        register = (Button) findViewById(R.id.register);

        activateButton = (Button) findViewById(R.id.activateButton);
        resendCode = (TextView) findViewById(R.id.resendCode);

        infoText = (TextView) findViewById(R.id.info);
        code = (EditText) findViewById(R.id.activationCode);
        codeContainer = (LinearLayout) findViewById(R.id.codeContainer);

        progress1 = (ProgressBar) findViewById(R.id.progress1);
        progress2 = (ProgressBar) findViewById(R.id.progress2);
        progress3 = (ProgressBar) findViewById(R.id.progress3);

        Intent i = getIntent();

        boolean alreadyRegistered = i.getBooleanExtra("ALREADY_REGISTERED", false);

        if (alreadyRegistered)
            enableActivationCodeInput();
        else
            disableActivationCodeInput();

        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        container.setOnTouchListener(this);

        register.setOnClickListener(this);

        ImageView closeImage = (ImageView) findViewById(R.id.closeImage);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        String mobileNo = mobile.getText().toString();

        boolean errorFound = false;

        if (!ValidationUtil.isValidMobile(mobileNo)) {
            mobile.setError("Invalid Mobile");
            errorFound = true;
        }
        if (tooLong(deviceName.getText().toString())) { deviceName.setError("Too long"); errorFound = true; }

        if (!errorFound) register();
    }

    private boolean tooLong(String name) {
        if (name.length() > 200) {
            return true;
        }
        return false;
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
        else if (v.getId() == R.id.activationCode) {
            // TODO
        }
        else if (v.getId() == R.id.register) {
            Button b = (Button) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                b.setBackgroundResource(R.drawable.super_rounded_button_yellow);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                b.setBackgroundResource(R.drawable.super_rounded_button_darkblue);
            }
            KeyboardUtil.hideSoftKeyboard(this);
        }
        else {
            KeyboardUtil.hideSoftKeyboard(this);
        }
        return false;
    }

    private void register() {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        progress1.setVisibility(View.VISIBLE);
        register.setEnabled(false);
        register.setClickable(false);
        register.setBackgroundResource(R.drawable.super_rounded_button_darkgrey);

        mobile.addTextChangedListener(textWatcher);
        deviceName.addTextChangedListener(textWatcher);

        mobileNo = mobile.getText().toString().replaceAll("[^\\d]", "");

        Log.d(LOG_TAG, "Calling the API to register...");
        ExistingRegistrationInput input = new ExistingRegistrationInput();
        input.setMobileNo(mobileNo);
        input.setDeviceName(deviceName.getText().toString());
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<ExistingRegistrationResponse> call = service.existingRegister(input);

        CustomCallback<ExistingRegistrationResponse> customCallback = new CustomCallback<ExistingRegistrationResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<ExistingRegistrationResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                ExistingUserActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterRegistration(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void afterRegistration(ExistingRegistrationResponse response) {

        progress1.setVisibility(View.GONE);

        if (response.getCode().equals(CodeConstants.RC13)) {

            // User has been registered. No matching prior registration is found.
            // So, the user is new. Activation has been sent by SMS and E-mail.
            // Now, the mobile user should be prompted to enter the activation code.

            DialogUtil.showOkDialog(this, this.getString(R.string.code_sent), false);

            infoText.setText(this.getString(R.string.code_sent));
            enableActivationCodeInput();
        }
        else if (response.getCode().equals(CodeConstants.RC11)) {
            // Mobile number did not match any user records
            DialogUtil.showOkDialog(this, "Mobile number not found.", false);
        }
        else if (response.getCode().equals(CodeConstants.RC12)) {
            Intent i = new Intent(this, RegistrationActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }
    }

    private void disableActivationCodeInput() {
        infoText.setVisibility(View.GONE);
        codeContainer.setVisibility(View.GONE);
    }

    private void  enableActivationCodeInput() {

        infoText.setVisibility(View.VISIBLE);
        codeContainer.setVisibility(View.VISIBLE);

        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // send the activation code for device verification
                sendCode();
            }
        });
        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // re-send activation code to SMS and email
                resendCode();
            }
        });
    }

    public void sendCode() {

        String activationCode = code.getText().toString();

        progress2.setVisibility(View.VISIBLE);
        activateButton.setEnabled(false);
        activateButton.setClickable(false);
        activateButton.setBackgroundResource(R.drawable.super_rounded_button_darkgrey);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to activate user...");
        AuthInput input = new AuthInput();
        input.setAuthenticationCode(CodeConstants.AC10);
        input.setAuthorizationCode(CodeConstants.AC20);
        input.setConfigurationCode(CodeConstants.AC30);
        input.setUuid(InstanceIdService.getAppInstanceId(this));
        input.setActivationCode(activationCode);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<AuthResponse> call = service.auth(input);

        CustomCallback<AuthResponse> customCallback = new CustomCallback<AuthResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<AuthResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                ExistingUserActivity.this.runOnUiThread(new Runnable() {
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


    public void afterActivate(AuthResponse response) {

        progress2.setVisibility(View.GONE);

        SharedPreferencesUtil util = new SharedPreferencesUtil(this);

        if (response.getAuthenticationCode().equals(CodeConstants.RC03)) {
            // This code will always run on the UI thread, therefore is safe to modify UI elements.
            util.addAuthTokens(response);

            Intent i = new Intent(this, LandingScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }
        else if (response.getAuthenticationCode().equals(CodeConstants.RC06)) {
            util.addAuthTokens(response);

            Intent i = new Intent(this, ProfileActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }
        else if (response.getAuthenticationCode().equals(CodeConstants.RC07)) {
            // If the code sent has expired and new code has been sent by SMS and email.
            DialogUtil.showOkDialog(this, this.getString(R.string.new_code_sent), false);
            infoText.setText(R.string.new_code_sent);
            activateButton.setEnabled(true);
            activateButton.setClickable(true);
            activateButton.setBackgroundResource(R.drawable.super_rounded_button_darkblue);
        }
        else if (response.getAuthenticationCode().equals(CodeConstants.RC08)) {
            // If the code entered was incorrect because of a typo or for whatever other reason
            DialogUtil.showOkDialog(this, this.getString(R.string.invalid_code), false);
            infoText.setText(R.string.invalid_code);
            activateButton.setEnabled(true);
            activateButton.setClickable(true);
            activateButton.setBackgroundResource(R.drawable.super_rounded_button_darkblue);
        }

    }

    public void resendCode() {

        progress3.setVisibility(View.VISIBLE);
        resendCode.setClickable(false);
        resendCode.setEnabled(false);
        resendCode.setTextColor(ContextCompat.getColor(this, R.color.colorLightGrey));

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to resend activation code...");
        ResendCodeUserInput input = new ResendCodeUserInput();
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<Void> call = service.resendCodeUser(input);

        CustomCallback<Void> customCallback = new CustomCallback<Void>(call.getUrl(), this) {
            @Override
            public void success(Response<Void> response) {
                Log.d(LOG_TAG, "OUTPUT: VOID");

                ExistingUserActivity.this.runOnUiThread(new Runnable() {
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
        resendCode.setClickable(true);
        resendCode.setEnabled(true);
        resendCode.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            register.setEnabled(true);
            register.setClickable(true);
            register.setBackgroundResource(R.drawable.super_rounded_button_darkblue);

            mobile.removeTextChangedListener(textWatcher);
            deviceName.removeTextChangedListener(textWatcher);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
}
