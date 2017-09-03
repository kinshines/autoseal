package com.inktech.autoseal.Util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.File;

/**
 * Created by Chaoyu on 2017/8/15.
 */

public class NewSurfaceHolder implements SurfaceHolder.Callback {

    private static final String TAG = "NewSurfaceHolder";
    int camera_facing;
    Activity activity;
    Camera mCamera;
    public NewSurfaceHolder(Activity activity,int camera_facing){
        this.activity=activity;
        this.camera_facing=camera_facing;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {

            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == camera_facing) {
                    mCamera = Camera.open(i);
                    mCamera.setPreviewDisplay(holder);

                    mCamera.startPreview();

                    /**
                     * 相机开启需要时间 延时takePicture
                     */
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    String filePath= BitmapUtil.getFilePath(activity);

                                    int degree = BitmapUtil.readPictureDegree(filePath,camera_facing);
                                    Bitmap bitmap = BitmapUtil.rotaingImageView(degree, source);

                                    BitmapUtil.saveBitmap(bitmap, new File(filePath));
                                    Intent intent=new Intent();
                                    intent.putExtra("filePath",filePath);
                                    activity.setResult(activity.RESULT_OK,intent);
                                    activity.finish();
                                }
                            });
                        }
                    }, 2000);


                }
            }

        } catch (Exception e) {
            Log.e(TAG, "surfaceCreated: ", e);
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters(); // 获取各项参数
        parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
        parameters.setJpegQuality(100); // 设置照片质量

        /**
         * 以下不设置在某些机型上报错
         */
        int mPreviewHeight = parameters.getPreviewSize().height;
        int mPreviewWidth = parameters.getPreviewSize().width;
        parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
        parameters.setPictureSize(mPreviewWidth, mPreviewHeight);

        mCamera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.unlock();
        mCamera.release();
    }
}
