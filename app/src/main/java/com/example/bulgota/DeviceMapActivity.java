package com.example.bulgota;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bulgota.api.MarkerService;
import com.example.bulgota.api.Marker_list;
import com.example.bulgota.api.ResponseWithMarkerData;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.overlay.Overlay;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeviceMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int PAGE_UP = 8;
    private static final int PAGE_LEFT = 4;
    private static final int PAGE_RIGHT = 6;
    private static final int PAGE_DOWN = 2;

    private MapView mapView;
    private FusedLocationSource locationSource;
    private NaverMap map;

    private Marker lastMarker;
    private Marker[] markerItems;
    private boolean initMapLoad = true;
    private Button btnHomeLocation;
    private Button btnInfoLocation;
    private Button btnHomeZoomIn;
    private Button btnHomeZoomOut;
    private Button btnInfoZoomIn;
    private Button btnInfoZoomOut;

    private ImageView btnHamberger;

    private View viewLayer;

    private ConstraintLayout clHamberger;
    private ConstraintLayout clModelInfo;
    private ConstraintLayout clToolbar;

    private Animation translateUpAim;
    private Animation translateDownAim;
    private Animation translateRightAim;
    private Animation translateLeftAim;

    private boolean isInfoPageOpen = false;
    private boolean isHambergerOpen = false;
    private int pageValue;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_map);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(isHambergerOpen) {
            viewLayer.performClick();
            return;
        } else if(isInfoPageOpen) {
            map.getOnMapClickListener().onMapClick(new PointF(10,10), lastMarker.getPosition());
            //return;
        } else {
            super.onBackPressed();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                map.setLocationTrackingMode(LocationTrackingMode.None);
            }
            map.setLocationSource(locationSource);
            map.setLocationTrackingMode(LocationTrackingMode.Follow);
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        map = naverMap;
        setHamberger();
        makeUiSetting();
        getMarker();
        makeCircle();
        mapLoad();
    }

    private void makeUiSetting() {
        btnHomeLocation = findViewById(R.id.btn_home_location);
        btnInfoLocation = findViewById(R.id.btn_info_location);
        btnHomeZoomIn = findViewById(R.id.btn_home_zoom_in);
        btnHomeZoomOut = findViewById(R.id.btn_home_zoom_out);
        btnInfoZoomIn = findViewById(R.id.btn_info_zoom_in);
        btnInfoZoomOut = findViewById(R.id.btn_info_zoom_out);

        UiSettings uiSettings = map.getUiSettings();

        uiSettings.setCompassEnabled(false);
        uiSettings.setScaleBarEnabled(false);
        uiSettings.setZoomControlEnabled(false);

        btnHomeLocation.setOnClickListener(l -> {
            btnLocationClickEvent(btnHomeLocation);
        });

        btnInfoLocation.setOnClickListener(l -> {
            btnLocationClickEvent(btnInfoLocation);
        });

        btnHomeZoomIn.setOnClickListener(l -> {
            btnZoomClickEvent(btnHomeZoomIn, true);
        });

        btnHomeZoomOut.setOnClickListener(l -> {
            btnZoomClickEvent(btnHomeZoomOut, false);
        });

        btnInfoZoomIn.setOnClickListener(l -> {
            btnZoomClickEvent(btnInfoZoomIn, true);
        });

        btnInfoZoomOut.setOnClickListener(l -> {
            btnZoomClickEvent(btnInfoZoomOut, false);
        });
    }

    private void btnLocationClickEvent(Button button) {
        button.setOnClickListener(l -> {
            if(locationSource.getLastLocation() != null) {
                map.moveCamera(CameraUpdate.scrollAndZoomTo(new LatLng(locationSource.getLastLocation().getLatitude(), locationSource.getLastLocation().getLongitude()), 15)
                        .animate(CameraAnimation.Linear, 3000));
                map.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        });
    }

    private void btnZoomClickEvent(Button button, boolean zoom) {
        if(zoom) {
            map.moveCamera(CameraUpdate.zoomIn().animate(CameraAnimation.Easing, 1500));
        } else {
            map.moveCamera(CameraUpdate.zoomOut().animate(CameraAnimation.Fly, 1500));
        }
    }

    protected void getMarker() {
        //애니메이션 준비
        translateUpAim = AnimationUtils.loadAnimation(this, R.anim.translate_up);
        SlidingPageAnimationListener animationListener = new SlidingPageAnimationListener();

        clModelInfo = findViewById(R.id.cl_model_info);

        TextView tvModelNum = findViewById(R.id.tv_model_num);
        TextView tvBatteryValue = findViewById(R.id.tv_battery_value);
        TextView tvTimeValue = findViewById(R.id.tv_time_value);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MarkerService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MarkerService markerService = retrofit.create(MarkerService.class);
        markerService.getMarkerAll().enqueue(new Callback<ResponseWithMarkerData>() {
            @Override
            public void onResponse(Call<ResponseWithMarkerData> call, Response<ResponseWithMarkerData> response) {
                if (response.body().getSuccess()) {
                    ArrayList<Marker_list> markerDataList = response.body().getData();

                    if (markerDataList.get(0) == null) {
                        return;
                    }

                    markerItems = new Marker[markerDataList.size()];

                    for (int i = 0; i < markerDataList.size(); i++) {
                        Log.e("data" + i, markerDataList.get(i).getModel() + "");
                        markerItems[i] = new Marker();
                        markerItems[i].setTag(i + 1);
                        markerItems[i].setPosition(new LatLng(markerDataList.get(i).getLatitude(), markerDataList.get(i).getLongitude()));
                        markerItems[i].setIcon(OverlayImage.fromResource(R.drawable.normal_marker));
                        markerItems[i].setWidth(60);
                        markerItems[i].setHeight(60);
                        markerItems[i].setMap(map);

                        int finalI = i;

                        markerItems[i].setOnClickListener(overlay -> {
                            if(lastMarker == null || lastMarker.getTag() != markerItems[finalI].getTag()) {
                                LatLng coord = new LatLng(markerDataList.get(finalI).getLatitude(), markerDataList.get(finalI).getLongitude());
                                map.moveCamera(CameraUpdate.scrollAndZoomTo(coord, 16)
                                        .animate(CameraAnimation.Easing, 1500));

                                if (OverlayImage.fromResource(R.drawable.normal_marker).equals(markerItems[finalI].getIcon())) {
                                    if (lastMarker != null) {
                                        lastMarker.setIcon(OverlayImage.fromResource(R.drawable.normal_marker));
                                    }
                                    markerItems[finalI].setIcon(OverlayImage.fromResource(R.drawable.selected_marker));
                                    markerItems[finalI].setWidth(80);
                                    markerItems[finalI].setHeight(80);
                                    lastMarker = markerItems[finalI];
                                } else {
                                    markerItems[finalI].setIcon(OverlayImage.fromResource(R.drawable.normal_marker));
                                }

                                tvModelNum.setText(markerDataList.get(finalI).getModel());
                                tvBatteryValue.setText(String.valueOf(markerDataList.get(finalI).getBattery()) + "%");
                                tvTimeValue.setText(markerDataList.get(finalI).getTime());

                                //애니메이션 실행
                                pageValue = PAGE_UP;
                                translateUpAim.setAnimationListener(animationListener);
                                clModelInfo.setVisibility(View.VISIBLE);
                                clModelInfo.startAnimation(translateUpAim);
                            }
                            return true;
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseWithMarkerData> call, Throwable t) {
            }
        });
    }

    protected void makeCircle() {
        CircleOverlay circleOverlay = new CircleOverlay();
        circleOverlay.setCenter(new LatLng(37.4963111, 126.9574596));
        circleOverlay.setRadius(2000);
        circleOverlay.setColor(Color.parseColor("#196ED3EF"));
        circleOverlay.setOutlineColor(Color.parseColor("#FF4EBFDE"));
        circleOverlay.setOutlineWidth(3);
        circleOverlay.setMap(map);
    }

    private void mapLoad() {
        //애니메이션 준비
        translateDownAim = AnimationUtils.loadAnimation(this, R.anim.translate_down);
        SlidingPageAnimationListener animationListener = new SlidingPageAnimationListener();

        map.setLocationSource(locationSource);

        map.addOnLocationChangeListener(location -> {
            if(initMapLoad) {
                map.moveCamera(CameraUpdate.scrollAndZoomTo(new LatLng(location.getLatitude(), location.getLongitude()), 14)
                        .animate(CameraAnimation.Linear, 3000));
                map.setLocationTrackingMode(LocationTrackingMode.Follow);
                initMapLoad = false;
            }
        });

        map.addOnOptionChangeListener(() -> {
            locationSource.setCompassEnabled(true);
        });
        map.setLocationTrackingMode(LocationTrackingMode.Follow);

        map.setOnMapClickListener((point, coord) -> {
            //애니메이션
            if(isInfoPageOpen) {
                translateDownAim.setAnimationListener(animationListener);
                pageValue = PAGE_DOWN;
                clModelInfo.startAnimation(translateDownAim);

                lastMarker.setIcon(OverlayImage.fromResource(R.drawable.normal_marker));
                lastMarker.setWidth(60);
                lastMarker.setHeight(60);

                map.moveCamera(CameraUpdate.zoomTo(14)
                        .animate(CameraAnimation.Fly, 2000));
            }
        });
    }

    private void setHamberger() {
        translateLeftAim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        SlidingPageAnimationListener animationListener = new SlidingPageAnimationListener();

        clHamberger = findViewById(R.id.cl_hamberger);
        clToolbar = findViewById(R.id.cl_toolbar);
        btnHamberger = findViewById(R.id.btn_hamberger);
        viewLayer = findViewById(R.id.view_layer);

        btnHamberger.setOnClickListener(l -> {
            //애니메이션 준비
            translateRightAim = AnimationUtils.loadAnimation(this, R.anim.translate_right);
            translateRightAim.setAnimationListener(animationListener);

            Log.e("hamberger", "클릭");
            pageValue = PAGE_RIGHT;
            clHamberger.setAnimation(translateRightAim);
            clHamberger.setVisibility(View.VISIBLE);
            viewLayer.setVisibility(View.VISIBLE);
            clToolbar.setVisibility(View.GONE);

        });

        viewLayer.setOnClickListener((l -> {
            translateLeftAim.setAnimationListener(animationListener);

            pageValue = PAGE_LEFT;
            clHamberger.startAnimation(translateLeftAim);
        }));
    }

    private class SlidingPageAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
            switch (pageValue) {
                case PAGE_DOWN : {
                    isInfoPageOpen = false;
                    btnInfoLocation.setVisibility(View.GONE);
                    btnInfoZoomIn.setVisibility(View.GONE);
                    btnInfoZoomOut.setVisibility(View.GONE);
                    break;
                }
                case PAGE_UP : {
                    isInfoPageOpen = true;
                    break;
                }
                case PAGE_LEFT : {
                    clHamberger.setVisibility(View.GONE);
                    viewLayer.setVisibility(View.GONE);
                    clToolbar.setVisibility(View.VISIBLE);
                    isHambergerOpen = false;
                    break;
                }
                case PAGE_RIGHT : {
                    clToolbar.setVisibility(View.GONE);
                    viewLayer.setVisibility(View.VISIBLE);
                    isHambergerOpen = true;
                }
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            switch (pageValue) {
                case PAGE_DOWN : {
                    clModelInfo.setVisibility(View.GONE);
                    break;
                }
                case PAGE_UP : {
                    clModelInfo.setVisibility(View.VISIBLE);
                    btnInfoLocation.setVisibility(View.VISIBLE);
                    btnInfoZoomIn.setVisibility(View.VISIBLE);
                    btnInfoZoomOut.setVisibility(View.VISIBLE);
                    break;
                }
                case PAGE_LEFT : {
                    clHamberger.setVisibility(View.GONE);
                    break;
                }
                case PAGE_RIGHT : {
                    Log.e("why", "why");
                    clHamberger.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}