package com.weqa.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.weqa.R;
import com.weqa.model.FloorplanImageInput;
import com.weqa.service.RetrofitBuilder;
import com.weqa.util.BuildingUtil;
import com.weqa.util.SharedPreferencesUtil;
import com.weqa.util.async.FloorplanImageAsyncTask;

import retrofit2.Retrofit;

public class TestActivity extends AppCompatActivity implements FloorplanImageAsyncTask.UpdateImage {

    ImageView floorplan;
    boolean progress = false;
    private static final String LOG_TAG = "WEQA-LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button register = (Button) findViewById(R.id.register);
        final ProgressBar progress1 = (ProgressBar) findViewById(R.id.progress1);

        SharedPreferencesUtil util = new SharedPreferencesUtil(this);
        final BuildingUtil bUtil = new BuildingUtil(LOG_TAG, util, this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bUtil.testNearestBuilding();
                if (!progress) {
                    progress1.setVisibility(View.VISIBLE);
                    progress = true;
                }
                else {
                    progress1.setVisibility(View.GONE);
                    progress = false;
                }
            }
        });
    }

    private void getFloorplanImage() {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        FloorplanImageAsyncTask runner = new FloorplanImageAsyncTask(retrofit, LOG_TAG, this);
        FloorplanImageInput input = new FloorplanImageInput();
        input.setFloorPlanId(2);
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, input);
    }

    @Override
    public void updateUI(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Log.d(LOG_TAG, "Image Width: " + decodedByte.getWidth());
        Log.d(LOG_TAG, "Image Height: " + decodedByte.getHeight());
        floorplan.setImageBitmap(decodedByte);

        int width = floorplan.getWidth();
        int height = (int) (width*1.0*decodedByte.getHeight()/decodedByte.getWidth());
        floorplan.setMinimumHeight(height);
        floorplan.setMaxHeight(height);
    }

}
