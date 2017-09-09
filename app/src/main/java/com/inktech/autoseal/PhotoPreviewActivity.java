package com.inktech.autoseal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.inktech.autoseal.Util.BitmapUtil;
import com.inktech.autoseal.Util.SoapCallbackListener;
import com.inktech.autoseal.Util.WebServiceUtil;
import com.inktech.autoseal.model.TakePhotoImage;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoPreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PhotoPreviewActivity";
    ImageView imagePreview;
    AppCompatButton btnConfirm;
    AppCompatButton btnTakePhoto;
    byte[] photoData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        initViews();
        Intent intent=getIntent();
        photoData=intent.getByteArrayExtra("photo_data");
        Bitmap previewBitmap= BitmapFactory.decodeByteArray(photoData,0,photoData.length);
        imagePreview.setImageBitmap(previewBitmap);
    }

    private void initViews(){
        imagePreview=(ImageView) findViewById(R.id.image_preview);
        btnConfirm=(AppCompatButton) findViewById(R.id.btn_confirm);
        btnTakePhoto=(AppCompatButton) findViewById(R.id.btn_take_photo);
        btnConfirm.setOnClickListener(this);
        btnTakePhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                String filename = BitmapUtil.getFilePath(PhotoPreviewActivity.this);
                try {
                    FileOutputStream fos = new FileOutputStream(filename);
                    fos.write(photoData);
                    fos.close();
                    WebServiceUtil.uploadByUsing(filename, "用印人", new SoapCallbackListener() {
                    @Override
                    public void onFinish(SoapObject soapObject) {
                        handler.sendEmptyMessage(0x001);
                    }

                    @Override
                    public void onError(Exception e) {
                        handler.sendEmptyMessage(0x002);
                    }
                });
                }catch (Exception error) {
                    Toast.makeText(PhotoPreviewActivity.this, "保存失败", Toast.LENGTH_SHORT)
                            .show();
                    Log.i(TAG, "保存照片失败" + error.toString());
                    error.printStackTrace();
                }
                startActivity(new Intent(this,SealProcessActivity.class));
                finish();
                break;
            case R.id.btn_take_photo:
                startActivity(new Intent(this,TakePhotoActivity.class));
                finish();
                break;
        }
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x001:
                    Toast.makeText(PhotoPreviewActivity.this,"照片上传成功",Toast.LENGTH_SHORT).show();
                    break;
                case 0x002:
                    Toast.makeText(PhotoPreviewActivity.this,"照片上传失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
