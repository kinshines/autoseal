package com.inktech.autoseal.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.adapter.BluetoothCmdInterpreter;
import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.model.OutSealSummary;
import com.inktech.autoseal.model.UploadFileResponse;
import com.inktech.autoseal.service.BluetoothService;
import com.inktech.autoseal.util.BitmapUtil;
import com.inktech.autoseal.util.DbUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.model.UsingSealSummary;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import com.inktech.autoseal.R;
import com.inktech.autoseal.util.XmlParseUtil;

import dmax.dialog.SpotsDialog;

public class SealProcessActivity extends AppCompatActivity implements View.OnClickListener {

    BluetoothService bluetoothService;
    BluetoothDevice device;

    Toolbar toolbar;
    ProgressBar toolbalProgressBar;
    CoordinatorLayout coordinatorLayout;
    MenuItem reconnectButton;
    Snackbar snackTurnOn;
    myHandler handler;

    TextView textSealProcess;
    TextView textSealHead;
    AppCompatButton btnConfirmSeal;
    String sealProcessInfoCaption="";
    String sealProcessInfoHead="";
    private SurfaceView mySurfaceView;
    private SurfaceHolder myHolder;
    private Camera myCamera;
    AlertDialog loadingView;

    private static final String TAG = "SealProcessActivity";

    private String WebServiceMethod="";
    private boolean usingSealFlag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seal_process);

        initViews();
        snackTurnOn = Snackbar.make(coordinatorLayout, "Bluetooth turned off", Snackbar.LENGTH_INDEFINITE)
                .setAction("Turn On", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        enableBluetooth();
                    }
                });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setSupportActionBar(toolbar);

        handler = new myHandler(SealProcessActivity.this);

        assert getSupportActionBar() != null; // won't be null, lint error
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        device = intent.getExtras().getParcelable(Constants.EXTRA_DEVICE);
        WebServiceMethod=intent.getStringExtra(Constants.web_service_method);
        if(WebServiceUtil.uploadByUsing.equals(WebServiceMethod)||WebServiceUtil.uploadByUrgentUsing.equals(WebServiceMethod)){
            usingSealFlag=true;
        }
        bluetoothService = new BluetoothService(handler, device);

        refreshSealProcess();
        setTitle(device.getName());
    }

    @Override protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        bluetoothService.connect();
        Log.d(Constants.TAG, "Connecting");
    }

    @Override protected void onStop() {
        super.onStop();
        if (bluetoothService != null) {
            bluetoothService.stop();
            Log.d(Constants.TAG, "Stopping");
        }

        unregisterReceiver(mReceiver);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                setStatus("None");
            } else {
                setStatus("Error");
                Snackbar.make(coordinatorLayout, "Failed to enable bluetooth", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                enableBluetooth();
                            }
                        }).show();
            }
        }

    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != Constants.STATE_CONNECTED) {
            Snackbar.make(coordinatorLayout, "尚未连接盖章机", Snackbar.LENGTH_LONG)
                    .setAction("连接", new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            reconnect();
                        }
                    }).show();
        } else {
            byte[] send = BluetoothCmdInterpreter.getHexBytes(message);
            bluetoothService.write(send);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_menu, menu);
        reconnectButton = menu.findItem(R.id.action_reconnect);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                retunToHome();
                return true;
            case R.id.action_reconnect:
                reconnect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        snackTurnOn.show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        if (snackTurnOn.isShownOrQueued()) snackTurnOn.dismiss();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        reconnect();
                }
            }
        }
    };

    private void setStatus(String status) {
        toolbar.setSubtitle(status);
    }

    private void enableBluetooth() {
        setStatus("Enabling Bluetooth");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
    }

    private void reconnect() {
        reconnectButton.setVisible(false);
        bluetoothService.stop();
        bluetoothService.connect();
    }

    private void refreshSealProcess(){
        if(usingSealFlag){
            refreshUsingSealProcess();
        }else{
            refreshOutSealProcess();
        }
    }

    private void refreshUsingSealProcess(){
        String sealType= UsingSealSummary.getCurrentSealType();
        if(TextUtils.isEmpty(sealType)){
            sealProcessInfoHead="盖章完成!";
            sealProcessInfoCaption="";
            btnConfirmSeal.setVisibility(View.GONE);
            textSealHead.setText(sealProcessInfoHead);
            textSealProcess.setText(sealProcessInfoCaption);
            return;
        }
        String sealTypeChinese= UsingSealSummary.getSealTypeChinese();
        int currentCount= UsingSealSummary.getCurrentSealCount();
        sealProcessInfoCaption="设备已就绪，请将需要盖章的文件放置于指定位置后，点击确认盖章按钮后执行盖章。\n";
        sealProcessInfoHead="当前执行的盖章类型为：\n"+sealTypeChinese+"，第 "+currentCount+" 次";
        textSealProcess.setText(sealProcessInfoCaption);
        textSealHead.setText(sealProcessInfoHead);
    }

    private void refreshOutSealProcess(){
        String sealType= OutSealSummary.getCurrentSealType();
        if(TextUtils.isEmpty(sealType)){
            sealProcessInfoHead="取印完成!";
            sealProcessInfoCaption="";
            btnConfirmSeal.setVisibility(View.GONE);
            textSealHead.setText(sealProcessInfoHead);
            textSealProcess.setText(sealProcessInfoCaption);
            return;
        }
        String sealTypeChinese=OutSealSummary.getSealTypeChinese();
        sealProcessInfoCaption="设备已就绪，点击确认取印按钮后取出印章，使用完毕请及时归还\n";
        sealProcessInfoHead="当前即将取出的印章类型为：\n"+sealTypeChinese;

        textSealProcess.setText(sealProcessInfoCaption);
        textSealHead.setText(sealProcessInfoHead);
        btnConfirmSeal.setText("确认取印");
    }

    private void initViews(){
        textSealProcess=(TextView) findViewById(R.id.text_seal_process);
        textSealHead=(TextView) findViewById(R.id.text_seal_head);
        btnConfirmSeal=(AppCompatButton) findViewById(R.id.btn_confirm_seal);
        btnConfirmSeal.setOnClickListener(this);
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbalProgressBar=(ProgressBar) findViewById(R.id.toolbar_progress_bar);
        coordinatorLayout=(CoordinatorLayout) findViewById(R.id.coordinator_layout_bluetooth);
        loadingView=new SpotsDialog(this,"盖章机通信中……");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_confirm_seal:
                //loadingView.show();
                String command="";
                if(usingSealFlag){
                    command= BluetoothCmdInterpreter.usingSend(
                            UsingSealSummary.getCurrentSealType(), UsingSealSummary.isCurrentSealEnd());
                }else{
                    command=BluetoothCmdInterpreter.outSend(OutSealSummary.getCurrentSealType());
                }
                sendMessage(command);
                break;
        }
    }

    private class myHandler extends Handler{
        private final WeakReference<SealProcessActivity> mActivity;
        public myHandler(SealProcessActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        public void handleMessage(Message msg){
            final SealProcessActivity activity = mActivity.get();
            switch (msg.what){
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            activity.setStatus("Connected");
                            activity.reconnectButton.setVisible(false);
                            activity.toolbalProgressBar.setVisibility(View.GONE);
                            break;
                        case Constants.STATE_CONNECTING:
                            activity.setStatus("Connecting");
                            activity.toolbalProgressBar.setVisibility(View.VISIBLE);
                            break;
                        case Constants.STATE_NONE:
                            activity.setStatus("Not Connected");
                            activity.toolbalProgressBar.setVisibility(View.GONE);
                            break;
                        case Constants.STATE_ERROR:
                            activity.setStatus("Error");
                            activity.reconnectButton.setVisible(true);
                            activity.toolbalProgressBar.setVisibility(View.GONE);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage=BluetoothCmdInterpreter.bytesToHexString(writeBuf,writeBuf.length);
                    Toast.makeText(activity,"usingSend:"+writeMessage,Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:

                    String readMessage = (String) msg.obj;

                    if (readMessage != null ) {
                        Toast.makeText(activity,"Reveive:"+readMessage,Toast.LENGTH_SHORT).show();
                        if(BluetoothCmdInterpreter.UsingFeedbackSealOver.equals(readMessage)){
                            startTakePhoto();
                            UsingSealSummary.completeOnce();
                            refreshSealProcess();
                            //loadingView.dismiss();
                        }
                        if(BluetoothCmdInterpreter.OutFeedbackSealOver.equals(readMessage)){
                            OutSealSummary.completeOnce();
                            refreshSealProcess();
                            //loadingView.dismiss();
                        }
                    }
                    break;

                case Constants.MESSAGE_SNACKBAR:
                    Snackbar.make(activity.coordinatorLayout, msg.getData().getString(Constants.SNACKBAR), Snackbar.LENGTH_LONG)
                            .setAction("连接", new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    activity.reconnect();
                                }
                            }).show();

                    break;
                case Constants.MESSAGE_FILE_UPLOAD_SUCCEED:
                    Toast.makeText(activity,"照片上传成功",Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_FILE_UPLOAD_FAIL:
                    Toast.makeText(activity,"照片上传失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // 初始化surface
    @SuppressWarnings("deprecation")
    private void initSurface() {
        // 初始化surfaceview
        if (mySurfaceView == null && myHolder == null) {
            mySurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
            // 初始化surfaceholder
            myHolder = mySurfaceView.getHolder();
        }

    }
    /**
     * 开始拍照
     */
    private void startTakePhoto() {
        //初始化surface
        initSurface();
        //这里得开线程进行拍照，因为Activity还未显示完全的时候是无法进行拍照的，SurfacaView必须先显示
        new Thread() {
            @Override
            public void run() {
                super.run();
                //如果存在摄像头
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    //获取摄像头
                    if (openFacingBackCamera()) {
                        Log.i(TAG, "openCameraSuccess");
                        //进行对焦
                        autoFocus();
                    } else {
                        Log.i(TAG, "openCameraFailed");
                    }

                }

            }
        }.start();
    }

    // 自动对焦回调函数(空实现)
    private Camera.AutoFocusCallback myAutoFocus = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    // 对焦并拍照
    private void autoFocus() {

        try {
            // 因为开启摄像头需要时间，这里让线程睡两秒
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 自动对焦
        myCamera.autoFocus(myAutoFocus);

        // 对焦后拍照
        myCamera.takePicture(null, null, myPicCallback);
    }

    // 拍照成功回调函数
    private Camera.PictureCallback myPicCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            String filename = BitmapUtil.getFilePath(SealProcessActivity.this);

            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.close();
                WebServiceUtil.uploadByMethod(WebServiceMethod, filename, Constants.Document, new SoapCallbackListener() {
                    @Override
                    public void onFinish(String xml, String method, String sealCode, String filePath) {
                        UploadFileResponse response= XmlParseUtil.pullUploadFileResponse(xml);
                        if(response.getStatus()==1){
                            DbUtil.uploadSuccess(method,sealCode,filePath,Constants.Document);
                            handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_SUCCEED);
                        }else{
                            DbUtil.uploadFail(method,sealCode,filePath,Constants.Document);
                            handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_FAIL);
                        }
                    }

                    @Override
                    public void onError(Exception e, String method, String sealCode, String filePath) {
                        DbUtil.uploadFail(method,sealCode,filePath,Constants.Document);
                        handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_FAIL);
                    }
                });
            } catch (Exception error) {
                Toast.makeText(SealProcessActivity.this, "拍照失败", Toast.LENGTH_SHORT)
                        .show();

                Log.i(TAG, "保存照片失败" + error.toString());
                error.printStackTrace();
                myCamera.stopPreview();
                myCamera.release();
                myCamera = null;
            }

            Log.i(TAG, "获取照片成功");
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
    };

    // 得到后置摄像头
    private boolean openFacingBackCamera() {
        // 尝试开启后置摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    Log.i(TAG, "tryToOpenCamera");
                    myCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        try {
            // 这里的myCamera为已经初始化的Camera对象
            myCamera.setPreviewDisplay(myHolder);
        } catch (IOException e) {
            e.printStackTrace();
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }

        myCamera.startPreview();

        Camera.Parameters parameters = myCamera.getParameters(); // 获取各项参数
        parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
        parameters.setJpegQuality(100); // 设置照片质量
        List<Camera.Size> sizeList=parameters.getSupportedPictureSizes();
        Camera.Size camreaSize=sizeList.get(sizeList.size()/3);
        parameters.setPictureSize(camreaSize.width, camreaSize.height);
        parameters.setRotation(90);
        parameters.setFocusMode("continuous-picture");
        myCamera.setParameters(parameters);

        return true;
    }

    @Override
    public void onBackPressed() {
        retunToHome();
    }

    private void retunToHome(){
        Intent intent=new Intent(SealProcessActivity.this,MainActivity.class);
        if(WebServiceUtil.uploadByUrgentOut.equals(WebServiceMethod)){
            intent.setAction(Constants.ACTION_GET_SEAL_OFFLINE);
        }else if(WebServiceUtil.uploadByUrgentUsing.equals(WebServiceMethod)){
            intent.setAction(Constants.ACTION_USING_SEAL_OFFLINE);
        }else if(WebServiceUtil.uploadByOut.equals(WebServiceMethod)){
            intent.setAction(Constants.ACTION_GET_SEAL);
        }else {
            intent.setAction(Constants.ACTION_USING_SEAL);
        }
        startActivity(intent);
        finish();
    }
}
