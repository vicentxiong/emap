package com.eshion.emap;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.RouteSearch;

import java.util.Iterator;
import java.util.List;

/**
 * Created by eshion on 16-11-21.
 */
public class RoutePlanningActivity extends AppCompatActivity implements View.OnClickListener, GeocodeSearch.OnGeocodeSearchListener, AdapterView.OnItemClickListener, PoiSearch.OnPoiSearchListener, MapApplication.MyLocationChangeListener {
    private AutoCompleteTextView mFrom,mTo;
    private Button mDrive,mBus,mWalk;
    private ArrayAdapter<String> addresses ;
    private GeocodeSearch mGeocodeSearch;
    private LatLonPoint fromPoint,toPoint;
    private List<GeocodeAddress> addressAll;
    List<PoiItem> allPoiItem;

    private PoiSearch mPoiSearch;

    private boolean itemSelect = false;
    private FromOrTo mState = FromOrTo.From;
    private MapApplication application;

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        Log.d("xx","onPoiSearched = " + i);
        if(i==1000 && !itemSelect){
            addresses = new ArrayAdapter<String>(this,R.layout.mysimple_dropdown_item_1line);
            allPoiItem = poiResult.getPois();
            for (int index=0;index<allPoiItem.size();index++){

                addresses.add(allPoiItem.get(index).getTitle());
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (mState){
                        case From:
                            mFrom.setAdapter(addresses);
                            mFrom.showDropDown();
                            break;
                        case To:
                            mTo.setAdapter(addresses);
                            mTo.showDropDown();
                            break;
                    }

                }
            });
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void dispatcherLocationChange(AMapLocation aMapLocation) {
        fromPoint = new LatLonPoint(aMapLocation.getLatitude(),aMapLocation.getLongitude());
        mFrom.setHint(aMapLocation.getAddress());
    }

    enum FromOrTo{
        From,
        To
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.route_planning);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        application = (MapApplication) getApplication();
        addresses = new ArrayAdapter<String>(getApplicationContext(),R.layout.mysimple_dropdown_item_1line);

        mGeocodeSearch = new GeocodeSearch(this);
        mGeocodeSearch.setOnGeocodeSearchListener(this);

        mFrom = (AutoCompleteTextView) findViewById(R.id.fromTv);
        mTo = (AutoCompleteTextView) findViewById(R.id.toTv);
        mDrive = (Button) findViewById(R.id.drive);
        mBus = (Button) findViewById(R.id.bus);
        mWalk = (Button) findViewById(R.id.walk);

        mDrive.setOnClickListener(this);
        mBus.setOnClickListener(this);
        mWalk.setOnClickListener(this);
        mFrom.setThreshold(Integer.MAX_VALUE);
        mTo.setThreshold(Integer.MIN_VALUE);
        mFrom.setAdapter(addresses);
        mTo.setAdapter(addresses);

        mFrom.addTextChangedListener(new FromTextWatcher());
        mTo.addTextChangedListener(new ToTextWatcher());

        mFrom.setOnItemClickListener(this);
        mTo.setOnItemClickListener(this);

        application = (MapApplication) getApplication();
        application.resgiterLocationChangeDispatcher(this);
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Intent result = new Intent();
        result.putExtra("from_and_to",new RouteSearch.FromAndTo(fromPoint,toPoint));
        switch (v.getId()){
            case R.id.drive:
                result.putExtra("mode",R.id.id_drive);
                break;
            case R.id.bus:
                result.putExtra("mode",R.id.id_bus);
                break;
            case R.id.walk:
                result.putExtra("mode",R.id.id_walk);
                break;
        }
        setResult(RESULT_OK,result);
        finish();
    }

    class ToTextWatcher implements TextWatcher{

        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * are about to be replaced by new text with length <code>after</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         *
         * @param s
         * @param start
         * @param count
         * @param after
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            itemSelect =false;
            Log.d("xx","beforeTextChanged");
        }

        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * have just replaced old text that had length <code>before</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         *
         * @param s
         * @param start
         * @param before
         * @param count
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("xx", "onTextChanged = " + s);
           /* GeocodeQuery query = new GeocodeQuery(s.toString(), MainActivity.cityCode);

            mGeocodeSearch.getFromLocationNameAsyn(query);*/
            PoiSearch.Query pq = new PoiSearch.Query(s.toString(),null,application.getLocation().getCityCode());
            if(mPoiSearch==null){
                mPoiSearch = new PoiSearch(getApplicationContext(),pq);
                mPoiSearch.setOnPoiSearchListener(RoutePlanningActivity.this);
            }else {
                mPoiSearch.setQuery(pq);
            }

            mPoiSearch.searchPOIAsyn();

            mState = FromOrTo.To;
        }

        /**
         * This method is called to notify you that, somewhere within
         * <code>s</code>, the text has been changed.
         * It is legitimate to make further changes to <code>s</code> from
         * this callback, but be careful not to get yourself into an infinite
         * loop, because any changes you make will cause this method to be
         * called again recursively.
         * (You are not told where the change took place because other
         * afterTextChanged() methods may already have made other changes
         * and invalidated the offsets.  But if you need to know here,
         * you can use {@link Spannable#setSpan} in {@link #onTextChanged}
         * to mark your place and then look up from here where the span
         * ended up.
         *
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    class FromTextWatcher implements TextWatcher{
        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * are about to be replaced by new text with length <code>after</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         *
         * @param s
         * @param start
         * @param count
         * @param after
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            itemSelect =false;
            Log.d("xx","beforeTextChanged");
        }

        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * have just replaced old text that had length <code>before</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback.
         *
         * @param s
         * @param start
         * @param before
         * @param count
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("xx", "onTextChanged = " + s);
           /* GeocodeQuery query = new GeocodeQuery(s.toString(), MainActivity.cityCode);

            mGeocodeSearch.getFromLocationNameAsyn(query);*/
            PoiSearch.Query pq = new PoiSearch.Query(s.toString(),null,application.getLocation().getCityCode());
            if(mPoiSearch==null){
                mPoiSearch = new PoiSearch(getApplicationContext(),pq);
                mPoiSearch.setOnPoiSearchListener(RoutePlanningActivity.this);
            }else {
                mPoiSearch.setQuery(pq);
            }

            mPoiSearch.searchPOIAsyn();
            mState = FromOrTo.From;
        }

        /**
         * This method is called to notify you that, somewhere within
         * <code>s</code>, the text has been changed.
         * It is legitimate to make further changes to <code>s</code> from
         * this callback, but be careful not to get yourself into an infinite
         * loop, because any changes you make will cause this method to be
         * called again recursively.
         * (You are not told where the change took place because other
         * afterTextChanged() methods may already have made other changes
         * and invalidated the offsets.  But if you need to know here,
         * you can use {@link Spannable#setSpan} in {@link #onTextChanged}
         * to mark your place and then look up from here where the span
         * ended up.
         *
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) {
            Log.d("xx","afterTextChanged");
        }
    }



    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        Log.d("xx","ResultCode = " + i + " isshow = " +  mFrom.isPopupShowing());

        if(i==1000 && !itemSelect){

            addresses = new ArrayAdapter<String>(this,R.layout.mysimple_dropdown_item_1line);
            addressAll = geocodeResult.getGeocodeAddressList();
            Log.d("xx", "ResultSize = " + addressAll.size());
            for (int index=0;index<addressAll.size();index++){
                Log.d("xx", addressAll.get(index).getFormatAddress());
                addresses.add(addressAll.get(index).getFormatAddress());
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (mState){
                        case From:
                            mFrom.setAdapter(addresses);
                            mFrom.showDropDown();
                            break;
                        case To:
                            mTo.setAdapter(addresses);
                            mTo.showDropDown();
                            break;
                    }

                }
            });

        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("xx","onItemClick");
        switch (mState){
            case From:
                application.resgiterLocationChangeDispatcher(null);
                fromPoint = allPoiItem.get(position).getLatLonPoint();

                break;
            case To:
                toPoint = allPoiItem.get(position).getLatLonPoint();
                break;
        }
        itemSelect = true;
    }
}
