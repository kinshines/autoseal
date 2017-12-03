package com.inktech.autoseal.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.TextViewAction;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.view.MaterialListView;
import com.google.android.cameraview.CameraView;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.adapter.BluetoothCmdInterpreter;
import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.model.OutSealSummary;
import com.inktech.autoseal.model.UploadFileResponse;
import com.inktech.autoseal.service.BluetoothService;
import com.inktech.autoseal.util.BitmapUtil;
import com.inktech.autoseal.util.DbUtil;
import com.inktech.autoseal.util.PreferenceUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.model.UsingSealSummary;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inktech.autoseal.R;
import com.inktech.autoseal.util.XmlParseUtil;
import com.squareup.picasso.RequestCreator;

import dmax.dialog.SpotsDialog;

public class SealProcessActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    BluetoothService bluetoothService;
    BluetoothDevice device;

    Toolbar toolbar;
    ProgressBar toolbalProgressBar;
    CoordinatorLayout coordinatorLayout;
    MenuItem reconnectButton;
    Snackbar snackTurnOn;
    myHandler handler;

    AlertDialog loadingView;
    MaterialListView listSealProcess;
    private CameraView mCameraView;

    private static final String TAG = "SealProcessActivity";

    private String WebServiceMethod="";
    private String sealTypeChinese="";
    private String takingPictureSealType="";
    private boolean usingSealFlag=false;
    private boolean returnSealFlag=false;
    private Handler mBackgroundHandler;
    Date lastClickTime=null;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Intent intent=getIntent();
        device = intent.getExtras().getParcelable(Constants.EXTRA_DEVICE);
        WebServiceMethod=intent.getStringExtra(Constants.web_service_method);
        if(WebServiceUtil.uploadByUsing.equals(WebServiceMethod)||WebServiceUtil.uploadByUrgentUsing.equals(WebServiceMethod)){
            usingSealFlag=true;
        }
        if(Constants.ReturnSeal.equals(WebServiceMethod)){
            returnSealFlag=true;
        }
        bluetoothService = new BluetoothService(handler, device);

        refreshSealProcess();
        lastClickTime=new Date();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        bluetoothService.connect();
        Log.d(Constants.TAG, "Connecting");

        //用印时先开灯
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(usingSealFlag&&Constants.STATE_CONNECTED==bluetoothService.getState()){
            sendBluetoothMessage(BluetoothCmdInterpreter.LightSwitchOn);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothService != null) {
            bluetoothService.stop();
            Log.d(Constants.TAG, "Stopping");
        }

        unregisterReceiver(mReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
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

    private boolean sendBluetoothMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != Constants.STATE_CONNECTED) {
            loadingView.dismiss();
            Snackbar.make(coordinatorLayout, "尚未连接盖章机", Snackbar.LENGTH_LONG)
                    .setAction("连接", new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            reconnect();
                        }
                    }).show();
            return false;
        } else {
            byte[] send = BluetoothCmdInterpreter.getHexBytes(message);
            return bluetoothService.write(send);
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
        listSealProcess.getAdapter().clearAll();
        if(usingSealFlag){
            refreshUsingSealProcess();
        }else{
            refreshOutSealProcess();
        }
    }

    private void refreshUsingSealProcess(){
        HashMap<String,Integer> overallMap=UsingSealSummary.getOverallMap();
        if(UsingSealSummary.isAllCompleted()||overallMap.isEmpty()){
            Card card = new Card.Builder(this)
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_welcome_card_layout)
                    .setTitle("盖章完成")
                    .setBackgroundColor(ContextCompat.getColor(this,R.color.colorWarningLight))
                    .addAction(R.id.ok_button, new WelcomeButtonAction(this)
                            .setText("返回首页")
                            .setTextColor(ContextCompat.getColor(this,R.color.colorPrimary))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    retunToHome();
                                }
                            }))
                    .endConfig()
                    .build();
            listSealProcess.getAdapter().add(card);

            //用印结束后关灯
            sendBluetoothMessage(BluetoothCmdInterpreter.LightSwitchOff);
        }else{
            for(String sealType:overallMap.keySet()){
                if(UsingSealSummary.isCanceled(sealType))
                    continue;
                //once you start to seal,you should process this seal to end
                if(!UsingSealSummary.isCurrentCompleted()&&!sealType.equals(UsingSealSummary.getCurrentSealType()))
                    continue;
                Integer remainingCount=UsingSealSummary.getRemainCount(sealType);
                if(remainingCount==0)
                    continue;
                Card card=populateCardWithUsingSeal(sealType,remainingCount);
                listSealProcess.getAdapter().add(card);
            }
        }
    }

    private void refreshOutSealProcess(){
        ArrayList<String> overallMap=OutSealSummary.getOverallMap();
        if(OutSealSummary.isAllCompleted()||overallMap.isEmpty()){
            Card card = new Card.Builder(this)
                    .withProvider(new CardProvider())
                    .setLayout(R.layout.material_welcome_card_layout)
                    .setTitle(returnSealFlag?"归还印章完成":"取出印章完成")
                    .setBackgroundColor(ContextCompat.getColor(this,R.color.colorWarningLight))
                    .addAction(R.id.ok_button, new WelcomeButtonAction(this)
                            .setText("返回首页")
                            .setTextColor(ContextCompat.getColor(this,R.color.colorPrimary))
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    retunToHome();
                                }
                            }))
                    .endConfig()
                    .build();
            listSealProcess.getAdapter().add(card);
        }else{
            for (String sealType:overallMap){
                if(OutSealSummary.isCanceled(sealType))
                    continue;
                Card card=populateCardWithOutSeal(sealType);
                listSealProcess.getAdapter().add(card);
            }
        }
    }

    private void initViews(){
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbalProgressBar=(ProgressBar) findViewById(R.id.toolbar_progress_bar);
        coordinatorLayout=(CoordinatorLayout) findViewById(R.id.coordinator_layout_bluetooth);
        loadingView=new SpotsDialog(this,"盖章机通信中……");
        listSealProcess=(MaterialListView) findViewById(R.id.list_seal_process);
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
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
                            activity.setStatus("盖章机已连接");
                            activity.reconnectButton.setVisible(false);
                            activity.toolbalProgressBar.setVisibility(View.GONE);
                            break;
                        case Constants.STATE_CONNECTING:
                            activity.setStatus("盖章机连接中……");
                            activity.toolbalProgressBar.setVisibility(View.VISIBLE);
                            break;
                        case Constants.STATE_NONE:
                            activity.setStatus("未连接盖章机");
                            activity.toolbalProgressBar.setVisibility(View.GONE);
                            break;
                        case Constants.STATE_ERROR:
                            activity.setStatus("出错啦");
                            activity.reconnectButton.setVisible(true);
                            activity.toolbalProgressBar.setVisibility(View.GONE);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage=BluetoothCmdInterpreter.bytesToHexString(writeBuf,writeBuf.length);
                    //Toast.makeText(activity,"Send:"+writeMessage,Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    if (readMessage != null ) {
                        //Toast.makeText(activity,"Reveive:"+readMessage,Toast.LENGTH_SHORT).show();
                        if(BluetoothCmdInterpreter.UsingFeedbackSealOver.equals(readMessage)){
                            takingPictureSealType=UsingSealSummary.getCurrentSealType();
                            startTakePhoto();
                            UsingSealSummary.completeOnce();
                            refreshSealProcess();
                            loadingView.dismiss();
                        }

                        if(BluetoothCmdInterpreter.ReturnFeedbackSealOver.equals(readMessage)){
                            loadingView.dismiss();
                            //还印后询问是否还印完成
                            android.support.v7.app.AlertDialog.Builder dialog=new android.support.v7.app.AlertDialog.Builder(SealProcessActivity.this);
                            dialog.setTitle("还印完成");
                            dialog.setCancelable(false);
                            dialog.setMessage("是否已将印章归还？");
                            dialog.setPositiveButton("已经归还", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    loadingView=new SpotsDialog(SealProcessActivity.this,"印章归还确认中……");
                                    loadingView.show();
                                    String sealType=OutSealSummary.getCurrentSealType();
                                    String command=BluetoothCmdInterpreter.returnSend(sealType,true);
                                    boolean sendFlag=sendBluetoothMessage(command);
                                    if(sendFlag){
                                        PreferenceUtil.removeOutSealRecord(
                                                OutSealSummary.getCurrentSealCode(),
                                                OutSealSummary.getCurrentSealType()
                                        );
                                    }
                                }
                            });
                            dialog.show();
                        }

                        if(BluetoothCmdInterpreter.OutFeedbackSealOver.equals(readMessage)){
                            loadingView.dismiss();
                            //取印完成后询问是否立即还印
                            android.support.v7.app.AlertDialog.Builder dialog=new android.support.v7.app.AlertDialog.Builder(SealProcessActivity.this);
                            dialog.setTitle("立即还印");
                            dialog.setCancelable(false);
                            dialog.setMessage("印章已取出，是否立即归还？");
                            dialog.setPositiveButton("立即归还", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    outSealConfirmDialog();
                                }
                            });
                            dialog.setNegativeButton("以后归还", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    PreferenceUtil.addOutSealRecord(
                                            OutSealSummary.getCurrentSealCode(),
                                            OutSealSummary.getCurrentSealType(),
                                            OutSealSummary.getCurrentSealName());
                                    outSealConfirmDialog();
                                }
                            });
                            dialog.show();
                        }

                        //还印确认反馈
                        if(BluetoothCmdInterpreter.ReturnConfirmedFeedback.equals(readMessage)){
                            loadingView.dismiss();
                            PreferenceUtil.removeOutSealRecord(
                                    OutSealSummary.getCurrentSealCode(),
                                    OutSealSummary.getCurrentSealType()
                            );
                            OutSealSummary.completeOnce();
                            refreshSealProcess();
                        }

                        //取印确认反馈
                        if(BluetoothCmdInterpreter.OutConfirmedFeedback.equals(readMessage)){
                            loadingView.dismiss();
                            OutSealSummary.completeOnce();
                            refreshSealProcess();
                        }
                    }
                    break;

                case Constants.MESSAGE_SNACKBAR:
                    loadingView.dismiss();
                    Snackbar.make(activity.coordinatorLayout, msg.getData().getString(Constants.SNACKBAR), Snackbar.LENGTH_LONG)
                            .setAction("连接", new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    activity.reconnect();
                                }
                            }).show();
                    break;
                case Constants.MESSAGE_FILE_UPLOAD_SUCCEED:
                    //Toast.makeText(activity,"照片上传成功",Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_FILE_UPLOAD_FAIL:
                    //Toast.makeText(activity,"照片上传失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void outSealConfirmDialog(){
        //确认取印完成
        android.support.v7.app.AlertDialog.Builder dialog=new android.support.v7.app.AlertDialog.Builder(SealProcessActivity.this);
        dialog.setTitle("取印是否已完成？");
        dialog.setCancelable(false);
        dialog.setPositiveButton("取印完成", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String sealType=OutSealSummary.getCurrentSealType();
                String command=BluetoothCmdInterpreter.outSend(sealType,true);
                sendBluetoothMessage(command);
                loadingView=new SpotsDialog(SealProcessActivity.this,"取印完成确认中……");
                loadingView.show();
            }
        });
        dialog.show();
    }

    /**
     * 开始拍照
     */
    private void startTakePhoto() {
        if (mCameraView != null) {
            mCameraView.takePicture();
        }
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

    private Card populateCardWithUsingSeal(String sealType,Integer remainingCount){
        String chineseSealType=UsingSealSummary.getSealTypeChinese(sealType);
        String description="剩余 "+remainingCount+" 次";


        Card card = new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_basic_image_buttons_card_layout)
                .setTitle(chineseSealType)
                .setTitleGravity(Gravity.END)
                .setDescription(description)
                .setDescriptionGravity(Gravity.END)
                .setDrawable(getDrawableIcon(sealType))
                .setDrawableConfiguration(new CardProvider.OnImageConfigListener() {
                    @Override
                    public void onImageConfigure(@NonNull RequestCreator requestCreator) {
                        requestCreator.fit();
                    }
                })
                .addAction(R.id.left_text_button, new TextViewAction(this)
                        .setText("确认盖章")
                        .setTextResourceColor(R.color.colorPrimary)
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                Date now=new Date();
                                if(now.getTime()-lastClickTime.getTime()<2000){
                                    return;
                                }
                                lastClickTime=now;
                                loadingView=new SpotsDialog(SealProcessActivity.this,"盖章中……请勿取走文件");
                                loadingView.show();
                                startCamera();
                                UsingSealSummary.setCurrentSealType(sealType);
                                String command=BluetoothCmdInterpreter.usingSend(
                                        sealType,
                                        remainingCount,
                                        UsingSealSummary.getTotalCount(sealType));
                                sendBluetoothMessage(command);
                            }
                        }))
                .addAction(R.id.right_text_button, new TextViewAction(this)
                        .setText("取消盖章")
                        .setTextResourceColor(R.color.colorAccent)
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                android.support.v7.app.AlertDialog.Builder dialog=new android.support.v7.app.AlertDialog.Builder(SealProcessActivity.this);
                                dialog.setTitle("确认取消盖章");
                                dialog.setMessage("本次盖章将不再提供此类印章");
                                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //一上来就取消的话，不发送指令
                                        if(remainingCount!=UsingSealSummary.getTotalCount(sealType)){
                                            String command=BluetoothCmdInterpreter.cancelUsingSend(sealType);
                                            sendBluetoothMessage(command);
                                        }
                                        UsingSealSummary.cancelSeal(sealType);
                                        UsingSealSummary.setCurrentSealType("");
                                        card.dismiss();
                                        refreshSealProcess();
                                    }
                                });
                                dialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                dialog.show();
                            }
                        }))
                .endConfig()
                .build();
        return card;
    }

    private Card populateCardWithZeroCount(String sealType){
        String chineseSealType=UsingSealSummary.getSealTypeChinese(sealType);
        Card card = new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_small_image_card)
                .setTitle(chineseSealType)
                .setTitleGravity(Gravity.END)
                .setDescription("盖章完成")
                .setDescriptionGravity(Gravity.END)
                .setDrawable(getDrawableIcon(sealType))
                .setDrawableConfiguration(new CardProvider.OnImageConfigListener() {
                    @Override
                    public void onImageConfigure(@NonNull RequestCreator requestCreator) {
                        requestCreator.centerCrop();
                    }
                })
                .endConfig()
                .build();
        return card;
    }

    private Card populateCardWithOutSeal(String sealType){
        String chineseSealType=OutSealSummary.getSealTypeChinese(sealType);
        boolean hasTakenOut=OutSealSummary.sealHasTakenOut(sealType);
        String description="";
        if(hasTakenOut){
            description=returnSealFlag?"已归还印章":"已取出印章";
        }else{
            description=returnSealFlag?"等待归还印章":"等待取出印章";
        }
        CardProvider cardProvider = new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_basic_image_buttons_card_layout)
                .setTitle(chineseSealType)
                .setTitleGravity(Gravity.END)
                .setDescription(description)
                .setDescriptionGravity(Gravity.END)
                .setDrawable(getDrawableIcon(sealType))
                .setDrawableConfiguration(new CardProvider.OnImageConfigListener() {
                    @Override
                    public void onImageConfigure(@NonNull RequestCreator requestCreator) {
                        requestCreator.fit();
                    }
                });

        if(!hasTakenOut){
            cardProvider
                    .addAction(R.id.left_text_button, new TextViewAction(this)
                            .setText(returnSealFlag?"确认归还印章":"确认取出印章")
                            .setTextResourceColor(R.color.colorPrimary)
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    loadingView=new SpotsDialog(SealProcessActivity.this,returnSealFlag?"归还印章中……":"取出印章中……");
                                    loadingView.show();
                                    OutSealSummary.setCurrentSealType(sealType);
                                    String command=returnSealFlag
                                            ?BluetoothCmdInterpreter.returnSend(sealType,false)
                                            :BluetoothCmdInterpreter.outSend(sealType,false);
                                    sendBluetoothMessage(command);
                                }
                            }))
                    .addAction(R.id.right_text_button, new TextViewAction(this)
                            .setText("取消")
                            .setTextResourceColor(R.color.colorAccent)
                            .setListener(new OnActionClickListener() {
                                @Override
                                public void onActionClicked(View view, Card card) {
                                    android.support.v7.app.AlertDialog.Builder dialog=new android.support.v7.app.AlertDialog.Builder(SealProcessActivity.this);
                                    dialog.setTitle("确认取消");
                                    dialog.setMessage(returnSealFlag?"本次还印将不再接受此类印章":"本次取印将不再提供此类印章");
                                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            OutSealSummary.cancelSeal(sealType);
                                            card.dismiss();
                                            refreshSealProcess();
                                        }
                                    });
                                    dialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    dialog.show();
                                }
                            }));
        }
        Card card= cardProvider
                .endConfig()
                .build();
        return card;
    }

    private static int getDrawableIcon(String sealType){
        switch (sealType){
            case Constants.gz:
                return R.drawable.gz;
            case Constants.frz:
                return R.drawable.frz;
            case Constants.fpz:
                return R.drawable.fpz;
            case Constants.htz:
                return R.drawable.htz;
            case Constants.cwz:
                return R.drawable.cwz;
        }
        return R.drawable.gz;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startCamera(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            TakePhotoActivity.ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            Constants.REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), Constants.FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION);
        }
    }

    private void stopCamera(){
        mCameraView.stop();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    String filename = BitmapUtil.getFilePath(SealProcessActivity.this);
                    FileOutputStream fos=null;
                    try {
                        fos = new FileOutputStream(filename);
                        fos.write(data);
                        fos.close();
                        sealTypeChinese=UsingSealSummary.getSealTypeChinese(takingPictureSealType);
                        WebServiceUtil.uploadByMethod(WebServiceMethod, filename, Constants.Document,sealTypeChinese,new SoapCallbackListener() {
                            @Override
                            public void onFinish(String xml, String method, String sealCode, String filePath) {
                                UploadFileResponse response= XmlParseUtil.pullUploadFileResponse(xml);
                                if(response.getStatus()==1){
                                    DbUtil.uploadSuccess(method,sealCode,filePath,Constants.Document,sealTypeChinese);
                                    handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_SUCCEED);
                                }else{
                                    DbUtil.uploadFail(method,sealCode,filePath,Constants.Document,sealTypeChinese);
                                    handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_FAIL);
                                }
                            }

                            @Override
                            public void onError(Exception e, String method, String sealCode, String filePath) {
                                DbUtil.uploadFail(method,sealCode,filePath,Constants.Document,sealTypeChinese);
                                handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_FAIL);
                            }
                        });
                    } catch (Exception error) {
                        Toast.makeText(SealProcessActivity.this, "拍照失败", Toast.LENGTH_SHORT)
                                .show();
                        Log.i(TAG, "保存照片失败" + error.toString());
                        error.printStackTrace();
                    }finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                // Ignore
                            }
                        }
                        stopCamera();
                    }
                }
            });
        }
    };
}
