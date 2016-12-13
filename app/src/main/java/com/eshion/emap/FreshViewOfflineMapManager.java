package com.eshion.emap;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.daimajia.numberprogressbar.NumberProgressBar;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by eshion on 16-12-9.
 */
public class FreshViewOfflineMapManager implements OfflineMapManager.OfflineMapDownloadListener {

    private OfflineMapManager mOfflineMapManager;
    private Context mContext;
    private HashMap<String,OfflineMapActivity.ViewHolder> viewMap;

    public FreshViewOfflineMapManager(Context context){
        mContext = context;
        mOfflineMapManager = new OfflineMapManager(mContext,this);
    }

    public void setMap(HashMap<String,OfflineMapActivity.ViewHolder> map){
        viewMap = map;
    }

    public void downloadByCityCode(String citycode) throws AMapException {
        mOfflineMapManager.downloadByCityCode(citycode);
    }

    public OfflineMapCity getItemByCityCode(java.lang.String cityCode){
        return mOfflineMapManager.getItemByCityCode(cityCode);
    }

    public ArrayList<OfflineMapCity> getDownloadingCityList(){
        return mOfflineMapManager.getDownloadingCityList();
    }

    public ArrayList<OfflineMapCity>  	getDownloadOfflineMapCityList(){
        return mOfflineMapManager.getDownloadOfflineMapCityList();
    }

    public ArrayList<OfflineMapCity> getOfflineMapCityList(){
        return mOfflineMapManager.getOfflineMapCityList();
    }

    @Override
    public void onDownload(int i, int i1, String s) {
        NumberProgressBar mDownLoadProgressBar =null;
        TextView mDownLoadStatus = null;
        Button mDownLoadOp = null;
        OfflineMapActivity.ViewHolder holder = viewMap.get(s);
        if(holder!=null){
            OfflineMapCity city = (OfflineMapCity) holder.mOp.getTag(R.id.id_model);
            if(s.equals(city.getCity())){
                mDownLoadProgressBar = holder.mProgressBar;
                mDownLoadStatus = holder.mStatus;
                mDownLoadOp = holder.mOp;
            }
        }
        Log.d("xx", "status = " + i + " progress = " + i1 + " cityName = " + s);
        if((i == com.amap.api.maps.offlinemap.OfflineMapStatus.LOADING) ||
                (i == com.amap.api.maps.offlinemap.OfflineMapStatus.UNZIP)){
            if(mDownLoadProgressBar!=null)
                mDownLoadProgressBar.setProgress(i1);
            if(i == com.amap.api.maps.offlinemap.OfflineMapStatus.LOADING){
                if(mDownLoadProgressBar!=null)
                    mDownLoadProgressBar.setVisibility(View.VISIBLE);
                if(mDownLoadStatus!=null){
                    mDownLoadStatus.setVisibility(View.VISIBLE);
                    mDownLoadStatus.setText(mContext.getResources().getString(R.string.offlinemap_downloading_str));
                }

            }else {
                if(mDownLoadStatus!=null)
                    mDownLoadStatus.setText(mContext.getResources().getString(R.string.offlinemap_upzip_str));
            }
        }else if(i== com.amap.api.maps.offlinemap.OfflineMapStatus.SUCCESS){
            if(mDownLoadProgressBar!=null)
                mDownLoadProgressBar.setProgress(i1);
            if(mDownLoadStatus!=null){
                mDownLoadStatus.setText(mContext.getResources().getString(R.string.offlinemap_success_str));
                mDownLoadOp.setText(mContext.getResources().getString(R.string.offlinemap_completedownload_str));
            }


        }else if(i== com.amap.api.maps.offlinemap.OfflineMapStatus.WAITING){
            if(mDownLoadStatus!=null){
                mDownLoadStatus.setText(mContext.getResources().getString(R.string.offlinemap_waitdownload_str));
            }
        }
    }

    @Override
    public void onCheckUpdate(boolean b, String s) {

    }

    @Override
    public void onRemove(boolean b, String s, String s1) {

    }
}
