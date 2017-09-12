package com.inktech.autoseal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class SettingsPreferenceActivity extends AppCompatActivity {

    AppCompatButton btnSaveIp;
    AppCompatEditText editServerIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editServerIp=(AppCompatEditText) findViewById(R.id.edit_server_ip);
        btnSaveIp=(AppCompatButton) findViewById(R.id.btn_save_ip);
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
        String savedIp=pref.getString("serverIP","192.168.");
        editServerIp.setText(savedIp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}