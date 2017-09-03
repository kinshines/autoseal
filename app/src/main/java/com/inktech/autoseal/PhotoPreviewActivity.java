package com.inktech.autoseal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
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
import java.io.IOException;

public class PhotoPreviewActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int TAKE_PHOTO=4;
    public static final int PREVIEW_PHOTO=5;

    ImageView imagePreview;
    AppCompatButton btnConfirm;
    AppCompatButton btnTakePhoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        initViews();
        Intent intent=getIntent();
        if(intent.getIntExtra("next_action",0)==TAKE_PHOTO){
            takePhoto();
        }

    }

    private void initViews(){
        imagePreview=(ImageView) findViewById(R.id.image_preview);
        btnConfirm=(AppCompatButton) findViewById(R.id.btn_confirm);
        btnTakePhoto=(AppCompatButton) findViewById(R.id.btn_take_photo);
        btnConfirm.setOnClickListener(this);
        btnTakePhoto.setOnClickListener(this);
    }


    private void takePhoto(){
        File outputImage=new File(BitmapUtil.getFilePath(this));
        try{
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        Uri imageUri=Uri.fromFile(outputImage);
        TakePhotoImage.ImageUri=imageUri;
        TakePhotoImage.FilePath=imageUri.getPath();
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, PhotoPreviewActivity.TAKE_PHOTO);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_confirm:
                WebServiceUtil.uploadByUsing(TakePhotoImage.FilePath, "用印人", new SoapCallbackListener() {
                    @Override
                    public void onFinish(SoapObject soapObject) {
                        handler.sendEmptyMessage(0x001);
                    }

                    @Override
                    public void onError(Exception e) {
                        handler.sendEmptyMessage(0x002);
                    }
                });
                startActivity(new Intent(this,SealProcessActivity.class));
                break;
            case R.id.btn_take_photo:
                takePhoto();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK){
                    imagePreview.setImageBitmap(decodeSampledBitmap(TakePhotoImage.FilePath));
                }
                break;
            default:break;
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

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap decodeSampledBitmap(String pathName,
                                       int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    //I added this to have a good approximation of the screen size:
    private Bitmap decodeSampledBitmap(String pathName) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return decodeSampledBitmap(pathName, width, height);
    }
}
