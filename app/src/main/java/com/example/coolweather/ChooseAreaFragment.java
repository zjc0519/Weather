package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE =0;
    public static final int LEVEL_CITY =1;
    public static final int LEVEL_COUNTY =2;
    private  int currentLevel; //当前的访问状态 对应于LEVEL_PROVINCE LEVEL_CITY LEVEL_COUNTY

    private TextView titleText;
    private Button back_btn;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist = new ArrayList<>();
    private ProgressDialog progressDialog;//进度加载对话框
    private Province currentProvince;
    private City currentCity;
    //所有的省
    private  List<Province> provinceList;
    private  List<City> cityList;
    private  List<County> countyList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        //获取控件
        titleText =(TextView) view.findViewById(R.id.title_text);
        back_btn = (Button) view.findViewById(R.id.back_btn);
        listView = (ListView) view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if(event.getTargetState() == Lifecycle.State.CREATED){

                    //处理语句
                    queryProvince();
                    //添加点击事件
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        //如果列表显示的是省级数据，则点击某省显示对应的市级数据
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (currentLevel == LEVEL_PROVINCE){
                                currentProvince = provinceList.get(i);
                                queryCity();
                            }
                            //如果列表显示的是市级数据，则点击某省显示对应的市级数据
                            else if(currentLevel == LEVEL_CITY){
                                currentCity = cityList.get(i);
                                queryCounty();
                            }
                        }
                    });
                    //添加点击返回事件
                    back_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //当前显示市级数据，则点击返回键返回显示省级数据
                            if(currentLevel== LEVEL_CITY){
                                queryProvince();
                            }else if(currentLevel == LEVEL_COUNTY){
                                queryCity();
                            }
                        }

                    });
                    requireActivity().getLifecycle().removeObserver(this);
                }
            }
        });

    }

    //从服务器获取数据
    public void queryFromService(String address,final String type){
        //显示进度对话框
        showProgressDialog();

        //调用函数，访问服务器
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgressDialog();
                    Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                }
            });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText, currentProvince.getProvinceCode());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText,currentCity.getCityCode());
                }
                if(result){
                    //涉及到UI界面，切换回到主线程进行相应的处理
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭进度对话框
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvince();
                            }else if ("city".equals(type)){
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                    //读取数据库，并显示在界面上


                }
            }
        });

    }
    //读取所有的省信息并显示在UI上
    //1如果数据库有信息则读取数据库
    //2数据库没有信息，则先连接服务器读取数据并存储，再读取数据库
    public void queryProvince(){
        titleText.setText("中国");
        back_btn.setVisibility(View.GONE); //显示省级数据时，不显示返回键
        //读取数据库，并判断是否为空
        provinceList = LitePal.findAll(Province.class);
        //如何数据库有数据，直接读取并显示
        if(provinceList.size()>0){
            datalist.clear();
            for (Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            //页面显示数据
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;

        }else{
            String address = "http://guolin.tech/api/china";
            queryFromService(address,"province");
        }

    }
    //获取市级数据
    public void queryCity(){
        titleText.setText(currentProvince.getProvinceName());
        back_btn.setVisibility(View.VISIBLE);
        //优先从数据库获取数据
        //查找当前选中的省下面的所有的市
      cityList = LitePal.where("provinceCode=?",String.valueOf(currentProvince.getProvinceCode())).find(City.class);
      //判断数据库是否为空
        if(cityList.size()>0){
            //数据库不为空，直接显示
            datalist.clear();
            for(City city:cityList){
                datalist.add(city.getCityName());
            }
            //页面显示
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //更新当前数据访问的状态
            currentLevel = LEVEL_CITY;
        }else{
            //数据库为空号，则去访问服务器
            String address = "http://guolin.tech/api/china/"+currentProvince.getProvinceCode();
            queryFromService(address,"city");

        }
    }
    //获取访问县级数据
    public void queryCounty(){
        titleText.setText(currentCity.getCityName());
        back_btn.setVisibility(View.VISIBLE);
        //优先从数据库获取数据
        //查找当前选中的省下面的所有的市
       countyList = LitePal.where("CityCode=?",String.valueOf(currentCity.getCityCode())).find(County.class);
        //判断数据库是否为空
        if(countyList.size()>0){
            //数据库不为空，直接显示
            datalist.clear();
            for(County county:countyList){
                datalist.add(county.getCountyName());
            }
            //页面显示
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //更新当前数据访问的状态
            currentLevel = LEVEL_COUNTY;
        }else{
            //数据库为空号，则去访问服务器
            String address = "http://guolin.tech/api/china/"+currentProvince.getProvinceCode()+"/"+currentCity.getCityCode();
            queryFromService(address,"county");

        }
    }
    //加载时显示进度对话框
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false); //用户点击对话框外部，对话框无法取消
        }
        progressDialog.show();
    }
    private  void closeProgressDialog(){
        if(progressDialog !=null){
            progressDialog.dismiss();
        }
    }
}
