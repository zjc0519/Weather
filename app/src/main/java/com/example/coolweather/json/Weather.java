package com.example.coolweather.json;

import java.util.List;

public class Weather {
    public Basic basic;
    public Now now;
    public Suggestion suggestion;
    public AQI aqi;
    public List<Forcast> forcastList; //Forcast是单日天气预测信息，forcastList是多日天气预测集合
    public String status;//当前响应数据的状态，（不是布尔类型）
}
