package com.weqa.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.weqa.R;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.AuthInput;
import com.weqa.model.AuthResponse;
import com.weqa.model.Authentication;
import com.weqa.model.CodeConstants;
import com.weqa.model.Org;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.SharedPreferencesUtil;

import net.glxn.qrgen.android.QRCode;

import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static String LOG_TAG = "WEQA-LOG";

    private String screenFrom;

    private ProgressBar progress1;

    private SharedPreferencesUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_profile);

        Intent i = getIntent();
        screenFrom = i.getStringExtra("SCREEN_FROM");

        Typeface tf = Typeface.createFromAsset(this.getAssets(), "font/HelveticaNeueMed.ttf");

        util = new SharedPreferencesUtil(this, LOG_TAG);
        Authentication authentication = util.getAuthenticationInfo();

        String[] nameTokens = authentication.getEmployeeName().split(" ");
        String mobile = authentication.getMobileNo();

        String firstName = "";
        String lastName = "";
        if (nameTokens.length == 1) {
            firstName = nameTokens[0];
        }
        else if (nameTokens.length > 1){
            firstName = nameTokens[0];
            lastName = nameTokens[1];
        }

        String email = getEmail(authentication);
        String designation = getDesignation(authentication);

        String qrCodeText = CodeConstants.QR_CODE_MEMBER + "," + InstanceIdService.getAppInstanceId(this)
                            + "," + getOrgIdString(authentication)
                            + "," + firstName + "," + lastName
                            + "," + designation + "," + mobile;
        Log.d(LOG_TAG, "Profile QR Code: " + qrCodeText);
        Bitmap bm = QRCode.from(qrCodeText).bitmap();

        float bitmapWidth = bm.getWidth();
        float bitmapHeight = bm.getHeight();

        Log.d(LOG_TAG, "Bitmap width = " + bitmapWidth + ", height = " + bitmapHeight);

        ImageView qrCodeImage = (ImageView) findViewById(R.id.qrCode);
        qrCodeImage.setImageBitmap(bm);

        TextView nameTextView = (TextView) findViewById(R.id.nameText);
        nameTextView.setText(authentication.getEmployeeName());

        nameTextView.setTypeface(tf);

        TextView emailTextView = (TextView) findViewById(R.id.emailText);
        emailTextView.setText(email);

        TextView mobileTextView = (TextView) findViewById(R.id.mobileText);
        mobileTextView.setText(mobile);

        Button doneButton = (Button) findViewById(R.id.doneButton);

        if (authentication.getOrganization() == null || authentication.getOrganization().size() == 0) {
            doneButton.setVisibility(View.GONE);
        }
        else {
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (screenFrom != null &&
                            (screenFrom.equals("TeamSummary") || screenFrom.equals("Landing"))) {
                        Intent intent = new Intent();
                        setResult(20, intent);
                    }
                    finish();
                }
            });
            doneButton.setOnTouchListener(this);
        }

        RelativeLayout orgContainer = (RelativeLayout) findViewById(R.id.orgContainer);

        orgContainer.setOnClickListener(this);

        progress1 = (ProgressBar) findViewById(R.id.progress1);
        progress1.setVisibility(View.GONE);
    }

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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.orgContainer) {
            authenticate();
        }
    }

    private void authenticate() {
        progress1.setVisibility(View.VISIBLE);

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

                ProfileActivity.this.runOnUiThread(new Runnable() {
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

            progress1.setVisibility(View.GONE);

            util.addAuthTokens(response);

            Intent i = new Intent(this, ConnectOrgActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
    }


    private String getEmail(Authentication auth) {
        if (auth == null)
            return "";

        String email = auth.getEmailId();
        if (email == null)
            email = "";
        return email;
    }

    private String getOrgEmail(Authentication auth) {
        if (auth.getOrganization() == null || auth.getOrganization().size() == 0)
            return "";

        String email = auth.getOrganization().get(0).getEmailId();
        if (email == null || email.equals(""))
            email = "";
        return email;
    }

    private String getOrgIdString(Authentication auth) {
        if (auth.getOrganization() == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer("");
        boolean firstTime = true;
        for (Org o : auth.getOrganization()) {
            if (!firstTime) {
                sb.append("_");
            }
            sb.append(o.getOrganizationId());
            firstTime = false;
        }
        return sb.toString();
    }

    private String getDesignation(Authentication auth) {
        if (auth.getOrganization() == null || auth.getOrganization().size() == 0)
            return "";

        String designation = auth.getOrganization().get(0).getDesignation();
        if (designation == null || designation.equals(""))
            designation = "";
        return designation;
    }
}
