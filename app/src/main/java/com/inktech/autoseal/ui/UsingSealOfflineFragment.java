package com.inktech.autoseal.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.inktech.autoseal.model.SealSummary;
import com.inktech.autoseal.model.UsingSealInfoItemOffline;
import com.inktech.autoseal.util.PreferenceUtil;
import com.inktech.autoseal.util.SealOfflineUtil;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsingSealOfflineFragment extends Fragment {

    AppCompatEditText editUsingCode;
    AppCompatButton btnUsingCode;
    AppCompatButton btnScan;
    MaterialListView listSealInfo;
    AlertDialog loadingView;

    public UsingSealOfflineFragment() {
        // Required empty public constructor
    }
    public static UsingSealOfflineFragment newInstance(String param1, String param2) {
        UsingSealOfflineFragment fragment = new UsingSealOfflineFragment();
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
        View view = inflater.inflate(R.layout.fragment_using_seal_offline, container, false);
        initViews(view);

        btnUsingCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sealCode=editUsingCode.getText().toString().trim();
                SealSummary.setCurrentSealCode(sealCode);
                PreferenceUtil.setSealCode(sealCode);
                UsingSealInfoItemOffline sealInfo=SealOfflineUtil.validateUsingSealCode(sealCode);
                if(sealInfo==null){
                    Toast.makeText(getContext(),R.string.using_seal_code_invalid,Toast.LENGTH_LONG).show();
                    return;
                }
                String result=SealSummary.translateSealItemToChinese(sealInfo);
                Card sealCard = new Card.Builder(getContext())
                        .withProvider(new CardProvider())
                        .setLayout(R.layout.material_welcome_card_layout)
                        .setTitle(result)
                        .setDescription(R.string.using_seal_code_description)
                        .setBackgroundColor(getResources().getColor(R.color.colorLight))
                        .addAction(R.id.ok_button, new WelcomeButtonAction(getContext())
                                .setText(R.string.confirm_take_photo)
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
        loadingView=new SpotsDialog(getContext(),"校验中……");
    }

}
