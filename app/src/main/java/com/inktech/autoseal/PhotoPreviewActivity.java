package com.inktech.autoseal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.inktech.autoseal.model.Constants;
import com.inktech.autoseal.utility.BitmapUtil;
import com.inktech.autoseal.utility.SoapCallbackListener;
import com.inktech.autoseal.utility.WebServiceUtil;

import org.ksoap2.serialization.SoapObject;

import java.io.FileOutputStream;

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
                        handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_SUCCEED);
                    }

                    @Override
                    public void onError(Exception e) {
                        handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_FAIL);
                    }
                });
                }catch (Exception error) {
                    Toast.makeText(PhotoPreviewActivity.this, "保存失败", Toast.LENGTH_SHORT)
                            .show();
                    Log.i(TAG, "保存照片失败" + error.toString());
                    error.printStackTrace();
                }
                startActivity(new Intent(this,BluetoothSearchActivity.class));
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
                case Constants.MESSAGE_FILE_UPLOAD_SUCCEED:
                    Toast.makeText(PhotoPreviewActivity.this,"照片上传成功",Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_FILE_UPLOAD_FAIL:
                    Toast.makeText(PhotoPreviewActivity.this,"照片上传失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
