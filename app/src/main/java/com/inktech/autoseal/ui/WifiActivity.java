package com.inktech.autoseal.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.inktech.autoseal.R;
import com.inktech.autoseal.adapter.WifiAdapter;
import com.inktech.autoseal.util.WifiAdmin;

import java.util.List;

public class WifiActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnOpenWifi,btnCloseWifi;
    private ListView mlistView;
    protected WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    private WifiAdapter wifiAdapter;

    protected String ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mWifiAdmin = new WifiAdmin(WifiActivity.this);
        initViews();
        IntentFilter filter = new IntentFilter(
                WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //="android.net.wifi.STATE_CHANGE"  监听wifi状态的变化
        registerReceiver(mReceiver, filter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                AlertDialog.Builder alert=new AlertDialog.Builder(WifiActivity.this);
                ssid=mWifiList.get(position).SSID;
                alert.setTitle(ssid);
                alert.setMessage("输入密码");
                final EditText et_password=new EditText(WifiActivity.this);
                final SharedPreferences preferences=getSharedPreferences("wifi_password", Context.MODE_PRIVATE);
                et_password.setText(preferences.getString(ssid, ""));
                alert.setView(et_password);
                //alert.setView(view1);
                alert.setPositiveButton("连接", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pw = et_password.getText().toString();
                        if(null == pw  || pw.length() < 8){
                            Toast.makeText(WifiActivity.this, "密码至少8位", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString(ssid, pw);   //保存密码
                        editor.commit();
                        mWifiAdmin.addNetwork(mWifiAdmin.CreateWifiInfo(ssid, et_password.getText().toString(), 3));
                    }
                });
                alert.setNegativeButton("取消连接", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                        mWifiAdmin.removeWifi(mWifiAdmin.getNetworkId());
                    }
                });
                alert.create();
                alert.show();

            }
        });

        if(mWifiAdmin.isWifiEnabled()){
            scanWifi();
        }
    }

    /*
 * 控件初始化
 * */
    private void initViews() {
        btnOpenWifi=(AppCompatButton) findViewById(R.id.btn_open_wifi);
        btnCloseWifi=(AppCompatButton) findViewById(R.id.btn_close_wifi);
        mlistView=(ListView) findViewById(R.id.wifi_list);
        btnOpenWifi.setOnClickListener(WifiActivity.this);
        btnCloseWifi.setOnClickListener(WifiActivity.this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_open_wifi:
                btnOpenWifi.setText("WiFi 开启中……");
                mWifiAdmin.openWifi(WifiActivity.this);
                try {
                    Thread.sleep(3000);
                    scanWifi();
                    btnOpenWifi.setText("WiFi已开启");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btn_close_wifi:
                mWifiAdmin.closeWifi(WifiActivity.this);
                if(mWifiList!=null){
                    mWifiList.removeAll(mWifiList);
                    wifiAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private void scanWifi(){
        mWifiAdmin.startScan(WifiActivity.this);
        mWifiList=mWifiAdmin.getWifiList();
        if(mWifiList!=null){
            wifiAdapter=new WifiAdapter(this,mWifiList,mWifiAdmin);
            mlistView.setAdapter(wifiAdapter);
            setListViewHeightBasedOnChildren(mlistView);
        }
    }

    //监听wifi状态
    private BroadcastReceiver mReceiver = new BroadcastReceiver (){
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(wifiInfo.isConnected()){
                String wifiSSID = mWifiAdmin.getSSID();
                Toast.makeText(context, wifiSSID+"连接成功", Toast.LENGTH_SHORT).show();
            }
        }

    };

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
