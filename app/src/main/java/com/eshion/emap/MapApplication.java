package com.eshion.emap;

import android.app.Application;
import android.location.Location;
import android.os.*;
import android.os.Process;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapsInitializer;

/**
 * Created by eshion on 16-12-7.
 */
public class MapApplication extends Application implements AMapLocationListener {
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    private LocationSource.OnLocationChangedListener mLocationChangeedListener;
    private AMapLocation mLocation;

    private MyLocationChangeListener mDispatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        option.setOnceLocationLatest(false);
        option.setSensorEnable(true);
        mLocationClient.setLocationOption(option);
        myStartLocation();
        // mLocationClient.startAssistantLocation();
        //指定离线地图下载路径
        MapsInitializer.sdcardDir = "/sdcard/xx/amap";
    }

    public void myStartLocation(){
        mLocationClient.startLocation();
    }

    public boolean isStartState(){
        return mLocationClient.isStarted();
    }

    public void exitApplication(){
        mLocationClient.stopLocation();
        // mLocationClient.stopAssistantLocation();
        android.os.Process.killProcess(Process.myUid());
    }

    public AMapLocation getLocation(){
        return mLocation;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        mLocation = aMapLocation;
        if(aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS){
            mLocationChangeedListener.onLocationChanged(aMapLocation);
            if(mDispatcher!=null) mDispatcher.dispatcherLocationChange(mLocation);
            Log.d("xx", "onLocationChanged = " + aMapLocation.getAddress());
        }

    }


    public void addLocationChangedListener(LocationSource.OnLocationChangedListener l){
        mLocationChangeedListener = l;
    }

    public void resgiterLocationChangeDispatcher(MyLocationChangeListener dispatcher){
        mDispatcher = dispatcher;
    }

    public interface MyLocationChangeListener{
        public void dispatcherLocationChange(AMapLocation aMapLocation);
    }




}
