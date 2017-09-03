package com.inktech.autoseal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.inktech.autoseal.Util.SoapCallbackListener;
import com.inktech.autoseal.Util.WebServiceUtil;
import com.inktech.autoseal.model.SealSummary;
import com.inktech.autoseal.model.TakePhotoImage;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.IOException;

public class UsingCodeFragment extends Fragment {

    private AppCompatEditText editUsingCode;
    private AppCompatButton btnUsingCode;
    private TextView textSealInfo;
    private CardView cardSealInfo;
    private AppCompatButton btnTakePhoto;
    private ProgressBar progressBar;

    public UsingCodeFragment() {
        // Required empty public constructor
    }

    public static UsingCodeFragment newInstance(String param1, String param2) {
        UsingCodeFragment fragment = new UsingCodeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_using_code, container, false);
        initViews(view);
        btnUsingCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putString("sealCode",editUsingCode.getText().toString());
                editor.apply();
                WebServiceUtil.getUsingSealInfo(new SoapCallbackListener() {
                    @Override
                    public void onFinish(SoapObject soapObject) {
                        Message message=new Message();
                        message.obj=soapObject;
                        message.what=0x001;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onError(Exception e) {
                        handler.sendEmptyMessage(0x002);
                    }
                });
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),PhotoPreviewActivity.class);
                intent.putExtra("next_action",PhotoPreviewActivity.TAKE_PHOTO);
                startActivity(intent);
            }
        });
        return view;
    }

    private void initViews(View view) {
        editUsingCode = view.findViewById(R.id.edit_using_code);
        btnUsingCode= view.findViewById(R.id.btn_using_code);
        textSealInfo=view.findViewById(R.id.text_seal_info);
        cardSealInfo=view.findViewById(R.id.card_seal_info);
        btnTakePhoto=view.findViewById(R.id.btn_take_photo);
        progressBar=view.findViewById(R.id.progress_bar);
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x001:
                    progressBar.setVisibility(View.GONE);
                    cardSealInfo.setVisibility(View.VISIBLE);
                    SoapObject sealInfoResult=(SoapObject)((SoapObject)msg.obj).getProperty("getUsingSealInfoResult");
                    SoapObject outerResult=(SoapObject)sealInfoResult.getProperty("result");
                    int sealStatus=Integer.parseInt(outerResult.getPropertySafelyAsString("sealCount"));
                    if(sealStatus==0){
                        textSealInfo.setText("用印编码不存在");
                        return;
                    }
                    if(sealStatus==-1){
                        textSealInfo.setText("机器码不对应");
                        return;
                    }
                    SoapObject sealList=(SoapObject)outerResult.getProperty("sealList");
                    int sealListCount=sealList.getPropertyCount();
                    String result="您输入的用印编码有效，盖章信息为：\n";
                    for(int i=0;i<sealListCount;i++){
                        SoapObject seal=(SoapObject)sealList.getProperty(i);
                        result=result+translateSealItem(seal)+"\n";
                    }
                    result+="确认盖章信息无误后，请点击下方拍照按钮，并拍下您的正面照以存档方可盖章";
                    textSealInfo.setText(result);
                    break;
                case 0x002:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"网络暂时无法连接，请使用紧急用印功能",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private String translateSealItem(SoapObject seal){
        String type=seal.getProperty("type").toString();
        String count=seal.getProperty("count").toString();
        String chineseType= SealSummary.translateSealTypeToChinese(type);
        SealSummary.addMap(type,Integer.parseInt(count));
        return chineseType+"：盖章 "+count+" 次";
    }
}
