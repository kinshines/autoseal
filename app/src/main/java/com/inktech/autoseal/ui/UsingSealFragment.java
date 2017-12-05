package com.inktech.autoseal.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.view.MaterialListView;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.model.UsingSealInfoItem;
import com.inktech.autoseal.model.UsingSealInfoResponse;
import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.util.PreferenceUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.model.UsingSealSummary;
import com.inktech.autoseal.util.XmlParseUtil;

import com.inktech.autoseal.R;

import dmax.dialog.SpotsDialog;
import android.app.AlertDialog;

public class UsingSealFragment extends Fragment {

    AppCompatEditText editUsingCode;
    AppCompatButton btnUsingCode;
    AppCompatButton btnScan;
    MaterialListView listSealInfo;
    AlertDialog loadingView;
    AppCompatButton btnConfirm;

    public UsingSealFragment() {
        // Required empty public constructor
    }

    public static UsingSealFragment newInstance(String param1, String param2) {
        UsingSealFragment fragment = new UsingSealFragment();
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
        View view= inflater.inflate(R.layout.fragment_using_seal, container, false);
        initViews(view);
        btnUsingCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingView.setCancelable(false);
                loadingView.show();
                String sealCode=editUsingCode.getText().toString().trim();
                UsingSealSummary.setCurrentSealCode(sealCode);
                PreferenceUtil.setSealCode(sealCode);
                WebServiceUtil.getUsingSealInfo(new SoapCallbackListener() {
                    @Override
                    public void onFinish(String xml, String method, String sealCode, String filePath) {
                        UsingSealInfoResponse response=XmlParseUtil.pullUsingSealInfoResponse(xml);
                        Message message=new Message();
                        message.obj=response;
                        message.what=Constants.MESSAGE_WEB_SERVICE_SUCCEED;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onError(Exception e, String method, String sealCode, String filePath) {
                        handler.sendEmptyMessage(Constants.MESSAGE_WEB_SERVICE_FAIL);
                    }
                });
            }
        });





        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.REQUEST_QR_SCAN:
                if(resultCode== Activity.RESULT_OK){
                    String qrText=data.getStringExtra(Constants.qr_text);
                    editUsingCode.setText(qrText);
                    btnUsingCode.callOnClick();
                }
                break;
        }
    }

    private void initViews(View view) {
        editUsingCode = view.findViewById(R.id.edit_using_code);
        btnUsingCode= view.findViewById(R.id.btn_using_code);
        listSealInfo=view.findViewById(R.id.list_seal_info);
        btnScan=view.findViewById(R.id.btn_scan);
        loadingView=new SpotsDialog(getContext(),getResources().getText(R.string.checking));
        btnConfirm=view.findViewById(R.id.btn_confirm);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),QRCodeReaderActivity.class);
                startActivityForResult(intent, Constants.REQUEST_QR_SCAN);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),TakePhotoActivity.class);
                intent.putExtra(Constants.web_service_method,WebServiceUtil.uploadByUsing);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case Constants.MESSAGE_WEB_SERVICE_SUCCEED:
                    loadingView.dismiss();
                    UsingSealInfoResponse sealInfoResult=(UsingSealInfoResponse)msg.obj;
                    int sealStatus=sealInfoResult.getSealCount();
                    if(sealStatus==0){
                        Card warnCard = new Card.Builder(getContext())
                                .withProvider(new CardProvider())
                                .setLayout(R.layout.material_small_image_card)
                                .setTitle(R.string.using_seal_code_invalid)
                                .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorWarningLight))
                                .endConfig()
                                .build();
                        listSealInfo.getAdapter().clearAll();
                        listSealInfo.getAdapter().add(warnCard);
                        return;
                    }
                    if(sealStatus==-1){
                        Card warnCard = new Card.Builder(getContext())
                                .withProvider(new CardProvider())
                                .setLayout(R.layout.material_small_image_card)
                                .setTitle(R.string.hardware_code_invalid)
                                .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorWarningLight))
                                .endConfig()
                                .build();
                        listSealInfo.getAdapter().clearAll();
                        listSealInfo.getAdapter().add(warnCard);
                        return;
                    }
                    if(sealStatus==-2){
                        Card warnCard = new Card.Builder(getContext())
                                .withProvider(new CardProvider())
                                .setLayout(R.layout.material_small_image_card)
                                .setTitle(R.string.using_seal_code_used)
                                .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorWarningLight))
                                .endConfig()
                                .build();
                        listSealInfo.getAdapter().clearAll();
                        listSealInfo.getAdapter().add(warnCard);
                        return;
                    }

                    String result="";
                    for(UsingSealInfoItem seal : sealInfoResult.getSealList()){
                        result=result+ UsingSealSummary.translateUsingSealItemToChinese(seal)+"\n";
                    }
                    result=result.substring(0,result.length()-1);
                    btnConfirm.setVisibility(View.VISIBLE);
                    btnUsingCode.setVisibility(View.GONE);
                    Card sealCard = new Card.Builder(getContext())
                            .withProvider(new CardProvider())
                            .setLayout(R.layout.material_small_image_card)
                            .setTitle(result)
                            //.setDescription(R.string.using_seal_code_description)
                            .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorLight))
                            .endConfig()
                            .build();
                    listSealInfo.getAdapter().clearAll();
                    listSealInfo.getAdapter().add(sealCard);
                    break;
                case Constants.MESSAGE_WEB_SERVICE_FAIL:
                    loadingView.dismiss();
                    Toast.makeText(getContext(),"网络暂时无法连接，请使用紧急用印功能",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
