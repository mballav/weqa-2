package com.weqa.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.weqa.R;
import com.weqa.adapter.AvailListAdapter;
import com.weqa.framework.CustomCallback;
import com.weqa.framework.MyCall;
import com.weqa.model.Authentication;
import com.weqa.model.Authorization;
import com.weqa.model.BookingInput;
import com.weqa.model.BookingReleaseInput;
import com.weqa.model.BookingResponse;
import com.weqa.model.CodeConstants;
import com.weqa.model.FloorPlan;
import com.weqa.model.FloorPlanDetailV2;
import com.weqa.model.FloorplanInputV2;
import com.weqa.model.FloorplanResponseV2;
import com.weqa.model.HotspotCenter;
import com.weqa.model.ItemTypeDetailV2;
import com.weqa.model.Org;
import com.weqa.model.ResponseOrgBasedItemType;
import com.weqa.model.adapterdata.AvailListData;
import com.weqa.model.adapterdata.AvailListItem;
import com.weqa.service.InstanceIdService;
import com.weqa.service.RetrofitBuilder;
import com.weqa.service.RetrofitService;
import com.weqa.util.AuthorizationUtil;
import com.weqa.util.MemoryUtil;
import com.weqa.util.async.BookingAsyncTask;
import com.weqa.util.async.BookingReleaseAsyncTask;
import com.weqa.util.async.BookingRenewAsyncTask;
import com.weqa.util.BuildingUtil;
import com.weqa.util.DatetimeUtil;
import com.weqa.util.ui.DialogUtil;
import com.weqa.util.async.FloorplanV2AsyncTask;
import com.weqa.util.GlobalExceptionHandler;
import com.weqa.util.QRCodeUtil;
import com.weqa.util.SharedPreferencesUtil;
import com.weqa.util.UIHelper;
import com.weqa.widget.SearchableSpinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LandingScreenActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener,
        FloorplanV2AsyncTask.UpdateFloorplan, AdapterView.OnItemSelectedListener,
        AvailListAdapter.OnAvailListClickListener {

    private static String LOG_TAG = "WEQA-LOG";

    private TabLayout mTabLayout;

    private final static int[] itemTypeColors = {R.color.colorDarkGreen, R.color.colorDarkGreen,
            R.color.colorDarkGreen, R.color.colorDarkGreen};
    private byte[] decodedString;
    private String floorLevel;
    private Map<Integer, String> locationsMap = new HashMap<Integer, String>();

    public static Typeface fontAwesome, fontLatoReg, fontLatoBlack, fontLatoLight;

    private SearchableSpinner spinner;
    private List items;
    private SharedPreferencesUtil util;
    private Authorization selectedBuilding;
    private long selectedBuildingId;
    private long selectedFloorplanId;
    private int defaultOrgId;

    private List<Authorization> compiledAuthList;

    private PhotoView floorplan;
    private TextView floorNumberText, listText;
    private ImageView listIcon;

    private int originalBitmapWidth, originalBitmapHeight;
    private float hotspotSize;
    private List<HotspotCenter> hotspotCenters = new ArrayList<HotspotCenter>();

    private boolean listenerAdded = false;
    private boolean buildingChanged = false;
    private boolean orgChanged = false;
    private Map<Integer, Integer> itemTypeIdMap = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> itemCountMap = new HashMap<Integer, Integer>();

    private RelativeLayout mapViewContainer, listContainer, progressBarContainer;
    private RecyclerView availList;
    private AvailListData availData;

    private List<Authorization> authListOriginal;
    private static String TEST_QR_CODE = "2,1,1,2017-09-02 00:00:40";
    private static String TEST_QR_CODE_2 = "2,1,1,2017-09-02 00:00:60";
    private static String TEST_QR_CODE_3 = "2,1,4,2017-09-02 00:00:30";

    private static enum ViewType { LIST, MAP };

    private ViewType currentView = ViewType.MAP;
    private int currentTabIndex = 0;

    private Map<Integer, Integer> itemTypeInverseIdMap = new HashMap<Integer, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
        setContentView(R.layout.activity_landing_screen);

        fontAwesome = Typeface.createFromAsset(this.getAssets(), "font/fontawesome-webfont.ttf");
        fontLatoBlack = Typeface.createFromAsset(this.getAssets(), "font/Lato-Black.ttf");
        fontLatoLight = Typeface.createFromAsset(this.getAssets(), "font/Lato-Light.ttf");
        fontLatoReg = Typeface.createFromAsset(this.getAssets(), "font/Lato-Regular.ttf");

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        floorNumberText = (TextView) findViewById(R.id.floorNumber);
        spinner = (SearchableSpinner) findViewById(R.id.spinner);

        mapViewContainer = (RelativeLayout) findViewById(R.id.mapViewContainer);
        listContainer = (RelativeLayout) findViewById(R.id.listContainer);
        progressBarContainer = (RelativeLayout) findViewById(R.id.progressBarContainer);
        availList = (RecyclerView) findViewById(R.id.availabilityList);

        LinearLayoutManager layoutManagerNew = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        availList.setLayoutManager(layoutManagerNew);

        listContainer.setVisibility(View.GONE);

        spinner.setPositiveButton("OK");
        spinner.setTitle("Buildings");

        util = new SharedPreferencesUtil(this);
        authListOriginal = util.getAuthorizationInfo();
        defaultOrgId = util.getDefaultOrganization();
        Log.d(LOG_TAG, "Default Org ID from user preferences is " + defaultOrgId);

        compiledAuthList = AuthorizationUtil.filterBuildings(authListOriginal, defaultOrgId);

        items = new ArrayList();
        for (Authorization a : compiledAuthList) {
            String bName = AuthorizationUtil.getBuildingDisplayName(a);
            items.add(bName);
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.spinner_item, items);
        spinner.setAdapter(arrayAdapter);

        BuildingUtil buildingUtil = new BuildingUtil(LOG_TAG, util, this);
        selectedBuilding = buildingUtil.getBuildingForSearchBar(compiledAuthList);
        selectedBuildingId = Integer.parseInt(selectedBuilding.getBuildingId());

        int index = compiledAuthList.indexOf(selectedBuilding);
        spinner.setSelection(index);

        spinner.setOnItemSelectedListener(this);

        floorplan = (PhotoView) findViewById(R.id.floorplan);

        CircleImageView profilePicture = (CircleImageView) findViewById(R.id.profilepicture);
        profilePicture.setOnClickListener(this);

        LinearLayout menu1 = (LinearLayout) findViewById(R.id.menu1);
        LinearLayout menu2 = (LinearLayout) findViewById(R.id.menu2);
        LinearLayout menu3 = (LinearLayout) findViewById(R.id.menu3);
        LinearLayout menu4 = (LinearLayout) findViewById(R.id.menu4);
        LinearLayout menu5 = (LinearLayout) findViewById(R.id.menu5);

        menu1.setOnClickListener(this);
        menu2.setOnClickListener(this);
        menu3.setOnClickListener(this);
        menu4.setOnClickListener(this);
        menu5.setOnClickListener(this);

        menu1.setOnTouchListener(this);
        menu2.setOnTouchListener(this);
        menu3.setOnTouchListener(this);
        menu4.setOnTouchListener(this);
        menu5.setOnTouchListener(this);

        TextView homeText = (TextView) findViewById(R.id.textView1);
        homeText.setTextColor(ContextCompat.getColor(this, R.color.colorTABtextSelected));

        listText = (TextView) findViewById(R.id.listtext);
        listIcon = (ImageView) findViewById(R.id.listicon);

        RelativeLayout tapToEnlarge = (RelativeLayout) findViewById(R.id.taptoenlarge);
        tapToEnlarge.setOnTouchListener(this);
        tapToEnlarge.setOnClickListener(this);

        updateUI();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.getId() == R.id.taptoenlarge) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                listText.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtext));
                listIcon.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorTABtext));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                listText.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
                listIcon.setColorFilter(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
            }
            return false;
        }
        else if (v.getId() == R.id.floorNumber) {
            TextView t = (TextView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                t.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorTOP));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                t.setTextColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
            }
            return true;
        }
        else {
            LinearLayout l = (LinearLayout) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                l.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorTABtextSelected));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                l.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorMENU));
            }
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.menu3) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        } else if (v.getId() == R.id.taptoenlarge) {
            if (listContainer.getVisibility() == View.GONE) {
                mapViewContainer.setVisibility(View.GONE);
                listContainer.setVisibility(View.VISIBLE);
                currentView = ViewType.LIST;
            }
        }
        else if (v.getId() == R.id.profilepicture) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("SCREEN_FROM", "Landing");
            i.setFlags(0);
            this.startActivityForResult(i, 20);
        }
        else if (v.getId() == R.id.menu4) {
            Intent i = new Intent(this, TeamSummaryActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }
        else if (v.getId() != R.id.menu1){
            Toast.makeText(v.getContext(), "Under Development", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int index = compiledAuthList.indexOf(selectedBuilding);
        if (index == position) return;

        selectedBuilding = compiledAuthList.get(position);
        selectedBuildingId = Integer.parseInt(selectedBuilding.getBuildingId());
        updateUI();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateTabLayoutMode();
    }

    private void updateTabLayoutMode() {

        mTabLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        ViewTreeObserver observer = mTabLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                int tabLayoutWidth = mTabLayout.getWidth();

                DisplayMetrics metrics = new DisplayMetrics();
                LandingScreenActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int deviceWidth = metrics.widthPixels;

                Log.d(LOG_TAG, "TabLayout WIDTH : " + tabLayoutWidth + ", Device Width: " + deviceWidth);
                if (tabLayoutWidth < deviceWidth) {
                    mTabLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    mTabLayout.setTabMode(TabLayout.MODE_FIXED);
                    mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                } else {
                    mTabLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                }
                mTabLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    public void updateUI() {
        buildingChanged = true;
        if (mapViewContainer.getVisibility() == View.VISIBLE)
            mapViewContainer.setVisibility(View.GONE);
        if (listContainer.getVisibility() == View.VISIBLE)
            listContainer.setVisibility(View.GONE);
        progressBarContainer.setVisibility(View.VISIBLE);
        fetchFloorplanAndAvailability();
    }

    public void updateBuildingAndFloorplanAvailability() {
        if (mapViewContainer.getVisibility() == View.VISIBLE)
            mapViewContainer.setVisibility(View.GONE);
        if (listContainer.getVisibility() == View.VISIBLE)
            listContainer.setVisibility(View.GONE);
        progressBarContainer.setVisibility(View.VISIBLE);

        int oldDefaultOrgId = defaultOrgId;
        defaultOrgId = util.getDefaultOrganization();

        if (oldDefaultOrgId != defaultOrgId) {
            compiledAuthList = AuthorizationUtil.filterBuildings(authListOriginal, defaultOrgId);

            items = new ArrayList();
            for (Authorization a : compiledAuthList) {
                String bName = AuthorizationUtil.getBuildingDisplayName(a);
                items.add(bName);
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.spinner_item, items);
            spinner.setAdapter(arrayAdapter);

            BuildingUtil buildingUtil = new BuildingUtil(LOG_TAG, util, this);
            selectedBuilding = buildingUtil.getBuildingForSearchBar(compiledAuthList);
            selectedBuildingId = Integer.parseInt(selectedBuilding.getBuildingId());

            int index = compiledAuthList.indexOf(selectedBuilding);
            spinner.setSelection(index);

            orgChanged = true;

            fetchFloorplanAndAvailability();
        }
        else {
            fetchAvailabilityForCurrentFloorplan();
        }
    }

    public void updateFloorplanAvailability() {
        if (mapViewContainer.getVisibility() == View.VISIBLE)
            mapViewContainer.setVisibility(View.GONE);
        if (listContainer.getVisibility() == View.VISIBLE)
            listContainer.setVisibility(View.GONE);
        progressBarContainer.setVisibility(View.VISIBLE);

        fetchAvailabilityForCurrentFloorplan();
    }

    @Override
    public void downloadAndUpdateFloorplan(long floorNumber) {
        if (mapViewContainer.getVisibility() == View.VISIBLE)
            mapViewContainer.setVisibility(View.GONE);
        if (listContainer.getVisibility() == View.VISIBLE)
            listContainer.setVisibility(View.GONE);
        progressBarContainer.setVisibility(View.VISIBLE);

        long floorplanId = 0;
        for (Authorization a : authListOriginal) {
            if (Integer.parseInt(a.getFloorLevel()) == floorNumber
                    && Integer.parseInt(a.getBuildingId()) == selectedBuildingId) {
                floorplanId = Long.parseLong(a.getFloorPlanId());
            }
        }

        selectedFloorplanId = floorplanId;
        currentView = ViewType.MAP;

        fetchFloorplanAndAvailability(floorplanId);
    }

    @Override
    public void updateFloorplan(FloorplanResponseV2 fr) {

        Log.d(LOG_TAG, "Inside updateFloorplan - 1");

        FloorPlanDetailV2 f = fr.getFloorPlanDetails().get(0);
        selectedFloorplanId = f.getFloorPlanId().longValue();
        floorLevel = util.getFloorLevel(selectedBuildingId, f.getFloorPlanId().longValue());

        if (orgChanged) {
            mTabLayout.clearOnTabSelectedListeners();
            listenerAdded = false;
            orgChanged = false;
        }

        Log.d(LOG_TAG, "Inside updateFloorplan - 2");
        if (!listenerAdded) {
            mTabLayout.removeAllTabs();
            for (ResponseOrgBasedItemType ri : fr.getResponseOrgBasedItemType()) {
                mTabLayout.addTab(mTabLayout.newTab().setText(ri.getItemType()));
            }
            int i = 0;
            itemTypeIdMap.clear();
            for (ResponseOrgBasedItemType ri : fr.getResponseOrgBasedItemType()) {
                itemTypeIdMap.put(i, ri.getItemTypeId());
                itemTypeInverseIdMap.put(ri.getItemTypeId(), i);
                i++;
            }

        }

        locationsMap.clear();
        for (Integer key : itemTypeIdMap.keySet()) {
            Integer itemTypeId = itemTypeIdMap.get(key);
            String locations = "";
            int itemCount = 0;
            for (ItemTypeDetailV2 itd : f.getItemTypeDetail()) {
                if (itd.getItemTypeId().equals(itemTypeId)) {
                    locations = itd.getImageLocation();
                    itemCount = itd.getItemCount();
                    break;
                }
            }
            locationsMap.put(key, locations);
            itemCountMap.put(key, itemCount);
        }
        Log.d(LOG_TAG, "Inside updateFloorplan - 3");

        if (buildingChanged) {
            availData = new AvailListData(fr, util, selectedBuildingId);
            buildingChanged = false;
        }
        else {
            availData.update(fr);
        }
        int itemTypeId = itemTypeIdMap.get(currentTabIndex);
        List<AvailListItem> items = availData.getListData(itemTypeId);
        AvailListAdapter adapter = new AvailListAdapter(items, this, this);
        availList.setAdapter(adapter);

        Log.d(LOG_TAG, "Inside updateFloorplan - 4");
        decodedString = null;
        if (f.getFloorImage() != null && f.getFloorImage().length() > 0) {
            String base64Image = f.getFloorImage();
            decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        } else {
            try {
                File floorplanFile = new File(Environment.getExternalStorageDirectory(),
                        "Pictures/floorplan_" + selectedBuildingId + "_" + f.getFloorPlanId().intValue());
                FileInputStream fis = new FileInputStream(floorplanFile);

                decodedString = new byte[(int) (floorplanFile.length())];

                fis.read(decodedString, 0, (int) (floorplanFile.length()));
                fis.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        Log.d(LOG_TAG, "Inside updateFloorplan - 5");


        if (decodedString != null) {
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            Log.d(LOG_TAG, "Inside updateFloorplan(): decodedString.size() = " + decodedString.length);

            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            Log.d(LOG_TAG, "Image Width: " + decodedByte.getWidth());
            Log.d(LOG_TAG, "Image Height: " + decodedByte.getHeight());

            originalBitmapWidth = decodedByte.getWidth();
            originalBitmapHeight = decodedByte.getHeight();

            hotspotSize = Math.min(originalBitmapWidth, originalBitmapHeight) / 15.0f;

            floorplan.setImageBitmap(decodedByte);

            mTabLayout.getTabAt(currentTabIndex).select();
            floorNumberText.setText("Floor " + floorLevel + " (" + itemCountMap.get(currentTabIndex) + " available)");

            String locations = (locationsMap.get(currentTabIndex) == null) ? "" : locationsMap.get(currentTabIndex);
            updateFloorplanWithHotspots(locations, itemTypeColors[currentTabIndex]);

            if (!listenerAdded) {
                mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        currentTabIndex = position;

                        String locations = (locationsMap.get(position) == null) ? "" : locationsMap.get(position);
                        updateFloorplanWithHotspots(locations, itemTypeColors[position]);
                        floorNumberText.setText("Floor " + floorLevel + " (" + itemCountMap.get(position) + " available)");

                        List<AvailListItem> items = availData.getListData(itemTypeIdMap.get(position));
                        AvailListAdapter adapter = new AvailListAdapter(items, LandingScreenActivity.this, LandingScreenActivity.this);
                        availList.setAdapter(adapter);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });
                listenerAdded = true;
            }

            UIHelper.changeTabsFont(mTabLayout, fontLatoReg);

            updateTabLayoutMode();

            if (currentView == ViewType.MAP) {
                if (listContainer.getVisibility() == View.VISIBLE)
                    listContainer.setVisibility(View.GONE);
                if (mapViewContainer.getVisibility() == View.GONE)
                    mapViewContainer.setVisibility(View.VISIBLE);
            } else {
                if (mapViewContainer.getVisibility() == View.VISIBLE)
                    mapViewContainer.setVisibility(View.GONE);
                if (listContainer.getVisibility() == View.GONE)
                    listContainer.setVisibility(View.VISIBLE);
            }
            progressBarContainer.setVisibility(View.GONE);

        } else {
            Toast.makeText(this, "Fatal Error... exiting!", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20) {
            updateBuildingAndFloorplanAvailability();
        }
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();QRCodeUtil qrCodeUtil = new QRCodeUtil(util, this);
/*                if (qrCodeUtil.isQRCodeValid(TEST_QR_CODE)) {
                    bookQRCodeItem(TEST_QR_CODE);
                }
                else {
                    DialogUtil.showOkDialog(this, "Invalid QR Code! Code: " + TEST_QR_CODE, false, false);
                } */
                } else {
                    String qrCode = result.getContents();
                    QRCodeUtil qrCodeUtil = new QRCodeUtil(util, this);
                    if (qrCodeUtil.isQRItemCodeValid(qrCode)) {
                        bookQRCodeItem(qrCode);
                    }
                    else {
                        DialogUtil.showOkDialog(this, "Invalid WEQA Code", false, false);
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void bookQRCodeItem(final String qrCode) {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();
        RetrofitService service = retrofit.create(RetrofitService.class);

        String qrCodeBooked = util.getBookingQRCode();
        // Case of new booking or Re-Booking
        if (qrCodeBooked == null || qrCodeBooked.equals(qrCode)) {

            Log.d(LOG_TAG, "Calling the API to book...");

            BookingInput input = new BookingInput(InstanceIdService.getAppInstanceId(this), qrCode);
            Gson gson = new Gson();
            String json = gson.toJson(input); // myObject - instance of MyObject
            Log.d(LOG_TAG, "INPUT: " + json);

            MyCall<BookingResponse> call = service.book(input);

            CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
                @Override
                public void success(final Response<BookingResponse> response) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.d(LOG_TAG, "OUTPUT: " + json);

                    LandingScreenActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showBookingResponse(response.body(), qrCode);
                        }
                    });
                }
            };

            call.enqueue(customCallback);
            Log.d(LOG_TAG, "Waiting for response...");
        }
        // Case of double booking
        else if (!qrCodeBooked.equals(qrCode)) {
            BookingReleaseInput input = new BookingReleaseInput(CodeConstants.AC601, InstanceIdService.getAppInstanceId(this), qrCode);

            Log.d(LOG_TAG, "Calling the API to release...");

            Gson gson = new Gson();
            String json = gson.toJson(input); // myObject - instance of MyObject
            Log.d(LOG_TAG, "INPUT: " + json);

            MyCall<BookingResponse> call = service.bookRelease(input);

            CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
                @Override
                public void success(final Response<BookingResponse> response) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.d(LOG_TAG, "OUTPUT: " + json);

                    LandingScreenActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showBookingReleaseResponse(response.body(), qrCode, true, false, false);
                        }
                    });
                }
            };

            call.enqueue(customCallback);
            Log.d(LOG_TAG, "Waiting for response...");
        }

    }

    public void releaseQRCodeItem(final String qrCode, final boolean showConfirmation, final boolean removeLocalBooking) {
        releaseQRCodeItem(qrCode, showConfirmation, removeLocalBooking, false);
    }

    public void releaseQRCodeItem(final String qrCode, final boolean showConfirmation, final boolean removeLocalBooking,
                                  final boolean refreshHotspots) {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();
        RetrofitService service = retrofit.create(RetrofitService.class);

        BookingReleaseInput input = new BookingReleaseInput(CodeConstants.AC301, InstanceIdService.getAppInstanceId(this), qrCode);
        Log.d(LOG_TAG, "Calling the API to release...");
        Gson gson = new Gson();
        String json = gson.toJson(input); // myObject - instance of MyObject
        Log.d(LOG_TAG, "INPUT: " + json);

        MyCall<BookingResponse> call = service.bookRelease(input);

        CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<BookingResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                LandingScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBookingReleaseResponse(response.body(), qrCode, showConfirmation, removeLocalBooking, refreshHotspots);
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void renewQRCodeItem(final String qrCode) {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        Log.d(LOG_TAG, "Calling the API to renew...");
        BookingReleaseInput input = new BookingReleaseInput(CodeConstants.AC302, InstanceIdService.getAppInstanceId(this), qrCode);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<BookingResponse> call = service.bookRelease(input);

        CustomCallback<BookingResponse> customCallback = new CustomCallback<BookingResponse>(call.getUrl(), this) {
            @Override
            public void success(final Response<BookingResponse> response) {
                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d(LOG_TAG, "OUTPUT: " + json);

                LandingScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBookingRenewResponse(response.body(), qrCode);
                    }
                });
            }
        };

        call.enqueue(customCallback);
        Log.d(LOG_TAG, "Waiting for response...");
    }

    public void showBookingRenewResponse(BookingResponse br, String qrCode) {
        if (br.getActionCode().equals(CodeConstants.RC302)) {
            String message = "Desk is successfully booked for next " + DatetimeUtil.getTimeDifference(br.getBookedTime());
            DialogUtil.showOkDialog(this, message, false, true);
            util.addBooking(qrCode, br.getBookedTime());
        }
    }

    public void showBookingResponse(BookingResponse br, String qrCode) {

        boolean refreshFloorplan = false;
        Log.d(LOG_TAG, "Building ID from qrCode: " + QRCodeUtil.getBuildingId(qrCode) + ", SelectedBuildingId: " + selectedBuildingId);
        if (QRCodeUtil.getBuildingId(qrCode) == selectedBuildingId) {
            refreshFloorplan = true;
        }

        // set the custom dialog components - text, image and button
        if (br.getActionCode().equals(CodeConstants.RC301)) {
            DialogUtil.showOkDialogWithCancel(this,
                    "Desk is successfully booked for next " + DatetimeUtil.getTimeDifference(br.getBookedTime()),
                    qrCode, refreshFloorplan);
            util.addBooking(qrCode, br.getBookedTime());
        } else if (br.getActionCode().equals(CodeConstants.RC401)) {
            String message = "Desk is not available - " + DatetimeUtil.getTimeDifference(br.getBookedTime())
                                + " remaining on current booking.";
            DialogUtil.showOkDialog(this, message, false, true);
        }
        // If user has booked the same qrCode before and has scanned the same qrCode again.
        else if (br.getActionCode().equals(CodeConstants.RC501)) {
            String message = "You still have " + DatetimeUtil.getTimeDifference(br.getBookedTime()) + " remaining on this booking";
            DialogUtil.showDialogWithThreeButtons(this, message, qrCode, refreshFloorplan);
        }
    }

    public void showBookingReleaseResponse(BookingResponse br, String qrCode,
                                           boolean showConfirmation, boolean removeLocalBooking, boolean refreshHotspots) {
        if (br.getActionCode().equals(CodeConstants.RC601)) {
            if (showConfirmation)
                DialogUtil.showOkDialog(this, "Desk is now released!", refreshHotspots, false);
            if (removeLocalBooking)
                util.removeBooking();
        } else if (br.getActionCode().equals(CodeConstants.RC401)) {
            String message = "Desk is not available - " + DatetimeUtil.getTimeDifference(br.getBookedTime())
                    + " remaining on current booking.";
            DialogUtil.showOkDialog(this, message, false, true);
        }
        // If user has already booked one qrCode and this is the second one booked
        else if (br.getActionCode().equals(CodeConstants.RC701)) {
            String qrCodeOld = util.getBookingQRCode();
            String message = "Desk is successfully booked for next " + DatetimeUtil.getTimeDifference(br.getBookedTime());

            boolean refreshFloorplan = false;
            Log.d(LOG_TAG, "Building ID from qrCode: " + QRCodeUtil.getBuildingId(qrCode) + ", SelectedBuildingId: " + selectedBuildingId);
            if (QRCodeUtil.getBuildingId(qrCode) == selectedBuildingId) {
                refreshFloorplan = true;
            }
            DialogUtil.showOkDialogWithCancelForSecondBooking(this, message, qrCodeOld, qrCode, br.getBookedTime(), refreshFloorplan);
        }
        // If user has already booked two desks, new booking is not allowed
        else if (br.getActionCode().equals(CodeConstants.RC801)) {
        }
    }


    private void updateFloorplanWithHotspots(String locations, int hotspotColor) {
        Matrix matrix = new Matrix();
        floorplan.getSuppMatrix(matrix);

        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        Bitmap copyBitmap = decodedByte.copy(Bitmap.Config.ARGB_8888, true);

        initHotspotData(locations);
        printHotspotData();

        drawHotspots(copyBitmap, hotspotColor);
        floorplan.setImageBitmap(copyBitmap);

        floorplan.setDisplayMatrix(matrix);

        /*
        if (hotspotCenters.size() > 0) {
            float minX = Float.MAX_VALUE;
            for (HotspotCenter c : hotspotCenters) {
                if (c.x < minX) minX = c.x;
            }

            int x = (int) (minX * originalBitmapWidth);
            floorplan.scrollTo(x, 0);
        }*/
    }

    public void drawHotspots(Bitmap bMap, int hotspotColor) {

        Canvas canvas = new Canvas();
        canvas.setBitmap(bMap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(this, hotspotColor));
        paint.setAlpha(140);

        int bWidth = bMap.getWidth();
        int bHeight = bMap.getHeight();

        int halfSizeX = (int) (hotspotSize * bWidth / (2 * originalBitmapWidth));
        int halfSizeY = (int) (hotspotSize * bHeight / (2 * originalBitmapHeight));

        for (HotspotCenter c : hotspotCenters) {
            int startX = (int) ((c.x) * bWidth);
            int startY = (int) ((c.y) * bHeight);
            canvas.drawRect(startX - halfSizeX, startY - halfSizeY, startX + halfSizeX, startY + halfSizeY, paint);
        }
    }

    public void initHotspotData(String locations) {
        if (hotspotCenters.size() > 0) {
            hotspotCenters.removeAll(hotspotCenters);
        }
        if (locations.trim().length() == 0)
            return;
        String[] hotspotCoords = locations.split(",");
        for (String coords : hotspotCoords) {
            String[] xy = coords.split("_");
            float x = (float) (Integer.parseInt(xy[0]) * 1.0 / originalBitmapWidth);
            float y = (float) (Integer.parseInt(xy[1]) * 1.0 / originalBitmapHeight);
            HotspotCenter c = new HotspotCenter(x, y);
            hotspotCenters.add(c);
        }
    }

    private void printHotspotData() {
        Log.d(LOG_TAG, "HotspotSize = " + hotspotSize);
        for (HotspotCenter c : hotspotCenters) {
            Log.d(LOG_TAG, "Center: (" + c.x + ", " + c.y + ")");
        }
    }

    private int getPrivilegeId() {
        Authentication auth = util.getAuthenticationInfo();
        for (Org o : auth.getOrganization()) {
            if (o.getOrganizationId() == defaultOrgId)
                return o.getPrivilegeId();
        }
        return 0;
    }

    private void fetchFloorplanAndAvailability() {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        final SharedPreferencesUtil util = new SharedPreferencesUtil(this);
        List<Authorization> authList = util.getAuthorizationInfo(selectedBuildingId);

        final FloorplanInputV2 input = new FloorplanInputV2();
        input.setActionCodeItemType(CodeConstants.AC101);
        input.setActionCodeFloorPlan(CodeConstants.AC201);
        input.setBuildingId("" + selectedBuildingId);
        input.setOrganizationId(defaultOrgId);
        input.setPrivilegeId(getPrivilegeId());
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        List<FloorPlan> floorplanList = new ArrayList<FloorPlan>();
        for (Authorization a : authList) {
            FloorPlan fp = new FloorPlan();
            fp.setFloorPlanId(Integer.parseInt(a.getFloorPlanId()));
            File floorplanFile = new File(Environment.getExternalStorageDirectory(),
                    "Pictures/floorplan_" + selectedBuildingId + "_" + a.getFloorPlanId());
            Log.d(LOG_TAG, "Checking if file exists: "
                    + "Pictures/floorplan_" + selectedBuildingId + "_" + a.getFloorPlanId());
            if (floorplanFile.exists()) {
                Log.d(LOG_TAG, "File exists!");
                fp.setImageStatus(true);
            } else {
                Log.d(LOG_TAG, "File does NOT exist!");
                fp.setImageStatus(false);
            }
            floorplanList.add(fp);
        }
        input.setFloorPlan(floorplanList);

        Gson gson = new Gson();
        String json = gson.toJson(input);
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<FloorplanResponseV2> call = service.floorplanV2(input);

        CustomCallback<FloorplanResponseV2> customCallback = new CustomCallback<FloorplanResponseV2>(call.getUrl(), this) {
            @Override
            public void success(final Response<FloorplanResponseV2> response) {

                FloorPlanDetailV2 firstAvailable = response.body().getFloorPlanDetails().get(0);
                boolean imageReturned = ((firstAvailable.getFloorImage() != null) && (firstAvailable.getFloorImage().length() > 0));

                Gson gson = new Gson();
                String json = gson.toJson(input); // myObject - instance of MyObject
                json = gson.toJson(response.body()); // myObject - instance of MyObject
                Log.d(LOG_TAG, "OUTPUT: " + json);
                Log.d(LOG_TAG, "Floorplan detail response received!");

                if (imageReturned && !(util.getNoSpaceOnDevice())) {
                    saveFloorplans(response.body(), selectedBuildingId);
                }
                LandingScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateFloorplan(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
    }

    private void saveFloorplans(FloorplanResponseV2 r, Long buildingId) {
        long extMemSize = MemoryUtil.getExternalStorageSpace();
        FloorPlanDetailV2 f = r.getFloorPlanDetails().get(0);

        if (f.getFloorImage() != null && f.getFloorImage().length() > 0) {
            byte[] decodedString = Base64.decode(f.getFloorImage(), Base64.DEFAULT);

            Log.d(LOG_TAG, "Saving floorplan for floorplan ID: " + f.getFloorPlanId());
            if (decodedString.length > extMemSize) {
                util.setNoSpaceOnDevice(true);
                return;
            }

            try {
                File floorplanFile = new File(Environment.getExternalStorageDirectory(),
                        "Pictures/floorplan_" + buildingId.intValue() + "_" + f.getFloorPlanId().intValue());
                Log.d(LOG_TAG, "Saving to file: " + "Pictures/floorplan_" + buildingId.intValue() + "_" + f.getFloorPlanId().intValue());

                File picturesDir = new File(Environment.getExternalStorageDirectory(), "Pictures");

                if (!picturesDir.exists()) {
                    if (!picturesDir.mkdirs()) {
                        Log.e(LOG_TAG, "Directory not created");
                    }
                }

                FileOutputStream fos = new FileOutputStream(floorplanFile);
                fos.write(decodedString);
                fos.close();
            } catch (IOException ioe) {
                Log.d(LOG_TAG, "Problem saving downloaded image! " + ioe.getMessage());
            }
            Log.d(LOG_TAG, "Floorplan saved for floorplan ID: " + f.getFloorPlanId());
        }
    }

    private void fetchFloorplanAndAvailability(long floorplanId) {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        final SharedPreferencesUtil util = new SharedPreferencesUtil(this);
        List<Authorization> authList = util.getAuthorizationInfo(selectedBuildingId);

        final FloorplanInputV2 input = new FloorplanInputV2();
        input.setActionCodeItemType(CodeConstants.AC101);
        input.setActionCodeFloorPlan(CodeConstants.AC201);
        input.setBuildingId("" + selectedBuildingId);
        input.setOrganizationId(defaultOrgId);
        input.setPrivilegeId(getPrivilegeId());
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        List<FloorPlan> floorplanList = new ArrayList<FloorPlan>();
        for (Authorization a : authList) {
            Log.d(LOG_TAG, "-------------------------------- a.getFloorPlanId(): " + a.getFloorPlanId() + ", floorplanId: " + floorplanId);
            if (Long.parseLong(a.getFloorPlanId()) >= floorplanId) {
                FloorPlan fp = new FloorPlan();
                fp.setFloorPlanId(Integer.parseInt(a.getFloorPlanId()));

                File floorplanFile = new File(Environment.getExternalStorageDirectory(),
                        "Pictures/floorplan_" + selectedBuildingId + "_" + a.getFloorPlanId());
                Log.d(LOG_TAG, "Checking if file exists: "
                        + "Pictures/floorplan_" + selectedBuildingId + "_" + a.getFloorPlanId());
                if (floorplanFile.exists()) {
                    Log.d(LOG_TAG, "File exists!");
                    fp.setImageStatus(true);
                } else {
                    Log.d(LOG_TAG, "File does NOT exist!");
                    fp.setImageStatus(false);
                }
                floorplanList.add(fp);
            }
        }
        input.setFloorPlan(floorplanList);
        Gson gson = new Gson();
        String json = gson.toJson(input); // myObject - instance of MyObject
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<FloorplanResponseV2> call = service.floorplanV2(input);

        CustomCallback<FloorplanResponseV2> customCallback = new CustomCallback<FloorplanResponseV2>(call.getUrl(), this) {
            @Override
            public void success(final Response<FloorplanResponseV2> response) {

                FloorPlanDetailV2 firstAvailable = response.body().getFloorPlanDetails().get(0);
                boolean imageReturned = ((firstAvailable.getFloorImage() != null) && (firstAvailable.getFloorImage().length() > 0));

                Gson gson = new Gson();
                String json = gson.toJson(input); // myObject - instance of MyObject
                json = gson.toJson(response.body()); // myObject - instance of MyObject
                Log.d(LOG_TAG, "OUTPUT: " + json);
                Log.d(LOG_TAG, "Floorplan detail response received!");

                if (imageReturned && !(util.getNoSpaceOnDevice())) {
                    saveFloorplans(response.body(), selectedBuildingId);
                }
                LandingScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateFloorplan(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
    }

    public void fetchAvailabilityForCurrentFloorplan() {
        Retrofit retrofit = RetrofitBuilder.getRetrofit();

        final FloorplanInputV2 input = new FloorplanInputV2();
        input.setActionCodeItemType(CodeConstants.AC101);
        input.setActionCodeFloorPlan(CodeConstants.AC201);
        input.setBuildingId("" + selectedBuildingId);
        input.setOrganizationId(defaultOrgId);
        input.setPrivilegeId(getPrivilegeId());
        input.setUuid(InstanceIdService.getAppInstanceId(this));

        List<FloorPlan> floorplanList = new ArrayList<FloorPlan>();
        FloorPlan fp = new FloorPlan();
        fp.setFloorPlanId(new Long(selectedFloorplanId).intValue());
        fp.setImageStatus(true);
        floorplanList.add(fp);

        input.setFloorPlan(floorplanList);
        Gson gson = new Gson();
        String json = gson.toJson(input); // myObject - instance of MyObject
        Log.d(LOG_TAG, "INPUT: " + json);

        RetrofitService service = retrofit.create(RetrofitService.class);
        MyCall<FloorplanResponseV2> call = service.floorplanV2(input);

        CustomCallback<FloorplanResponseV2> customCallback = new CustomCallback<FloorplanResponseV2>(call.getUrl(), this) {
            @Override
            public void success(final Response<FloorplanResponseV2> response) {

                FloorPlanDetailV2 firstAvailable = response.body().getFloorPlanDetails().get(0);
                boolean imageReturned = ((firstAvailable.getFloorImage() != null) && (firstAvailable.getFloorImage().length() > 0));

                Gson gson = new Gson();
                String json = gson.toJson(input); // myObject - instance of MyObject
                json = gson.toJson(response.body()); // myObject - instance of MyObject
                Log.d(LOG_TAG, "OUTPUT: " + json);
                Log.d(LOG_TAG, "Floorplan detail response received!");

                if (imageReturned && !(util.getNoSpaceOnDevice())) {
                    saveFloorplans(response.body(), selectedBuildingId);
                }
                LandingScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateFloorplan(response.body());
                    }
                });
            }
        };

        call.enqueue(customCallback);
    }
}
