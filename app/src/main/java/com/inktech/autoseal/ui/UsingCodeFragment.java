package com.inktech.autoseal.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.view.MaterialListView;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.model.SealInfo;
import com.inktech.autoseal.model.SealInfoResult;
import com.inktech.autoseal.model.SoapCallbackListener;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.model.SealSummary;
import com.inktech.autoseal.util.XmlParseUtil;

import org.ksoap2.serialization.SoapObject;

import com.inktech.autoseal.R;

public class UsingCodeFragment extends Fragment {

    AppCompatEditText editUsingCode;
    AppCompatButton btnUsingCode;
    AppCompatButton btnScan;
    ProgressBar progressBar;
    MaterialListView listSealInfo;

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

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),QRCodeReaderActivity.class);
                startActivityForResult(intent, Constants.REQUEST_QR_SCAN);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.REQUEST_QR_SCAN:
                if(resultCode== Activity.RESULT_OK){
                    String qrText=data.getStringExtra("qr_text");
                    editUsingCode.setText(qrText);
                    btnUsingCode.callOnClick();
                }
                break;
        }
    }

    private void initViews(View view) {
        editUsingCode = view.findViewById(R.id.edit_using_code);
        btnUsingCode= view.findViewById(R.id.btn_using_code);
        progressBar=view.findViewById(R.id.progress_bar);
        listSealInfo=view.findViewById(R.id.list_seal_info);
        btnScan=view.findViewById(R.id.btn_scan);
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x001:
                    progressBar.setVisibility(View.GONE);
                    String xml=((SoapObject)msg.obj).getPropertySafelyAsString("getUsingSealInfoResult");
                    SealInfoResult sealInfoResult=XmlParseUtil.parseXML2SealInfoResult(xml);
                    int sealStatus=sealInfoResult.getSealCount();
                    if(sealStatus==0){
                        Toast.makeText(getContext(),"用印编码不存在",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(sealStatus==-1){
                        Toast.makeText(getContext(),"机器码不对应",Toast.LENGTH_LONG).show();
                        return;
                    }

                    String result="";
                    SealSummary.Init();
                    for(SealInfo seal : sealInfoResult.getSealList()){
                        result=result+translateSealItem(seal)+"\n";
                    }
                    result=result.substring(0,result.length()-1);
                    Card sealCard = new Card.Builder(getContext())
                            .withProvider(new CardProvider())
                            .setLayout(R.layout.material_welcome_card_layout)
                            .setTitle(result)
                            .setDescription("用印编码有效，确认盖章信息无误后，请点击确认拍照按钮，并拍下您的正面照以存档方可盖章")
                            .setBackgroundColor(getResources().getColor(R.color.colorLight))
                            .addAction(R.id.ok_button, new WelcomeButtonAction(getContext())
                                    .setText("确认拍照")
                                    .setTextColor(getResources().getColor(R.color.colorAccent))
                                    .setListener(new OnActionClickListener() {
                                        @Override
                                        public void onActionClicked(View view, Card card) {
                                            Intent intent=new Intent(getActivity(),TakePhotoActivity.class);
                                            startActivity(intent);
                                            getActivity().finish();
                                        }
                                    }))
                            .setDividerVisible(true)
                            .endConfig()
                            .build();
                    listSealInfo.getAdapter().add(sealCard);
                    break;
                case 0x002:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"网络暂时无法连接，请使用紧急用印功能",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private String translateSealItem(SealInfo seal){
        String type=seal.getType();
        int count=seal.getCount();
        String chineseType= SealSummary.translateSealTypeToChinese(type);
        SealSummary.addMap(type,count);
        return chineseType+"：盖章 "+count+" 次";
    }
}
