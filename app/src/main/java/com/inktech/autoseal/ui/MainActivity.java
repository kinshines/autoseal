package com.inktech.autoseal.ui;

import android.content.Intent;
import android.os.Process;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.inktech.autoseal.R;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.service.SyncService;
import com.inktech.autoseal.util.WebServiceUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private UsingSealFragment usingSealFragment;
    private UsingSealOfflineFragment usingSealOfflineFragment;
    private GetSealFragment getSealFragment;
    private GetSealOfflineFragment getSealOfflineFragment;

    AppCompatEditText editUsingCode;
    AppCompatButton btnUsingCode;

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
        } else {
            usingSealFragment = new UsingSealFragment();
            usingSealOfflineFragment=new UsingSealOfflineFragment();
            getSealFragment=new GetSealFragment();
            getSealOfflineFragment=new GetSealOfflineFragment();
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

        Intent intent = getIntent();
        if (intent.getAction().equals(Constants.ACTION_GET_SEAL_OFFLINE)) {
            showHideFragment(3);
        } else if (intent.getAction().equals(Constants.ACTION_USING_SEAL_OFFLINE)){
            showHideFragment(2);
        } else if(intent.getAction().equals(Constants.ACTION_GET_SEAL)) {
            showHideFragment(1);
        } else {
            showHideFragment(0);
        }

        Intent serviceIntent=new Intent(this, SyncService.class);
        startService(serviceIntent);
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

        editUsingCode = (AppCompatEditText) findViewById(R.id.edit_using_code);
        btnUsingCode= (AppCompatButton) findViewById(R.id.btn_using_code);
    }
//    private void bindViews(){
//        editCode=(EditText)findViewById(R.id.edit_code);
//        btnCode=(Button)findViewById(R.id.btn_code);
//        sealInfo=(TextView)findViewById(R.id.seal_info);
//        btnCode.setOnClickListener(this);
////        takePhoto=(Button)findViewById(R.id.take_photo);
////        takePhoto.setOnClickListener(this);
////        pictureView=(ImageView)findViewById(R.id.picture_view);
//        searchBluetoothDevice=(Button)findViewById(R.id.search_bluetooth_device);
//        searchBluetoothDevice.setOnClickListener(this);
//        lvDevices = (ListView) findViewById(R.id.lv_devices);
//        btnTakePhotoBack=(Button)findViewById(R.id.take_photo_back);
//        btnTakePhotoFront=(Button)findViewById(R.id.take_photo_front);
//        btnTakePhotoFront.setOnClickListener(this);
//        btnTakePhotoBack.setOnClickListener(this);
//
//        arrayAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, android.R.id.text1,bluetoothDevices);
//        lvDevices.setAdapter(arrayAdapter);
//        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String s = arrayAdapter.getItem(position);
//                String address = s.substring(s.indexOf(":") + 1).trim();//把地址解析出来
//                //主动连接蓝牙服务端
//                try {
//                    //判断当前是否正在搜索
//                    if (mBluetoothAdapter.isDiscovering()) {
//                        mBluetoothAdapter.cancelDiscovery();
//                    }
//                    try {
//                        if (device == null) {
//                            //获得远程设备
//                            device = mBluetoothAdapter.getRemoteDevice(address);
//                        }
//                        if (clientSocket == null) {
//                            Method method=device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//                            clientSocket=(BluetoothSocket) method.invoke(device, 1);
//                            //创建客户端蓝牙Socket
//                            //clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//                            //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
//                            clientSocket.connect();
//                        }
//                        //获得输出流（客户端指向服务端输出文本）
//                        os = clientSocket.getOutputStream();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    if (os != null) {
//                        //往服务端写信息
//                        os.write("蓝牙信息来了".getBytes("utf-8"));
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_using_seal:
                showHideFragment(0);
                break;
            case R.id.menu_get_seal:
                showHideFragment(1);
                break;
            case R.id.menu_using_seal_offline:
                showHideFragment(2);
                break;
            case R.id.menu_get_seal_offline:
                showHideFragment(3);
                break;
            case R.id.about:
                startActivity(new Intent(MainActivity.this,SettingsPreferenceActivity.class));
                break;
            case R.id.exit:
                finish();
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
    private void showHideFragment(@IntRange(from = 0, to = 3) int position) {

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().hide(usingSealFragment).commit();
        manager.beginTransaction().hide(usingSealOfflineFragment).commit();
        manager.beginTransaction().hide(getSealFragment).commit();
        manager.beginTransaction().hide(getSealOfflineFragment).commit();


        if (position == 0) {
            manager.beginTransaction().show(usingSealFragment).commit();
            toolbar.setTitle(R.string.using_seal);
            navigationView.setCheckedItem(R.id.menu_using_seal);
        }else if(position==1){
            manager.beginTransaction().show(getSealFragment).commit();
            toolbar.setTitle(R.string.get_seal);
            navigationView.setCheckedItem(R.id.menu_get_seal);
        }else if(position==2){
            manager.beginTransaction().show(usingSealOfflineFragment).commit();
            toolbar.setTitle(R.string.using_seal_offline);
            navigationView.setCheckedItem(R.id.menu_using_seal_offline);
        }else if(position==3){
            manager.beginTransaction().show(getSealOfflineFragment).commit();
            toolbar.setTitle(R.string.get_seal_offline);
            navigationView.setCheckedItem(R.id.menu_get_seal_offline);
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
        switch (requestCode){
        }
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
