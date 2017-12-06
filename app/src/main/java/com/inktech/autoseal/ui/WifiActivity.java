package com.inktech.autoseal.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.inktech.autoseal.R;
import com.inktech.autoseal.adapter.WifiAdapter;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.util.WifiAdmin;
import com.thanosfisherman.wifiutils.WifiUtils;

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
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString(ssid, pw);   //保存密码
                        editor.commit();
                        //mWifiAdmin.addNetwork(mWifiAdmin.CreateWifiInfo(ssid, et_password.getText().toString(), 3));
                        WifiUtils.withContext(getApplicationContext())
                                .connectWith(ssid, et_password.getText().toString())
                                .onConnectionResult(this::checkResult)
                                .start();
                    }

                    private void checkResult(boolean isSuccess)
                    {
                        if (isSuccess){
                            Toast.makeText(WifiActivity.this, "WiFi连接成功", Toast.LENGTH_SHORT).show();
                            scanWifi();
                        }
                        else
                            Toast.makeText(WifiActivity.this, "WiFi密码错误", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.setNegativeButton("取消连接", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWifiAdmin.removeWifi(mWifiAdmin.getNetworkId());
                    }
                });
                alert.create();
                alert.show();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mWifiAdmin.isWifiEnabled()){
            btnOpenWifi.setText("WiFi已开启");
            scanWifiCheckPermission();
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
                scanWifiCheckPermission();
                btnOpenWifi.setText("WiFi开启中……");
                mWifiAdmin.openWifi(WifiActivity.this);

                btnOpenWifi.setText("WiFi已开启");
                break;
            case R.id.btn_close_wifi:
                mWifiAdmin.closeWifi(WifiActivity.this);
                btnOpenWifi.setText("开启WiFi");
                if(mWifiList!=null){
                    mWifiList.removeAll(mWifiList);
                    wifiAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private void scanWifi(){
        WifiUtils.withContext(getApplicationContext()).scanWifi(this::getScanResults).start();
    }

    private void getScanResults(@NonNull final List<ScanResult> results)
    {
        mWifiList=results;
        if (results.isEmpty())
        {
            return;
        }
        wifiAdapter=new WifiAdapter(WifiActivity.this,mWifiList,mWifiAdmin);
        mlistView.setAdapter(wifiAdapter);
    }

    //监听wifi状态
    private BroadcastReceiver mReceiver = new BroadcastReceiver (){
        @Override
        public void onReceive(Context context, Intent intent) {
            scanWifiCheckPermission();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    private void scanWifiCheckPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_GRANT_PERMISSION);
        }else{
            scanWifi();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == Constants.REQUEST_GRANT_PERMISSION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                scanWifi();
            } else
            {
                // Permission Denied
                Toast.makeText(WifiActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
