package com.inktech.autoseal.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.inktech.autoseal.R;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.util.PreferenceUtil;

public class SettingsPreferenceActivity extends AppCompatActivity {

    AppCompatButton btnSaveIp;
    AppCompatEditText editServerIp;

    AppCompatTextView textUsingCode;
    AppCompatTextView textOutCode;
    AppCompatTextView textHardwareCode;
    AppCompatTextView textBluetoothCode;
    AppCompatTextView textOutSealRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textOutCode=(AppCompatTextView) findViewById(R.id.text_out_code);
        textUsingCode=(AppCompatTextView) findViewById(R.id.text_using_code);
        editServerIp=(AppCompatEditText) findViewById(R.id.edit_server_ip);
        btnSaveIp=(AppCompatButton) findViewById(R.id.btn_save_ip);
        textHardwareCode=(AppCompatTextView) findViewById(R.id.text_hardware_code);
        textBluetoothCode=(AppCompatTextView) findViewById(R.id.text_bluetooth_code);
        textOutSealRecord=(AppCompatTextView) findViewById(R.id.out_seal_record);
        SharedPreferences pref  = PreferenceManager.getDefaultSharedPreferences(SettingsPreferenceActivity.this);

        btnSaveIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=pref.edit();
                editor.putString("serverIP",editServerIp.getText().toString());
                editor.apply();
                Toast.makeText(SettingsPreferenceActivity.this,"success",Toast.LENGTH_SHORT).show();
            }
        });

        String savedIp=pref.getString("serverIP","124.128.33.110:10003");
        editServerIp.setText(savedIp);

        String savedhardware= PreferenceUtil.getHardwareCode();
        textHardwareCode.setText("硬件编码："+savedhardware);
        String savedBluetooth=PreferenceUtil.getBluetoothPairCode();
        textBluetoothCode.setText("蓝牙配对码："+savedBluetooth);

        textUsingCode.setText(pref.getString(Constants.OfflineUsingSealCode,""));
        textOutCode.setText(pref.getString(Constants.OfflineOutSealCode,""));
        textOutSealRecord.setText(pref.getString(Constants.OutSealRecord,""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}