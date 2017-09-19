package com.coolweather.android.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.R;
import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 董长峰 on 2017/9/14.
 */

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist=new ArrayList<>(  );
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate( R.layout.choose_area,container,false );
        titleText= (TextView) view.findViewById( R.id.title_text );
        backButton= (Button) view.findViewById( R.id.back_btn );
        listView= (ListView) view.findViewById( R.id.list_view );
        adapter=new ArrayAdapter<String>( getContext(),android.R.layout.simple_list_item_1,datalist );
        listView.setAdapter( adapter );
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated( savedInstanceState );
        queryProvinces();
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
            }
        } );
        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        } );

    }
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility( View.GONE );
        provinceList= DataSupport.findAll( Province.class);
        if (provinceList.size()>0){
            datalist.clear();
            for (Province province : provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection( 0 );
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address1="http://guolin.tech/api/china";
            queryFromServer( address1,"province" );
        }
    }
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid= ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            datalist.clear();
            for (City city: cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address2="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address2,"city");
        }
    }
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility( View.VISIBLE );
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            datalist.clear();
            for (County county: countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address3="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address3,"county" );
        }
    }
    private void queryFromServer(String address,final String type){
        //显示dialog
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("city".equals( type )){
                    result=Utility.handleCityResponse( responseText,selectedProvince.getId() );
                }
                else if ("province".equals( type )){
                    result= Utility.handleProvinceResponse( responseText );
                }else if ("county".equals( type )){
                    result=Utility.handleCountyRequest( responseText,selectedCity.getId() );
                }
                if (result){
                    getActivity().runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            //关闭dialog
                            closeProgressDialog();
                            if ("province".equals( type )){
                                queryProvinces();
                            }else if ("city".equals( type )){
                                queryCities();
                            }else if ("county".equals( type )){
                                queryCounties();
                            }
                        }
                    } );
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        //关闭dialog
                        closeProgressDialog();
                        Toast.makeText( getContext(), "加载失败", Toast.LENGTH_SHORT ).show();
                    }
                } );
            }

        } );

    }
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage( "正在加载" );
            progressDialog.setCanceledOnTouchOutside( false );
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
