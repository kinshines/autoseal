package com.inktech.autoseal.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.view.MaterialListView;
import com.inktech.autoseal.R;
import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.model.UsingSealInfoResponse;
import com.inktech.autoseal.model.UsingSealSummary;
import com.inktech.autoseal.util.PreferenceUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.util.XmlParseUtil;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReturnSealFragment extends Fragment {

    AppCompatEditText editUsingCode;
    AppCompatButton btnUsingCode;
    AppCompatButton btnScan;
    MaterialListView listSealInfo;
    AlertDialog loadingView;

    public ReturnSealFragment() {
        // Required empty public constructor
    }

    public static ReturnSealFragment newInstance(String param1, String param2) {
        ReturnSealFragment fragment = new ReturnSealFragment();
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
        View view = inflater.inflate(R.layout.fragment_return_seal, container, false);
        initViews(view);
        btnUsingCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingView.setCancelable(false);
                loadingView.show();
                String sealCode=editUsingCode.getText().toString().trim();
                PreferenceUtil.setSealCode(sealCode);
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
        loadingView=new SpotsDialog(getContext(),getResources().getText(R.string.checking));
    }

}
