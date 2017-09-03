package com.inktech.autoseal;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.inktech.autoseal.Util.SoapCallbackListener;
import com.inktech.autoseal.Util.WebServiceUtil;
import com.inktech.autoseal.model.SealSummary;

import org.ksoap2.serialization.SoapObject;

public class SealProcessActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textSealProcess;
    AppCompatButton btnConfirmSeal;
    String sealProcessInfo="";
    public static final int TAKE_PHOTO_BACK=0;

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
                startActivityForResult(new Intent(this,PhotoBackActivity.class),TAKE_PHOTO_BACK);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case TAKE_PHOTO_BACK:
                if(resultCode==RESULT_OK){
                    String filePath=data.getStringExtra("filePath");
                    WebServiceUtil.uploadByUsing(filePath, "文件", new SoapCallbackListener() {
                        @Override
                        public void onFinish(SoapObject soapObject) {
                            handler.sendEmptyMessage(0x001);
                        }

                        @Override
                        public void onError(Exception e) {
                            handler.sendEmptyMessage(0x002);
                        }
                    });
                }
                //// TODO: 2017/8/28  接收蓝牙指令
                try {
                    Thread.sleep(10000);
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
}
