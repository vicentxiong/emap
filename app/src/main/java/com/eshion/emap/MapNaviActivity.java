package com.eshion.emap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStaticInfo;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.route.RouteSearch;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eshion on 16-12-2.
 */
public class MapNaviActivity  extends Activity implements AMapNaviViewListener, View.OnClickListener, AMapNaviListener {
    private AMapNavi mAmapNavi;
    private TTSController mTtsManager;

    private AMapNaviView naviMapView;
    private TextView stopNaviMap;

    private RouteSearch.FromAndTo mFormAndTo;
    private int mMode;
    private int mClass;

    private List<NaviLatLng> naviLatLngs = new ArrayList<NaviLatLng>();
    private List<NaviLatLng> naviLatLnge = new ArrayList<NaviLatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_navi);

        //实例化语音引擎
        mTtsManager = TTSController.getInstance(getApplicationContext());
        mTtsManager.init();

        mAmapNavi = AMapNavi.getInstance(getApplicationContext());
        mAmapNavi.addAMapNaviListener(this);
        mAmapNavi.addAMapNaviListener(mTtsManager);

        naviMapView = (AMapNaviView) findViewById(R.id.navi_view);
        naviMapView.onCreate(savedInstanceState);
        naviMapView.setAMapNaviViewListener(this);

        stopNaviMap = (TextView) findViewById(R.id.stopNavi);
        stopNaviMap.setOnClickListener(this);

        mFormAndTo = getIntent().getParcelableExtra("from_and_to");
        mMode = getIntent().getIntExtra("mode", 0);
        mClass = getIntent().getIntExtra("class",0);

        if(mClass==0){
            mAmapNavi.setEmulatorNaviSpeed(75);
        }

        naviLatLngs.add(new NaviLatLng(mFormAndTo.getFrom().getLatitude(), mFormAndTo.getFrom().getLongitude()));
        naviLatLnge.add(new NaviLatLng(mFormAndTo.getTo().getLatitude(), mFormAndTo.getTo().getLongitude()));
    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {
        Log.d("xx","onNaviViewLoaded");
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        stopMapNaviDialog();
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {
        Log.d("xx","onInitNaviSuccess");
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAmapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (mMode){
            case R.id.id_drive:
                mAmapNavi.calculateDriveRoute(naviLatLngs,naviLatLnge,null,strategy);
                break;
            case R.id.id_walk:
                mAmapNavi.calculateWalkRoute(new NaviLatLng(mFormAndTo.getFrom().getLatitude(),mFormAndTo.getFrom().getLongitude()),
                                             new NaviLatLng(mFormAndTo.getTo().getLatitude(),mFormAndTo.getTo().getLongitude()));
                break;
        }
    }
    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    /**
     * @param naviStaticInfo
     * @deprecated
     */
    @Override
    public void onArriveDestination(NaviStaticInfo naviStaticInfo) {

    }

    @Override
    public void onArriveDestination(AMapNaviStaticInfo aMapNaviStaticInfo) {

    }

    @Override
    public void onCalculateRouteSuccess() {
        Log.d("xx", "onCalculateRouteSuccess");
        if(mClass==0){
            mAmapNavi.startNavi(NaviType.EMULATOR);
        }else {
            mAmapNavi.startNavi(NaviType.GPS);
        }

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    /**
     * @param aMapNaviInfo
     * @deprecated
     */
    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    /**
     * @param aMapNaviTrafficFacilityInfo
     * @deprecated
     */
    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    /**
     * @param trafficFacilityInfo
     * @deprecated
     */
    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        naviMapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        naviMapView.onPause();

        mTtsManager.stopSpeaking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        naviMapView.onDestroy();

        mAmapNavi.stopNavi();
        mAmapNavi.destroy();
        mTtsManager.destroy();
    }

    private void stopMapNaviDialog(){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage(R.string.stopMapNavi_ornot_str);
        ab.setPositiveButton(R.string.stop_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent res = new Intent();
                setResult(RESULT_OK, res);
                finish();
            }
        });
        ab.setNegativeButton(R.string.cancel_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog d = ab.create();
        d.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            stopMapNaviDialog();
            return true;
        }
        return true;
    }
}
