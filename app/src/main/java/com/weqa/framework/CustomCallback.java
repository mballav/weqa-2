package com.weqa.framework;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import retrofit2.Response;

/**
 * Created by Manish Ballav on 10/12/2017.
 */

public abstract class CustomCallback<T> implements MyCallback<T> {

    static final String LOG_TAG = "WEQA-LOG";

    private String apiUrl;
    private Activity activity;

    public CustomCallback(String apiUrl, Activity activity) {
        this.apiUrl = apiUrl;
        this.activity = activity;
    }

    @Override
    public void unauthenticated(Response<?> response) {
        Log.d(LOG_TAG, "API: " + this.apiUrl + ": unauthenticated access denied by the server");
        displayErrorToast("");
    }

    @Override
    public void clientError(Response<?> response) {
        Log.d(LOG_TAG, "API: " + this.apiUrl + ": client error: " + response.code() + " " + response.message());
        displayErrorToast("");
    }

    @Override
    public void serverError(Response<?> response) {
        Log.d(LOG_TAG, "API: " + this.apiUrl + ": server error: " + response.code() + " " + response.message());
        displayErrorToast("");
    }

    @Override
    public void networkError(IOException e) {
        Log.d(LOG_TAG, "API: " + this.apiUrl + ": network error: " + e.getMessage());
        displayErrorToast("");
    }

    @Override public void unexpectedError(Throwable t) {
        Log.d(LOG_TAG, "API: " + this.apiUrl + ": fatal error: " + t.getMessage());
        displayErrorToast("");
    }

    private void displayErrorToast(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
