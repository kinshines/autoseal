package com.inktech.autoseal.ui;

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

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.model.UploadFileResponse;
import com.inktech.autoseal.util.BitmapUtil;
import com.inktech.autoseal.util.DbUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import java.io.FileOutputStream;

import  com.inktech.autoseal.R;
import com.inktech.autoseal.util.XmlParseUtil;

public class PhotoPreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PhotoPreviewActivity";
    private String WebServiceMethod="";
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
        WebServiceMethod=intent.getStringExtra(Constants.web_service_method);
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
                    final Integer position=(WebServiceUtil.uploadByOut.equals(WebServiceMethod)||WebServiceUtil.uploadByUrgentOut.equals(WebServiceMethod))?Constants.UserForOut:Constants.User;
                    WebServiceUtil.uploadByMethod(WebServiceMethod, filename, position, new SoapCallbackListener() {
                    @Override
                    public void onFinish(String xml, String method, String sealCode, String filePath) {
                        UploadFileResponse response= XmlParseUtil.pullUploadFileResponse(xml);

                        if(response.getStatus()==1){
                            DbUtil.uploadSuccess(method,sealCode,filePath,position);
                            handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_SUCCEED);
                        }else{
                            DbUtil.uploadFail(method,sealCode,filePath,position);
                            handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_FAIL);
                        }
                    }

                    @Override
                    public void onError(Exception e, String method, String sealCode, String filePath) {
                        DbUtil.uploadFail(method,sealCode,filePath,position);
                        handler.sendEmptyMessage(Constants.MESSAGE_FILE_UPLOAD_FAIL);
                    }
                });
                }catch (Exception error) {
                    Toast.makeText(PhotoPreviewActivity.this, "保存失败", Toast.LENGTH_SHORT)
                            .show();
                    Log.i(TAG, "保存照片失败" + error.toString());
                    error.printStackTrace();
                }
                Intent bluetoothIntent=new Intent(this,BluetoothSearchActivity.class);
                bluetoothIntent.putExtra(Constants.web_service_method,WebServiceMethod);
                startActivity(bluetoothIntent);
                finish();
                break;
            case R.id.btn_take_photo:
                Intent takephotoIntent=new Intent(this,TakePhotoActivity.class);
                takephotoIntent.putExtra(Constants.web_service_method,WebServiceMethod);
                startActivity(takephotoIntent);
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
