package com.weqa.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.weqa.R;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.adapterdata.GrantedListData;
import com.weqa.model.adapterdata.GrantedListItem;
import com.weqa.model.json.CreateGuestInput;
import com.weqa.model.json.UpdateGuestInput;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.ui.GuestUserActivity;
import com.weqa.util.UIHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Manish Ballav on 10/13/2017.
 */

public class GrantedListAdapter  extends RecyclerView.Adapter<GrantedListAdapter.GrantedItemHolder> {

    private static final String LOG_TAG = "WEQA-LOG";
    private Dialog dialog;
    private ProgressBar progress1;
    private Date endDate = new Date();

    class GrantedItemHolder extends RecyclerView.ViewHolder {

        private TextView name, grantDate, mobile;
        private ImageView editButton;

        public GrantedItemHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.name);
            mobile = (TextView)itemView.findViewById(R.id.mobile);
            grantDate = (TextView)itemView.findViewById(R.id.grantDate);
            editButton = (ImageView) itemView.findViewById(R.id.editButton);
        }

    }

    protected LayoutInflater inflater;
    protected GrantedListData itemData;
    protected Activity c;

    public GrantedListAdapter(GrantedListData itemData, Activity c){
        inflater = LayoutInflater.from(c);
        this.itemData = itemData;
        this.c = c;
    }

    public void setItemData(GrantedListData itemData) {
        this.itemData = itemData;
    }

    @Override
    public GrantedListAdapter.GrantedItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.granted_list_item, parent, false);
        return new GrantedListAdapter.GrantedItemHolder(view);
    }

    @Override
    public void onBindViewHolder(GrantedListAdapter.GrantedItemHolder holder, final int position) {
        final GrantedListItem item = this.itemData.getListData().get(position);
        holder.name.setText(item.getFirstName() + " " + item.getLastName());
        holder.mobile.setText(item.getMobile());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
        String gDate = formatter.format(item.getEndDate());
        holder.grantDate.setText(gDate);
        holder.grantDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateDateDialog(item.getOrgUserId(), position);
            }
        });
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateDateDialog(item.getOrgUserId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.itemData.getListData().size();
    }

    private void showUpdateDateDialog(final int orgUserId, final int position) {

        dialog = new Dialog(c);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_datepicker);

        DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                endDate = cal.getTime();
            }
        });
        datePicker.setMinDate(System.currentTimeMillis());

        UIHelper.hideDay(datePicker);

        progress1 = (ProgressBar) dialog.findViewById(R.id.progress1);
        progress1.setVisibility(View.GONE);

        Button updateButton = (Button) dialog.findViewById(R.id.updateButton);
        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEndDate(orgUserId, position);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void updateEndDate(int orgUserId, final int position) {

        progress1.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to create guest access...");

        UpdateGuestInput input = new UpdateGuestInput();
        input.setOrgUserId(orgUserId);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String endDateString = format.format(endDate);
//        endDateString += " 00:00:00.000";
        input.setEndDate(endDateString);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<Void> call = service.updateGuestUser(input);

        CustomCallback<Void> customCallback = new CustomCallback<Void>(call.getUrl(), c) {
            @Override
            public void success(final Response<Void> response) {
                Log.d(LOG_TAG, "OUTPUT: Void");

                c.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterUpdateGuest(position);
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    private void afterUpdateGuest(int position) {
        progress1.setVisibility(View.GONE);
        dialog.dismiss();

        GrantedListItem item = itemData.getListData().get(position);
        item.setEndDate(endDate);

        this.notifyDataSetChanged();
    }
}
