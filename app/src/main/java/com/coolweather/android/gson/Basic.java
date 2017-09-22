package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.jar.Attributes;

/**
 * Created by 董长峰 on 2017/9/19.
 */

public class Basic {
    @SerializedName( "city" )
    public String cityName;
    @SerializedName( "id" )
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName( "loc" )
        public String updateTime;
    }
}
