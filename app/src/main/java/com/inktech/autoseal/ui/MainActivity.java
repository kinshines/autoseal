package com.inktech.autoseal.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.inktech.autoseal.R;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.service.SyncService;
import com.inktech.autoseal.util.AesCryptoUtil;
import com.inktech.autoseal.util.PreferenceUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private UsingSealFragment usingSealFragment;
    private UsingSealOfflineFragment usingSealOfflineFragment;
    private GetSealFragment getSealFragment;
    private GetSealOfflineFragment getSealOfflineFragment;
    private ReturnSealFragment returnSealFragment;

    BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        if (savedInstanceState != null) {
            FragmentManager manager = getSupportFragmentManager();
            usingSealFragment = (UsingSealFragment) manager.getFragment(savedInstanceState, "usingSealFragment");
            usingSealOfflineFragment=(UsingSealOfflineFragment) manager.getFragment(savedInstanceState,"usingSealOfflineFragment");
            getSealFragment=(GetSealFragment) manager.getFragment(savedInstanceState,"getSealFragment");
            getSealOfflineFragment=(GetSealOfflineFragment) manager.getFragment(savedInstanceState,"getSealOfflineFragment");
            returnSealFragment=(ReturnSealFragment) manager.getFragment(savedInstanceState,"returnSealFragment");
        } else {
            usingSealFragment = new UsingSealFragment();
            usingSealOfflineFragment=new UsingSealOfflineFragment();
            getSealFragment=new GetSealFragment();
            getSealOfflineFragment=new GetSealOfflineFragment();
            returnSealFragment=new ReturnSealFragment();
        }

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .add(R.id.container_main, usingSealFragment, "usingCodeFragment")
                .commit();
        manager.beginTransaction()
                .add(R.id.container_main,usingSealOfflineFragment,"usingOfflineCodeFragment")
                .commit();
        manager.beginTransaction()
                .add(R.id.container_main,getSealFragment,"getSealFragment")
                .commit();
        manager.beginTransaction()
                .add(R.id.container_main,getSealOfflineFragment,"getSealOfflineFragment")
                .commit();
        manager.beginTransaction()
                .add(R.id.container_main,returnSealFragment,"returnSealFragment")
                .commit();

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId){
                    case R.id.tab_using_seal:
                        showHideFragment(0);
                        break;
                    case R.id.tab_get_seal:
                        showHideFragment(1);
                        break;
                    case R.id.tab_using_seal_offline:
                        showHideFragment(2);
                        break;
                    case R.id.tab_get_seal_offline:
                        showHideFragment(3);
                        break;
                    case R.id.tab_return_seal:
                        showHideFragment(4);
                        break;
                }
            }
        });

        Intent intent = getIntent();
        if (intent.getAction().equals(Constants.ACTION_GET_SEAL_OFFLINE)) {
            bottomBar.selectTabAtPosition(3);
        } else if (intent.getAction().equals(Constants.ACTION_USING_SEAL_OFFLINE)){
            bottomBar.selectTabAtPosition(2);
        } else if(intent.getAction().equals(Constants.ACTION_GET_SEAL)) {
            bottomBar.selectTabAtPosition(1);
        } else if(intent.getAction().equals(Constants.ACTION_RETURN_SEAL)){
            bottomBar.selectTabAtPosition(4);
        }else {
            bottomBar.selectTabAtPosition(0);
        }

        Intent serviceIntent=new Intent(this, SyncService.class);
        startService(serviceIntent);

        String hardwareCode=PreferenceUtil.getHardwareCode();
        if(TextUtils.isEmpty(hardwareCode)){
            final AppCompatEditText editSerialNo=new AppCompatEditText(MainActivity.this);
            AlertDialog alert=new AlertDialog.Builder(MainActivity.this)
                    .setView(editSerialNo)
                    .setCancelable(false)
                    .setTitle("请输入产品序列号")
                    .setPositiveButton("确定",null)
                    .create();
            alert.show();
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String serialNo = editSerialNo.getText().toString();
                    String clearText=AesCryptoUtil.decrypt(Constants.AesKey,serialNo);
                    if(TextUtils.isEmpty(clearText)){
                        editSerialNo.setError("无效的序列号");
                        return;
                    }
                    if(PreferenceUtil.checkSerialValide(clearText)){
                        alert.dismiss();
                    }else{
                        editSerialNo.setError("无效的序列号");
                        return;
                    }
                }
            });
        }
    }
    private void initViews() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bottomBar=(BottomBar) findViewById(R.id.bottomBar);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.about:
                startActivity(new Intent(MainActivity.this,SettingsPreferenceActivity.class));
                break;
            case R.id.wifi:
                startActivity(new Intent(MainActivity.this,WifiActivity.class));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (usingSealFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "usingSealFragment", usingSealFragment);
        }
        if (usingSealOfflineFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "usingSealOfflineFragment", usingSealOfflineFragment);
        }
        if (getSealFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "getSealFragment", getSealFragment);
        }
        if (getSealOfflineFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "getSealOfflineFragment", getSealOfflineFragment);
        }
        if(returnSealFragment.isAdded()){
            getSupportFragmentManager().putFragment(outState,"returnSealFragment",returnSealFragment);
        }

    }

    /**
     * show or hide the fragment
     * and handle other operations like set toolbar's title
     * set the navigation's checked item
     * @param position which fragment to show, only 3 values at this time
     *                 0 for translate fragment
     *                 1 for daily one fragment
     *                 2 for notebook fragment
     */
    private void showHideFragment(@IntRange(from = 0, to = 4) int position) {

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().hide(usingSealFragment).commit();
        manager.beginTransaction().hide(usingSealOfflineFragment).commit();
        manager.beginTransaction().hide(getSealFragment).commit();
        manager.beginTransaction().hide(getSealOfflineFragment).commit();
        manager.beginTransaction().hide(returnSealFragment).commit();

        if (position == 0) {
            manager.beginTransaction().show(usingSealFragment).commit();
            toolbar.setTitle(R.string.using_seal);
        }else if(position==1){
            manager.beginTransaction().show(getSealFragment).commit();
            toolbar.setTitle(R.string.get_seal);
        }else if(position==2){
            manager.beginTransaction().show(usingSealOfflineFragment).commit();
            toolbar.setTitle(R.string.using_seal_offline);
        }else if(position==3){
            manager.beginTransaction().show(getSealOfflineFragment).commit();
            toolbar.setTitle(R.string.get_seal_offline);
        }else if(position==4){
            manager.beginTransaction().show(returnSealFragment).commit();
            toolbar.setTitle(R.string.return_seal);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
    }

    /**
     * 点击返回键两次退出程序
     */
    private long exitTime = 0;
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else{
                finish();
                System.exit(0);
                Process.killProcess(Process.myPid());
            }
        }
    }
}
