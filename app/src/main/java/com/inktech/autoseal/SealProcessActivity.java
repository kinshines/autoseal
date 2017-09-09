package com.inktech.autoseal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.inktech.autoseal.Util.BitmapUtil;
import com.inktech.autoseal.Util.SoapCallbackListener;
import com.inktech.autoseal.Util.WebServiceUtil;
import com.inktech.autoseal.model.SealSummary;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SealProcessActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textSealProcess;
    AppCompatButton btnConfirmSeal;
    String sealProcessInfo="";
    private SurfaceView mySurfaceView;
    private SurfaceHolder myHolder;
    private Camera myCamera;
    public static final int TAKE_PHOTO_BACK=0;

    private static final String TAG = "SealProcessActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seal_process);
        initViews();
        refreshSealProcess();
    }
    private void refreshSealProcess(){
        String sealType=SealSummary.getCurrentSealType();
        if(TextUtils.isEmpty(sealType)){
            sealProcessInfo="盖章完成!";
            btnConfirmSeal.setVisibility(View.GONE);
            textSealProcess.setText(sealProcessInfo);
            return;
        }
        String sealTypeChinese=SealSummary.getSealTypeChinese();
        int currentCount=SealSummary.getCurrentSealCount();
        sealProcessInfo="设备已就绪，请将需要盖章的文件放置于指定位置后，点击确认盖章按钮后执行盖章。\n";
        sealProcessInfo+="当前执行的盖章类型为："+sealTypeChinese+"，第 "+currentCount+" 次";
        textSealProcess.setText(sealProcessInfo);
    }

    private void initViews(){
        textSealProcess=(TextView) findViewById(R.id.text_seal_process);
        btnConfirmSeal=(AppCompatButton) findViewById(R.id.btn_confirm_seal);
        btnConfirmSeal.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_confirm_seal:
                //// TODO: 2017/8/28 发送蓝牙指令
                startTakePhoto();
                //// TODO: 2017/8/28  接收蓝牙指令
                try {
                    Thread.sleep(5000);
                    SealSummary.completeOnce();
                    refreshSealProcess();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x001:
                    Toast.makeText(SealProcessActivity.this,"照片上传成功",Toast.LENGTH_SHORT).show();
                    break;
                case 0x002:
                    Toast.makeText(SealProcessActivity.this,"照片上传失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

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
            Thread.sleep(3000);
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

            // 将得到的照片进行270°旋转，使其竖直
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.preRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);

            try {
                FileOutputStream fos = new FileOutputStream(filename);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                WebServiceUtil.uploadByUsing(filename, "文件", new SoapCallbackListener() {
                    @Override
                    public void onFinish(SoapObject soapObject) {
                        handler.sendEmptyMessage(0x001);
                    }

                    @Override
                    public void onError(Exception e) {
                        handler.sendEmptyMessage(0x002);
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
    private boolean openFacingFrontCamera() {
        // 尝试开启前置摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    Log.i(TAG, "tryToOpenCamera");
                    myCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        // 如果开启前置失败（无前置）则开启后置
        if (myCamera == null) {
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        myCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        return false;
                    }
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

        return true;
    }

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
        parameters.setFocusMode("continuous-picture");
        myCamera.setParameters(parameters);

        return true;
    }

}
