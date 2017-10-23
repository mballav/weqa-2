package com.weqa.util.async;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.weqa.model.json.CreateTeamInput;
import com.weqa.service.AuthService;
import com.weqa.util.SharedPreferencesUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by Manish Ballav on 9/21/2017.
 */

public class CreateTeamAsyncTask extends AsyncTask<Object, String, String> {

    public static interface UpdateUI {
        public void updateUI();
    }

    public static final String STATUS_OK = "ok";
    public static final String STATUS_FAILURE = "not-ok";

    private Retrofit retrofit;
    private String logTag;
    private Activity activity;
    private String response;
    private SharedPreferencesUtil sharedPrefUtil;

    public CreateTeamAsyncTask(Retrofit retrofit, String logTAG, Activity activity) {
        this.retrofit = retrofit;
        this.logTag = logTAG;
        this.activity = activity;
        sharedPrefUtil = new SharedPreferencesUtil(this.activity.getApplicationContext());
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            createTeam((CreateTeamInput) params[0]);
        } catch (Exception e) {
            Log.d(logTag, "Error in async task " + e.getMessage());
            final Context context = this.activity.getApplication();
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Please try again later!", Toast.LENGTH_LONG).show();
                }
            });
            return STATUS_FAILURE;
        }
        return STATUS_OK;
    }

    private void createTeam(CreateTeamInput input) {
        AuthService service = retrofit.create(AuthService.class);
        Call<Void> call1 = service.createTeam(input);
        try {
            Log.e(logTag, "Retrofit call now...");
            Gson gson = new Gson();
            final String inputJson = gson.toJson(input);
            Log.d(logTag, "Input: " + inputJson);

            call1.execute().body();
        }
        catch (IOException ioe) {
            Log.d(logTag, "Error in retrofit call" + ioe.getMessage());
            final Context context = this.activity.getApplication();
/*            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Connectivity Problem. Please try again later!", Toast.LENGTH_LONG).show();
                }
            });*/
        }
    }

    private void saveToFile(String s) {
        try {
            File f = new File(Environment.getExternalStorageDirectory(),
                    "Pictures/json.txt");

            File picturesDir = new File(Environment.getExternalStorageDirectory(), "Pictures");

            if (!picturesDir.exists()) {
                if (!picturesDir.mkdirs()) {
                    Log.e(logTag, "Directory not created");
                }
            }

            FileWriter fos = new FileWriter(f);
            fos.write(s);
            fos.close();
        } catch (IOException ioe) {
            Log.d(logTag, "Problem saving file! " + ioe.getMessage());
        }

    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(String status) {

        final Activity a = this.activity;
        final Context context = this.activity.getApplication();

        if (status.equals(STATUS_OK)) {

            Log.d(logTag, "Create team response received!");

            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((CreateTeamAsyncTask.UpdateUI) activity).updateUI();
                }
            });
        }
        else {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Problem with connectivity... exiting!", Toast.LENGTH_LONG).show();
                    activity.finish();
                }
            });
        }
    }

}
