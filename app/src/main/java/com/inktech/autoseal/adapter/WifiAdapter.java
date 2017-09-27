package com.inktech.autoseal.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inktech.autoseal.R;
import com.inktech.autoseal.constant.MyApplication;
import com.inktech.autoseal.util.WifiAdmin;

import java.util.List;

/**
 * Created by Chaoyu on 2017/9/28.
 */


public class WifiAdapter extends BaseAdapter {
    private static final String TAG = "WifiAdapter";
    LayoutInflater inflater;
    public int level;
    List<ScanResult> list;
    WifiAdmin wifiAdmin;
    public WifiAdapter(Context context, List<ScanResult> list,WifiAdmin wifiAdmin){
        this.inflater=LayoutInflater.from(context);
        this.list=list;
        this.wifiAdmin=wifiAdmin;
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @SuppressLint({ "ViewHolder", "InflateParams" })
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=null;
        view=inflater.inflate(R.layout.wifi_list_item, null);
        ScanResult scanResult = list.get(position);
        TextView wifi_ssid=(TextView) view.findViewById(R.id.ssid);
        ImageView wifi_level=(ImageView) view.findViewById(R.id.wifi_level);
        wifi_ssid.setText(scanResult.SSID);

        if(scanResult.BSSID.equals(wifiAdmin.getBSSID())){
            wifi_ssid.setText(wifi_ssid.getText()+"(已连接)");
            wifi_ssid.setTextColor(ContextCompat.getColor(MyApplication.getContext(),R.color.colorPrimary));
        }
        Log.i(TAG, "scanResult.SSID="+scanResult);
        level= WifiManager.calculateSignalLevel(scanResult.level,5);
        if(scanResult.capabilities.contains("WEP")||scanResult.capabilities.contains("PSK")||
                scanResult.capabilities.contains("EAP")){
            wifi_level.setImageResource(R.mipmap.wifi_signal_lock);
        }else{
            wifi_level.setImageResource(R.mipmap.wifi_signal_open);
        }
        wifi_level.setImageLevel(level);
        //判断信号强度，显示对应的指示图标
        return view;
    }
}
