package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.json.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    //处理省级数据
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvince = new JSONArray(response);
                //遍历所有省
                for(int i=0;i<allProvince.length();i++){
                    JSONObject provinceObject  = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }
    //处理市级数据
    public static boolean handleCountyResponse(String response,int cityCode) {
        if(!TextUtils.isEmpty(response)) {
            try{
                JSONArray allCounty = new JSONArray(response);
                for(int i=0;i<allCounty.length();i++){
                    JSONObject countyObject  = allCounty.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeather_id(countyObject.getString("weather_id"));
                    county.setCityCode(cityCode);
                    county.save();
                }


        }catch (JSONException e){
                e.printStackTrace();
            }
            return true;

        }
        return false;

    }

    public static boolean handleCityResponse(String response,int provinceCode){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCity = new JSONArray(response);
                for(int i =0;i<allCity.length();i++){
                    JSONObject cityObject  = allCity.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setPrivinceCode(provinceCode);
                    city.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    //处理某县某日天气信息
    public static Weather handleWeatherRespons(String response){
        try {
            JSONObject jsonObject =new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);

        }catch (Exception e){
            e.printStackTrace();

        }
        return null;
    }
}
