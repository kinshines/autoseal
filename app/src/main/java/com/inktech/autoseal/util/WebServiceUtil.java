package com.inktech.autoseal.util;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.constant.MyApplication;
import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.db.FileUploadRecord;
import com.inktech.autoseal.model.OutSealSummary;
import com.inktech.autoseal.model.UsingSealSummary;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chaoyu on 2017/8/27.
 */

public class WebServiceUtil {
    private static final String AddressNameSpace = "http://tempuri.org/";
    private static String WebServiceUrl=getServerIp();
    public static final String UsingSealInfo ="getUsingSealInfo";
    public static final String OutSealInfo="getOutSealInfo";
    public static final String UpdateUsingSealCode="updateUsingSealCode";
    public static final String UpdateOutSealCode="updateOutSealCode";


    public static final String uploadByUsing ="uploadByUsing";
    public static final String uploadByOut ="uploadByOut";
    public static final String uploadByUrgentUsing ="uploadByUrgentUsing";
    public static final String uploadByUrgentOut ="uploadByUrgentOut";

    private static void sendRequest(final String method, final Map<String,Object> propertyMap, final SoapCallbackListener listener){
        sendRequest(method,propertyMap,listener,"");
    }
    private static void sendRequest(final String method, final Map<String,Object> propertyMap, final SoapCallbackListener listener,final String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpTransportSE httpTransportSE=null;
                String sealCode="";
                if(propertyMap.containsKey("sealCode")){
                    sealCode=propertyMap.get("sealCode").toString();
                }
                try {
                    SoapObject soapObject=new SoapObject(AddressNameSpace,method);
                    for (String key:propertyMap.keySet()) {
                        soapObject.addProperty(key,propertyMap.get(key));
                    }
                    SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.bodyOut=soapObject;
                    envelope.dotNet=true;
                    envelope.setOutputSoapObject(soapObject);
                    if (method.contains("uploadBy")){
                        new MarshalBase64().register(envelope);
                    }
                    httpTransportSE=new HttpTransportSE(getServerIp());
                    String action=AddressNameSpace+method;
                    httpTransportSE.call(action,envelope);
                    SoapObject object=(SoapObject)envelope.bodyIn;
                    String xml=object.getPropertySafelyAsString(method+"Result");
                    listener.onFinish(xml,method,sealCode,filePath);
                }
                catch (Exception e){
                    listener.onError(e,method,sealCode,filePath);
                }
            }
        }).start();
    }
    public static void getUsingSealInfo(final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        String sealCode= UsingSealSummary.getCurrentSealCode();
        map.put("usingSealCode",sealCode);
        map.put("hardwareCode",PreferenceUtil.getHardwareCode());
        sendRequest(UsingSealInfo,map,listener);
    }

    public static void getOutSealInfo(final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        String sealCode= OutSealSummary.getCurrentSealCode();
        map.put("outSealCode",sealCode);
        map.put("hardwareCode",PreferenceUtil.getHardwareCode());
        sendRequest(OutSealInfo,map,listener);
    }

    public static void uploadByMethod(final String method,final String filePath,final Integer position,final String sealName,final SoapCallbackListener listener){
        String sealCode="";
        if(uploadByUsing.equals(method)||uploadByUrgentUsing.equals(method)){
            sealCode= UsingSealSummary.getCurrentSealCode();
        }else{
            sealCode=OutSealSummary.getCurrentSealCode();
        }
        uploadBy(sealCode,method,filePath,position,sealName, listener);
    }

    private static void uploadBy(String sealCode,final String method,final String filePath,final Integer position,final String sealName,final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        map.put("sealCode",sealCode);
        File file=new File(filePath);
        if(!file.exists()){
            DbUtil.uploadSuccess(method,sealCode,filePath,position,sealName);
            return;
        }
        byte[] fileByte=getBytes(file);
        if(fileByte==null){
            listener.onError(new Exception("file byte null"),method,sealCode,filePath);
            return;
        }
        map.put("fileByte",fileByte);
        map.put("filename",file.getName());
        String positionChi="";
        if(Constants.User.equals(position)){
            positionChi="用印人";
        }
        if(Constants.UserForOut.equals(position)){
            positionChi="取印人";
        }
        if(Constants.Document.equals(position)){
            positionChi=sealName;
        }
        map.put("position",positionChi);
        sendRequest(method,map,listener,filePath);
    }

    public static void updateUsingSealCode(final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        map.put("hardwareCode",PreferenceUtil.getHardwareCode());
        sendRequest(UpdateUsingSealCode,map,listener);
    }

    public static void updateOutSealCode(final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        map.put("hardwareCode",PreferenceUtil.getHardwareCode());
        sendRequest(UpdateOutSealCode,map,listener);
    }

    private static byte[] getBytes(File file){
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            buffer=null;
        } catch (IOException e) {
            e.printStackTrace();
            buffer=null;
        }
        return buffer;
    }

    private static String getServerIp(){
        SharedPreferences pref  = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String savedIp=pref.getString("serverIP","124.128.33.110:10003");
        if(TextUtils.isEmpty(savedIp)){
            savedIp="124.128.33.110:10003";
        }
        return "http://"+savedIp+"/WebService/WebService.asmx";
    }

    public static void uploadByRecord(FileUploadRecord record,final SoapCallbackListener listener){
        String method="";
        Integer sealType=record.getSealType();
        if(Constants.uploadByUsing.equals(sealType)){
            method=uploadByUsing;
        }
        if(Constants.uploadByOut.equals(sealType)){
            method=uploadByOut;
        }
        if(Constants.uploadByUrgentOut.equals(sealType)){
            method=uploadByUrgentOut;
        }
        if(Constants.uploadByUrgentUsing.equals(sealType)){
            method=uploadByUrgentUsing;
        }
        uploadBy(record.getSealCode(),method,record.getFilePath(),record.getPosition(),record.getSealName(),listener);
    }

}
