package com.eshion.emap;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.overlay.BusRouteOverlay;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.util.List;
import java.util.jar.Manifest;

public class MapActivity extends AppCompatActivity implements LocationSource, View.OnClickListener, RouteSearch.OnRouteSearchListener {
    private MapView mapView;
    private AMap mAamp;


    private UiSettings mUiSettings;

    private RouteSearch mRouteSearch;
    private RouteSearch.FromAndTo mFromAndTo;
    private int mMode;

    private Toolbar toolbar;
    private AppCompatButton appBtn;
    private TextView mRoutePlanning ,mOffline;

    public static final int ROUTEPLANNING_INTENT = 10;
    public static final int MAPNAVI_INTENT = 100;
    private MapApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkoutPermission();
        }
        application = (MapApplication) getApplication();
        if(!application.isStartState()) application.myStartLocation();

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appBtn = (AppCompatButton) findViewById(R.id.startnavi);
        appBtn.setOnClickListener(this);

        mapView = (MapView) findViewById(R.id.map);
        mRoutePlanning = (TextView) findViewById(R.id.routePlanning);
        mOffline = (TextView) findViewById(R.id.offline);
        mRoutePlanning.setOnClickListener(this);
        mOffline.setOnClickListener(this);

        mapView.onCreate(savedInstanceState);
        mAamp = mapView.getMap();


        mUiSettings = mAamp.getUiSettings();
        mUiSettings.setScaleControlsEnabled(true);//显示比例尺控件
        mAamp.setLocationSource(this);// 设置定位监听
        mUiSettings.setMyLocationButtonEnabled(true); // 显示默认的定位按钮
        mAamp.setMyLocationEnabled(true);// 可触发定位并显示定位层




        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

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
        application.exitApplication();

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        application.addLocationChangedListener(onLocationChangedListener);
        Log.d("xx", "activate");


    }

    @Override
    public void deactivate() {
        application.addLocationChangedListener(null);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkoutPermission(){
        Context context = getApplicationContext();
        boolean var = (context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                                                 == PackageManager.PERMISSION_GRANTED);
        if(!var){
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }

        var = (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
        if(!var){
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},2);
        }

        var = (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED);
        if(!var){
            requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE},3);
        }

        var = (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
        if(!var){
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},4);
        }
    }



    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.routePlanning:
                Intent routeIntent = new Intent(this,RoutePlanningActivity.class);
                startActivityForResult(routeIntent, ROUTEPLANNING_INTENT);
                /*mAamp.setLoadOfflineData(false);
                mAamp.setLoadOfflineData(true);*/
                //mLocationChangeedListener.onLocationChanged();
               // Log.d("xx","load offline map");
                break;
            case R.id.startnavi:

                mapNaviModeDialog();
                break;
            case R.id.offline:
                Intent offlineIntent = new Intent(this,OfflineMapActivity.class);
                startActivity(offlineIntent);

                break;
        }

    }

    private void mapNaviModeDialog(){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setItems(new String[]{"模拟导航", "实时导航"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mapNaviIntent = new Intent(MapActivity.this,MapNaviActivity.class);
                mapNaviIntent.putExtra("from_and_to",mFromAndTo);
                mapNaviIntent.putExtra("mode", mMode);

                switch (which){
                    case 0:
                        mapNaviIntent.putExtra("class", 0);
                        break;
                    case 1:
                        mapNaviIntent.putExtra("class", 1);
                        break;
                }
                startActivityForResult(mapNaviIntent, MAPNAVI_INTENT);
            }
        });

        Dialog d = ab.create();
        d.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null){
            switch (requestCode){
                case ROUTEPLANNING_INTENT:
                    Log.d("xx","onActivityResult");
                    RouteSearch.FromAndTo fromAndTo = data.getParcelableExtra("from_and_to");
                    int mode = data.getIntExtra("mode",0);
                    if(fromAndTo!=null && mode !=0){
                        calculateRouteResult(fromAndTo,mode);
                    }
                    break;
                case MAPNAVI_INTENT:

                    toolbar.setVisibility(View.VISIBLE);
                    appBtn.setVisibility(View.GONE);
                    break;
            }

        }
    }

    private void calculateRouteResult(RouteSearch.FromAndTo fromAndTo,int mode){
        mFromAndTo = fromAndTo;
        mMode = mode;
        switch (mode){
            case R.id.id_drive:
                RouteSearch.DriveRouteQuery dq =
                        new RouteSearch.DriveRouteQuery(fromAndTo,RouteSearch.DrivingDefault,null,null,"");
                mRouteSearch.calculateDriveRouteAsyn(dq);
                break;
            case R.id.id_bus:
                RouteSearch.BusRouteQuery bq =
                        new RouteSearch.BusRouteQuery(fromAndTo,RouteSearch.BusLeaseWalk,application.getLocation().getCityCode(),0);
                mRouteSearch.calculateBusRouteAsyn(bq);
                break;
            case R.id.id_walk:
                RouteSearch.WalkRouteQuery wq = new RouteSearch.WalkRouteQuery(fromAndTo,RouteSearch.WalkDefault);
                mRouteSearch.calculateWalkRouteAsyn(wq);
                break;
        }
        if(mode!=R.id.id_bus){
            toolbar.setVisibility(View.GONE);
            appBtn.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        mAamp.clear(true);
        Log.d("xx","onBusRouteSearched = " + i);
        if(i==1000){
            BusPath onePath = null;
            List<BusPath> allPath = busRouteResult.getPaths();
            for (int index=0;index<allPath.size();index++){
                Log.d("xx",allPath.get(index).getBusDistance()+"");
                onePath = allPath.get(index);
            }
            BusRouteOverlay busOverlay =
                    new BusRouteOverlay(this,mAamp,onePath,mFromAndTo.getFrom(),mFromAndTo.getTo());
            busOverlay.addToMap();
            busOverlay.zoomToSpan();
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        mAamp.clear(true);
        Log.d("xx","onDriveRouteSearched = " + i);
        if(i==1000){
            DrivePath onePath = null;
            List<DrivePath> allPath = driveRouteResult.getPaths();
            for (int index=0;index<allPath.size();index++){
                Log.d("xx",allPath.get(index).getStrategy());
                onePath = allPath.get(index);
            }
            DrivingRouteOverlay driveOverlay =
                    new DrivingRouteOverlay(this,mAamp,onePath,mFromAndTo.getFrom(),mFromAndTo.getTo());
            driveOverlay.addToMap();
            driveOverlay.zoomToSpan();
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        mAamp.clear(true);
        Log.d("xx","onWalkRouteSearched = " + i);
        if(i==1000) {
            WalkPath onePath = null;
            List<WalkPath> allPath = walkRouteResult.getPaths();
            for (int index = 0; index < allPath.size(); index++) {
                Log.d("xx", allPath.get(index).getDistance() + "");
                onePath = allPath.get(index);
            }
            WalkRouteOverlay walkOverlay =
                    new WalkRouteOverlay(this, mAamp, onePath, mFromAndTo.getFrom(), mFromAndTo.getTo());
            walkOverlay.addToMap();
            walkOverlay.zoomToSpan();
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

}
