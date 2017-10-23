package com.weqa.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.weqa.model.CodeConstants;
import com.weqa.model.json.GetRegistrationInput;
import com.weqa.model.json.GetRegistrationResponse;
import com.weqa.model.json.Registration;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.async.AuthAsyncTask;
import com.weqa.util.async.AuthWithCodeAsyncTask;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.SharedPreferencesUtil;
import com.weqa.util.async.GetRegistrationAsyncTask;
import com.weqa.util.ui.KeyboardUtil;

import retrofit2.Response;
import retrofit2.Retrofit;

public class SplashScreenActivity extends AppCompatActivity implements AuthAsyncTask.UpdateUI {

    private static final String LOG_TAG = "WEQA-LOG";

    private Button newUser, existingUser;
    private SharedPreferencesUtil util;
    private ProgressBar progressBar;

    Thread thread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(3500); // As I am using LENGTH_LONG in Toast
                SplashScreenActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_splash_screen);

        if (android.os.Build.VERSION.SDK_INT < 21) {
            Toast.makeText(this, "This application requires newer version of OS than your android phone provides!", Toast.LENGTH_LONG).show();
            thread.start();
        }
        else {

            util = new SharedPreferencesUtil(this);

            newUser = (Button) findViewById(R.id.newUser);
            existingUser = (Button) findViewById(R.id.existingUser);

            newUser.setVisibility(View.GONE);
            existingUser.setVisibility(View.GONE);

        //TextView appslogan = (TextView) findViewById(R.id.appslogan);
        //appslogan.setTypeface(tf);

        /*
        LocationTracker tracker = new LocationTracker(this);
        // check if location is available
        if (tracker.isLocationEnabled()) {
            double lat1 = tracker.getLatitude();
            double lon1 = tracker.getLongitude();
            Toast.makeText(getApplicationContext(), "Lat: " + lat1 + ", Lon: " + lon1, Toast.LENGTH_LONG).show();
        } else {
            // show dialog box to user to enable location
            tracker.askToOnLocation();
        }
        */
            newUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(SplashScreenActivity.this, RegistrationActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            });

            existingUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(SplashScreenActivity.this, ExistingUserActivity.class);
                    i.putExtra("ALREADY_REGISTERED", false);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            });

        //It should show approx. 909 Km
        //Toast.makeText(getApplicationContext(), "Distance: " + LocationUtil.getDistance(50.3, -5.1, 58.4, -3.2), Toast.LENGTH_LONG).show();

        authenticate();
        }
    }

    private void authenticate() {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to authenticate...");
        AuthInput input = new AuthInput();
        input.setAuthenticationCode(CodeConstants.AC10);
        input.setAuthorizationCode(CodeConstants.AC20);
        input.setConfigurationCode(CodeConstants.AC30);
        input.setUuid(InstanceIdService.getAppInstanceId(SplashScreenActivity.this));

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

                SplashScreenActivity.this.runOnUiThread(new Runnable() {
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

    @Override
    public void updateUI(AuthResponse response) {

        if (response.getAuthenticationCode().equals(CodeConstants.RC01)) {
            // User has not registered and his device is new to the system
            newUser.setVisibility(View.VISIBLE);
            existingUser.setVisibility(View.VISIBLE);
        }
        else if (response.getAuthenticationCode().equals(CodeConstants.RC03)) {
            // successful case where in registraion done, device verified, and one of the org mobile matched,
            // so the user is connected to an organization as well.
            util.addAuthTokens(response);

            Intent i = new Intent(this, LandingScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }
        else if (response.getAuthenticationCode().equals(CodeConstants.RC06)) {
            // User is registered and the device is verified
            // But organization has provided a different number than the mobile
            // number used for registration. So, direct him to add organization
            // page
            util.addAuthTokens(response);

            Intent i = new Intent(this, ProfileActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }
        else if (response.getAuthenticationCode().equals(CodeConstants.RC09)) {
            // User has registered, but not verified his device
            Intent i = new Intent(SplashScreenActivity.this, RegistrationActivity.class);
            i.putExtra("FIRST_NAME", response.getResponseUser().getFirstName());
            i.putExtra("LAST_NAME", response.getResponseUser().getLastName());
            i.putExtra("EMAIL", response.getResponseUser().getEmail());
            i.putExtra("MOBILE", response.getResponseUser().getMobileNo());
            i.putExtra("DEVICE_NAME", response.getResponseUser().getDeviceName());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        else if (response.getAuthenticationCode().equals(CodeConstants.RC10)) {
            // User is registered and the device is verified
            // But organization has provided a different number than the mobile
            // number used for registration. So, direct him to add organization
            // page
            Intent i = new Intent(this, ExistingUserActivity.class);
            i.putExtra("ALREADY_REGISTERED", true);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
