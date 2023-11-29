package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.coolweather.json.Forcast;
import com.example.coolweather.json.Weather;
import com.example.coolweather.util.Utility;

import java.io.File;

public class WeatherActivity extends AppCompatActivity {
    public TextView titleCity;
    public TextView titleUpdateTime;
    public TextView degreeText;
    public TextView weatherInfoText;
    public LinearLayout forecastLayout;
    public TextView apiText;
    public TextView pm25Text;
    public TextView comfortText;
    public TextView  carWashText;
    public TextView sportText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //初始化控件（获取控件实例，用于数据传递和显示）
        titleCity=(TextView) findViewById(R.id.title_city);
        titleUpdateTime=(TextView) findViewById(R.id.title_update_time);
        degreeText=(TextView) findViewById(R.id.degree_text);
        weatherInfoText =(TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);//用于动态加载天气预测的显示部分
        apiText =(TextView) findViewById(R.id.api_text);
        pm25Text=(TextView) findViewById(R.id.pm25_text);
        comfortText=(TextView) findViewById(R.id.comfort_text);
        carWashText=(TextView) findViewById(R.id.car_wash_text);
        sportText =(TextView) findViewById(R.id.sport_text);


        //获取数据并显示
        //从本地缓冲中获取数据（SharedPreferences 本质上是以键值对的方式存储数据)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString =prefs.getString("weather",null);
        if(weatherString!=null){
            //缓冲器中数据不为空，则直接读取并显示
            Weather weather = Utility.handleWeatherRespons(weatherString);
            //显示天气数据
            showWeatherInfo(weather);
        }else{
            //缓冲器中数据为空，则先服务器请求当前天气数据
        }

    }
    //将天气信息显示在界面上
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName1;
        String updateTime= weather.basic.update.updateTime.split("")[1];
        String degree= weather.now.temperature+"℃";
        String weatherInfo =weather.now.more.info;
        //将相应数据传递给相应控件，并显示
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //天气列表
        forecastLayout.removeAllViews();
        for(Forcast forcast:weather.forcastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout,false);
            //获取控件
            TextView dateText =(TextView) findViewById(R.id.data_text);
            TextView infoText=(TextView) findViewById(R.id.info_text);
            TextView maxText =(TextView) findViewById(R.id.max_info);
            TextView minText =(TextView) findViewById(R.id.min_text);
            //传递数据并显示
            dateText.setText(forcast.date);
            infoText.setText(forcast.more.info);
            maxText.setText(forcast.temperature.max);
            minText.setText(forcast.temperature.min);
            //添加每一天的天气预测
            forecastLayout.addView(view);
        }
        //空气质量
        if(weather.aqi!=null){
            apiText.setText(weather.aqi.aoiCity.aqi);
            pm25Text.setText(weather.aqi.aoiCity.pm25);
        }
        
        //生活建议
        String comfort ="舒适度："+weather.suggestion.comfort.info;
        String carWash ="洗车指数："+weather.suggestion.carWash.info;
        String sprot="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sprot);
    }
}