package com.coolweather.android.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 董长峰 on 2017/9/14.
 */

public class Utility {
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty( response )){
            try {
                JSONArray allprovince=new JSONArray(response  );
                for (int i=0;i<allprovince.length();i++){
                    JSONObject provinceObject=allprovince.getJSONObject( i );
                    Province province=new Province();
                    province.setProvinceCode( provinceObject.getInt( "id" ) );
                    province.setProvinceName( provinceObject.getString( "name" ));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities=new JSONArray( response );
                for (int i=0;i<allCities.length();i++){
                    JSONObject cityObject=allCities.getJSONObject( i );
                    City city=new City();
                    city.setProvinceId( provinceId );
                    city.setCityName( cityObject.getString( "name" ));
                    city.setCityCode( cityObject.getInt( "id" ) );
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountyRequest(String response,int cityId){
        if (!TextUtils.isEmpty( response )) {
            try {
                JSONArray allCounties = new JSONArray( response );
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject( i );
                    County county = new County();
                    county.setCityId(cityId);
                    county.setWeatherId( countyObject.getString( "weather_id" ) );
                    county.setCountyName( countyObject.getString( "name" ) );
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray( "HeWeather" );
            String weatherContent=jsonArray.getJSONObject( 0 ).toString();
            return new Gson().fromJson( weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
