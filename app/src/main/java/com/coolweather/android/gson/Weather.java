package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by 董长峰 on 2017/9/19.
 */

public class Weather {
    public String status;
    public Basic basic;
    public aqi aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName( "daily_forecast" )
    public List<Forecast> forecastList;
}
