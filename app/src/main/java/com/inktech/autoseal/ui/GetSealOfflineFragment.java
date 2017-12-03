package com.inktech.autoseal.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.inktech.autoseal.R;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.model.OutSealInfoItemOffline;
import com.inktech.autoseal.model.OutSealSummary;
import com.inktech.autoseal.util.PreferenceUtil;
import com.inktech.autoseal.util.SealOfflineUtil;
import com.inktech.autoseal.util.WebServiceUtil;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetSealOfflineFragment extends Fragment {

    AppCompatEditText editUsingCode;
    AppCompatButton btnUsingCode;
    AppCompatButton btnScan;
    MaterialListView listSealInfo;
    OutSealInfoItemOffline sealInfoOffline;
    AppCompatButton btnConfirm;

    public GetSealOfflineFragment() {
        // Required empty public constructor
    }

    public static GetSealOfflineFragment newInstance(String param1, String param2) {
        GetSealOfflineFragment fragment = new GetSealOfflineFragment();
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
        View view = inflater.inflate(R.layout.fragment_get_seal_offline, container, false);

        initViews(view);

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
        btnConfirm=view.findViewById(R.id.btn_confirm);

        btnUsingCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sealCode=editUsingCode.getText().toString().trim();
                OutSealSummary.setCurrentSealCode(sealCode);
                PreferenceUtil.setSealCode(sealCode);
                sealInfoOffline= SealOfflineUtil.validateOutSealCode(sealCode);
                if(sealInfoOffline==null){
                    Card warnCard = new Card.Builder(getContext())
                            .withProvider(new CardProvider())
                            .setLayout(R.layout.material_small_image_card)
                            .setTitle(R.string.get_seal_code_invalid)
                            .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorWarningLight))
                            .endConfig()
                            .build();
                    listSealInfo.getAdapter().clearAll();
                    listSealInfo.getAdapter().add(warnCard);
                    return;
                }
                btnConfirm.setVisibility(View.VISIBLE);
                String result= OutSealSummary.translateOutSealItemToChinese(sealInfoOffline);
                Card sealCard = new Card.Builder(getContext())
                        .withProvider(new CardProvider())
                        .setLayout(R.layout.material_small_image_card)
                        .setTitle(result)
                        .setDescription(R.string.get_seal_code_description)
                        .setBackgroundColor(ContextCompat.getColor(getContext(),R.color.colorLight))
                        .endConfig()
                        .build();
                listSealInfo.getAdapter().clearAll();
                listSealInfo.getAdapter().add(sealCard);
            }
        });

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
                intent.putExtra(Constants.web_service_method, WebServiceUtil.uploadByUrgentOut);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

}
