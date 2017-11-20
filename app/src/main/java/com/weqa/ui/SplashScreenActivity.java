package com.weqa.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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
import com.weqa.util.ui.DialogUtil;
import com.weqa.util.ui.KeyboardUtil;

import java.util.ArrayList;

import retrofit2.Response;
import retrofit2.Retrofit;

public class SplashScreenActivity extends AppCompatActivity implements AuthAsyncTask.UpdateUI {

    private static final String LOG_TAG = "WEQA-LOG";

    private Button newUser, existingUser;
    private SharedPreferencesUtil util;
    private ProgressBar progressBar;

    Thread thread = new Thread() {
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
            Toast.makeText(this, R.string.os_version_old, Toast.LENGTH_LONG).show();
            thread.start();
        } else {

            util = new SharedPreferencesUtil(this);

            newUser = (Button) findViewById(R.id.newUser);
            existingUser = (Button) findViewById(R.id.existingUser);

            newUser.setVisibility(View.GONE);
            existingUser.setVisibility(View.GONE);

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

            requestPermissions();

            if (!isLocationEnabled()) {
                showOkDialog(this.getString(R.string.turn_on_location));
            }
            else if (!areNotificationsEnabled()) {
                showOkDialog(this.getString(R.string.enable_notification));
            }
            else if (!isConnected()) {
                showOkDialog(this.getString(R.string.turn_on_internet));
            }

            authenticate();
        }
    }

    public void showOkDialog(String textToDisplay) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.bookingmessage);
        text.setText(textToDisplay);


        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.GONE);

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        // if button is clicked, close the custom dialog
        okButton.setText("Close");
        okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    SplashScreenActivity.this.finish();
                }
            });

        dialog.show();
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
        } else if (response.getAuthenticationCode().equals(CodeConstants.RC03)) {
            // successful case where in registraion done, device verified, and one of the org mobile matched,
            // so the user is connected to an organization as well.
            util.addAuthTokens(response);

            Intent i = new Intent(this, LandingScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        } else if (response.getAuthenticationCode().equals(CodeConstants.RC06)) {
            // User is registered and the device is verified
            // But organization has provided a different number than the mobile
            // number used for registration. So, direct him to add organization
            // page
            util.addAuthTokens(response);

            Intent i = new Intent(this, ProfileActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        } else if (response.getAuthenticationCode().equals(CodeConstants.RC09)) {
            // User has registered, but not verified his device
            Intent i = new Intent(SplashScreenActivity.this, RegistrationActivity.class);
            i.putExtra("FIRST_NAME", response.getResponseUser().getFirstName());
            i.putExtra("LAST_NAME", response.getResponseUser().getLastName());
            i.putExtra("EMAIL", response.getResponseUser().getEmail());
            i.putExtra("MOBILE", response.getResponseUser().getMobileNo());
            i.putExtra("DEVICE_NAME", response.getResponseUser().getDeviceName());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else if (response.getAuthenticationCode().equals(CodeConstants.RC10)) {
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

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private boolean areNotificationsEnabled() {
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    private boolean isLocationEnabled() {

        LocationManager locationManager=(LocationManager) this.getSystemService(LOCATION_SERVICE);
        //check for gps availability
        boolean isGPSOn=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //check for network availablity
        boolean isNetWorkEnabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGPSOn && !isNetWorkEnabled)
        {
            return false;
        }
        else {
            return true;
        }
    }

    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 102;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 103;

    private void requestPermissions() {

        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.CAMERA);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        if(Manifest.permission.CAMERA.equals(permission)) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                // you do not have permission
                                Toast.makeText(this, R.string.no_access_camera, Toast.LENGTH_LONG).show();
                                thread.start();
                                break;

                            }
                        }
                        if(Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                // you do not have permission
                                Toast.makeText(this, R.string.no_access_location, Toast.LENGTH_LONG).show();
                                thread.start();
                                break;
                            }
                        }
                        if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                // you do not have permission
                                Toast.makeText(this, R.string.no_access_storage, Toast.LENGTH_LONG).show();
                                thread.start();
                                break;
                            }
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, R.string.no_access_camera, Toast.LENGTH_LONG).show();
                    thread.start();
                }
                break;
            }
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }


    private void requestPermissionForCamera() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
        else {
            requestPermissionForLocation();
        }
    }

    private void requestPermissionForLocation() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else {
            requestPermissionForStorage();
        }

    }

    private void requestPermissionForStorage() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.no_access_camera, Toast.LENGTH_LONG).show();
                        thread.start();
                }
                else {
                    requestPermissionForLocation();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.no_access_location, Toast.LENGTH_LONG).show();
                        thread.start();
                }
                else {
                    requestPermissionForStorage();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.no_access_storage, Toast.LENGTH_LONG).show();
                        thread.start();
                }
                return;
            }
        }
    }*/
}
