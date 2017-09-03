package com.inktech.autoseal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inktech.autoseal.Util.BitmapUtil;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private UsingCodeFragment usingCodeFragment;


    private static final String ACTION_NOTEBOOK = "com.marktony.translator.notebook";
    private static final String ACTION_DAILY_ONE = "com.marktony.translator.dailyone";

    private EditText editCode;
    private Button btnCode;
    private Button takePhoto;
    private Button btnTakePhotoBack;
    private Button btnTakePhotoFront;
    private Button searchBluetoothDevice;
    private TextView sealInfo;
    private String result;
    private Uri imageUri;
    private ImageView pictureView;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView lvDevices;
    private List<String> bluetoothDevices = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private final UUID MY_UUID = UUID
            .fromString("00001104-0000-1000-8000-00805F9B34FB");//随便定义一个
    private BluetoothSocket clientSocket;
    private BluetoothDevice device;
    private OutputStream os;//输出流

    private static final String AddressnameSpace = "http://tempuri.org/";
    private static final String WebServiceUrl="http://192.168.0.101:8020/Api.asmx";
    private static final String UploadByMethod="uploadByUsing";
    private static final String UploadByAction="http://tempuri.org/uploadByUsing";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        //bindViews();

        if (savedInstanceState != null) {
            FragmentManager manager = getSupportFragmentManager();
            usingCodeFragment = (UsingCodeFragment) manager.getFragment(savedInstanceState, "usingCodeFragment");
        } else {
            usingCodeFragment = new UsingCodeFragment();
        }

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .add(R.id.container_main, usingCodeFragment, "usingCodeFragment")
                .commit();

        Intent intent = getIntent();
        if (intent.getAction().equals(ACTION_NOTEBOOK)) {
            showHideFragment(2);
        } else if (intent.getAction().equals(ACTION_DAILY_ONE)){
            showHideFragment(1);
        } else {
            showHideFragment(0);
        }
    }
    private void initViews() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            case R.id.add_city:
                Toast.makeText(MainActivity.this, "此功能再下个版本添加！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.multi_cities:
                Toast.makeText(MainActivity.this, "此功能再下个版本添加！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about:
                Toast.makeText(MainActivity.this, "此功能再下个版本添加！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(MainActivity.this, "此功能再下个版本添加！", Toast.LENGTH_SHORT).show();
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
        if (usingCodeFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "usingCodeFragment", usingCodeFragment);
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
    private void showHideFragment(@IntRange(from = 0, to = 2) int position) {

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().hide(usingCodeFragment).commit();

        if (position == 0) {
            manager.beginTransaction().show(usingCodeFragment).commit();
            toolbar.setTitle(R.string.app_name);
            navigationView.setCheckedItem(R.id.add_city);
        }

    }

    private void accessCoarseLocation(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.btn_code:
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        getSealInfo();
//                    }
//                }).start();
//                break;
////            case R.id.take_photo:
////                takePhotoIntent();
////                break;
//            case R.id.search_bluetooth_device:
//                openBluetoothIntent();
//                break;
//            case R.id.take_photo_back:
//                takePhotoBack();
//                break;
//            case R.id.take_photo_front:
//                takePhotoFront();
//                break;
//        }
    }

    public void takePhotoBack(){
        startActivityForResult(new Intent(this,PhotoBackActivity.class),0);
    }
    public void takePhotoFront(){
        startActivityForResult(new Intent(this,PhotoFrontActivity.class),1);
    }


    public void uploadImage(String sealCode,String filename,byte[] fileByte,String position){
        result="";
        SoapObject request=new SoapObject(AddressnameSpace,UploadByMethod);
        request.addProperty("sealCode",sealCode);
        request.addProperty("filename",filename);
        request.addProperty("position",position);
        request.addProperty("fileByte",fileByte);

        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut=request;
        envelope.dotNet=true;
        envelope.setOutputSoapObject(request);
        new MarshalBase64().register(envelope);
        HttpTransportSE httpTransportSE=new HttpTransportSE(WebServiceUrl);
        try {
            httpTransportSE.call(UploadByAction,envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoapObject object=(SoapObject)envelope.bodyIn;
        SoapObject sealInfoResult=(SoapObject)object.getProperty("uploadByUsingResult");
        SoapObject outerResult=(SoapObject)sealInfoResult.getProperty("result");
        int sealStatus=Integer.parseInt(outerResult.getPropertySafelyAsString("status"));
        String message=outerResult.getProperty("message").toString();
        if(sealStatus==0){
            result="照片上传失败："+message;
            handler.sendEmptyMessage(0x012);
            return;
        }
        if(sealStatus==1){
            result="照片上传成功";
            handler.sendEmptyMessage(0x011);
            return;
        }
        result="照片上传失败，错误码："+sealStatus+"，"+message;
        handler.sendEmptyMessage(0x013);
    }

    public void takePhotoIntent(){
        File outputImage=new File(getExternalCacheDir(),System.currentTimeMillis()+".jpg");
        try{
            if(outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if(Build.VERSION.SDK_INT>=24){
            imageUri= FileProvider.getUriForFile(MainActivity.this,"com.inktech.autoseal.fileprovider",outputImage);
        }else {
            imageUri=Uri.fromFile(outputImage);
        }

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, 3);
    }

    public void openBluetoothIntent(){
        accessCoarseLocation();
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        boolean enabled = mBluetoothAdapter.isEnabled();
        if(!enabled){
            mBluetoothAdapter.enable();
        }
        Set<BluetoothDevice> pairedDevices  = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevices.add(device.getName() + ":"+ device.getAddress());
            }
        }


        // 设置广播信息过滤
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//每搜索到一个设备就会发送一个该广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//当全部搜索完后发送该广播
        filter.setPriority(Integer.MAX_VALUE);//设置优先级
// 注册蓝牙搜索广播接收者，接收并处理搜索结果
        this.registerReceiver(receiver, filter);

        discoveryDevice();
    }


    public void discoveryDevice(){
        //如果当前在搜索，就先取消搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //开启搜索
        mBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    bluetoothDevices.add(device.getName() + ":"+ device.getAddress());
                    arrayAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜素完成
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 0:
            case 1:
                String filePath=data.getStringExtra("filePath");
                File file=new File(filePath);
                Uri uploadImageUri=Uri.fromFile(file);
                if(resultCode==RESULT_OK){
                    uploadBitmapByUri(uploadImageUri);
                }
            case 3:
                if(resultCode==RESULT_OK){
                    uploadBitmapByUri(imageUri);
                }
                break;
        }
    }

    private void uploadBitmapByUri(Uri uploadImageUri){
        try {
            final Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(uploadImageUri));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            final byte[] array=out.toByteArray();
            //pictureView.setImageBitmap(BitmapUtil.decodeSampledBitmap(this,imageUri.getPath()));
            String filePath=uploadImageUri.getPath();
            final String fileName=filePath.substring(filePath.lastIndexOf('/')+1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadImage("12345",fileName,array,"文档");
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x001:
                    sealInfo.setText(result);
                    break;
                case 0x002:
                case 0x003:
                    sealInfo.setText(result);
                    break;
                case 0x011:
                case 0x012:
                case 0x013:
                    Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        if(receiver.getDebugUnregister()){
            unregisterReceiver(receiver);
        }
        super.onDestroy();
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
