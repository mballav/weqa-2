package com.weqa.util.async;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.weqa.model.json.DeleteTeamMemberInput;
import com.weqa.service.AuthService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by Manish Ballav on 9/27/2017.
 */

public class DeleteTeamMemberAsyncTask extends AsyncTask<Object, String, String> {

    public static interface UpdateUITeamMemberDelete {
        public void updateUIBeforeTeamMemberDelete();
        public void updateUIAfterTeamMemberDelete(int memberPosition);
    }

    public static final String STATUS_OK = "ok";
    public static final String STATUS_FAILURE = "not-ok";

    private Retrofit retrofit;
    private String logTag;
    private Activity activity;
    private String response;
    private int memberPosition;

    public DeleteTeamMemberAsyncTask(Retrofit retrofit, String logTAG, Activity activity, int memberPosition) {
        this.retrofit = retrofit;
        this.logTag = logTAG;
        this.activity = activity;
        this.memberPosition = memberPosition;
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            deleteTeamMember((DeleteTeamMemberInput) params[0]);
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

    private void deleteTeamMember(DeleteTeamMemberInput input) {
        AuthService service = retrofit.create(AuthService.class);
        Call<Void> call1 = service.deleteTeamMember(input);
        try {
            Log.e(logTag, "Retrofit call now...");
            Gson gson = new Gson();
            String inputJson = gson.toJson(input);
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

    @Override
    protected void onPreExecute() {
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((DeleteTeamMemberAsyncTask.UpdateUITeamMemberDelete) activity).updateUIBeforeTeamMemberDelete();
            }
        });
    }

    @Override
    protected void onPostExecute(String status) {

        final Activity a = this.activity;
        final Context context = this.activity.getApplication();

        if (status.equals(STATUS_OK)) {

            Log.d(logTag, "Output: " + response);
            Log.d(logTag, "Delete Team Member response received!");

            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((DeleteTeamMemberAsyncTask.UpdateUITeamMemberDelete) activity).updateUIAfterTeamMemberDelete(memberPosition);
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
