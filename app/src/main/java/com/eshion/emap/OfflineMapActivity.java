package com.eshion.emap;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.daimajia.numberprogressbar.NumberProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by eshion on 16-12-6.
 */
public class OfflineMapActivity extends AppCompatActivity implements  View.OnClickListener, MapApplication.MyLocationChangeListener {
    private TextView mCurrentCityName,mCurrentCityDownLoadStatus;
    private Button mOpOfflineMap;
    private ListView mCityList;
    private NumberProgressBar mCurrentCityProgressBar;

    private Button mDownLoadButton;

    private FreshViewOfflineMapManager mOfflineMgr;
    private OfflineMapCity mOfflineMapCity;
    private MapApplication application;

    private OfflineMapCityAdapter mAdapter;
    private HashMap<String,ViewHolder> cityViewHolderMap = new HashMap<String,ViewHolder>();
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        mOpOfflineMap.setEnabled(false);
        mCurrentCityProgressBar.setMax(100);
        try {
            mOfflineMgr.downloadByCityCode(mOfflineMapCity.getCode());
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispatcherLocationChange(AMapLocation aMapLocation) {
        //Toast.makeText(getApplicationContext(),""+aMapLocation.toString(),100).show();
        update(aMapLocation.getCityCode());
    }

    enum OfflineMapStatus{
        waitDownLoad,
        completeDownLoad,
        notDownLoad
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        application = (MapApplication) getApplication();
        mCurrentCityName = (TextView) findViewById(R.id.currentCityName);
        mCurrentCityDownLoadStatus = (TextView) findViewById(R.id.downLoadStatus);
        mCurrentCityProgressBar = (NumberProgressBar) findViewById(R.id.downLoadprogressBar);
        mOpOfflineMap = (Button) findViewById(R.id.opOfflineMap);
        mOpOfflineMap.setOnClickListener(this);
        mCityList = (ListView) findViewById(R.id.allCityList);
        mAdapter = new OfflineMapCityAdapter(getApplication());
        mCityList.setAdapter(mAdapter);


        mOfflineMgr = new FreshViewOfflineMapManager(this);
        mOfflineMgr.setMap(cityViewHolderMap);
        update(application.getLocation().getCityCode());
        updateOfflineMapCity();

        cityViewHolderMap.put(mOfflineMapCity.getCity(),createCurrcentCityHolder());
        application.resgiterLocationChangeDispatcher(this);
    }

    private ViewHolder createCurrcentCityHolder(){
        ViewHolder holder = new ViewHolder();
        holder.mProgressBar = mCurrentCityProgressBar;
        holder.mStatus = mCurrentCityDownLoadStatus;
        holder.mOp = mOpOfflineMap;
        holder.mOp.setTag(R.id.id_model,mOfflineMapCity);
        return holder;
    }

    private void updateOfflineMapCity(){
        List<OfflineMapCity> citys = mOfflineMgr.getOfflineMapCityList();
        citys.remove(mOfflineMapCity);
        mAdapter.set(citys);
        mAdapter.notifyDataSetChanged();
    }

    private void update(String cityCode){
        if(cityCode == null||cityCode.length()==0) return;
        mOfflineMapCity = mOfflineMgr.getItemByCityCode(cityCode);
        mCurrentCityName.setText(mOfflineMapCity.getCity());
        setTargetDownLoadButton(mOpOfflineMap);
        updateOpOfflineButton(cityCode);
    }

    private void setTargetDownLoadButton(Button btn){
        mDownLoadButton = btn;
    }

    private void updateOpOfflineButton(String code){
        OfflineMapStatus status = getOfflineMapCityStatus(code);
        switch (status){
            case waitDownLoad:
                /*mOpOfflineMap.setText(getResources().getString(R.string.offlinemap_waitdownload_str));
                mCurrentCityProgressBar.setVisibility(View.VISIBLE);*/
                break;
            case completeDownLoad:
                if(mDownLoadButton!=null){
                    mDownLoadButton.setText(getResources().getString(R.string.offlinemap_completedownload_str));
                    mDownLoadButton.setEnabled(false);
                }

                break;
            case notDownLoad:
                if(mDownLoadButton!=null){
                    mDownLoadButton.setText(getResources().getString(R.string.offlinemap_notdownload_str));
                    mDownLoadButton.setEnabled(true);
                }

                break;
        }
    }

    private OfflineMapStatus getOfflineMapCityStatus(String cityCode){
        String code = cityCode;
        OfflineMapStatus status = OfflineMapStatus.notDownLoad;
        List<OfflineMapCity> allCity = mOfflineMgr.getDownloadingCityList();
        //Log.d("xx","wait city size = " + allCity.size());
        for (int i=0;i<allCity.size();i++){
            OfflineMapCity city = allCity.get(i);
            //Log.d("xx","wait :" + city.getCode());
            if(code.equals(city.getCode())){
                return OfflineMapStatus.waitDownLoad;
            }
        }

        allCity = mOfflineMgr.getDownloadOfflineMapCityList();
        //Log.d("xx","complete city size = " + allCity.size());
        for (int j=0;j<allCity.size();j++){
            OfflineMapCity city = allCity.get(j);
            //Log.d("xx","complete :" + city.getCode());
            if(code.equals(city.getCode())){
                return OfflineMapStatus.completeDownLoad;
            }
        }
        return status;
    }

    class OfflineMapCityAdapter extends BaseAdapter{
        private Context mContext;
        private List<OfflineMapCity> allOffMapCity = new ArrayList<OfflineMapCity>();

        public OfflineMapCityAdapter(Context cx){
            mContext = cx;
        }

        public void set(List<OfflineMapCity> citys){
            allOffMapCity.clear();
            allOffMapCity.addAll(citys);
        }
        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return allOffMapCity.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return allOffMapCity.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            if(convertView ==null){
                LayoutInflater inflater =
                        (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.offlinemapcity_list,null,false);
                holder = new ViewHolder();
                holder.mCityName = (TextView) convertView.findViewById(R.id.otherOfflineMapCityName);
                holder.mProgressBar = (NumberProgressBar) convertView.findViewById(R.id.downLoadprogressBar);
                holder.mStatus = (TextView) convertView.findViewById(R.id.downLoadStatus);
                holder.mOp = (Button) convertView.findViewById(R.id.opOfflineMap);
                convertView.setTag(holder);
                holder.mOp.setTag(R.id.id_view,holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mCityName.setText(allOffMapCity.get(position).getCity());
            holder.mOp.setTag(R.id.id_model, allOffMapCity.get(position));
            setTargetDownLoadButton(holder.mOp);
            updateOpOfflineButton(allOffMapCity.get(position).getCode());
            cityViewHolderMap.put(allOffMapCity.get(position).getCity(),holder);
            holder.mOp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    ViewHolder holder = (ViewHolder) v.getTag(R.id.id_view);
                    holder.mProgressBar.setMax(100);
                    OfflineMapCity city = (OfflineMapCity) v.getTag(R.id.id_model);
                    try {
                        mOfflineMgr.downloadByCityCode(city.getCode());
                    } catch (AMapException e) {
                        e.printStackTrace();
                    }
                }
            });
            return convertView;
        }


    }

    static class ViewHolder{
        TextView mCityName;
        NumberProgressBar mProgressBar;
        TextView mStatus;
        Button mOp;
    }


}
