package com.inktech.autoseal.utility;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.inktech.autoseal.MyApplication;

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
    private static final String WebServiceUrl="http://192.168.18.213:8020/Api.asmx";
    private static final String UsingSealInfoMethod="getUsingSealInfo";
    private static final String UsingSealInfoAction="http://tempuri.org/getUsingSealInfo";
    private static final String UploadByUsingMethod="uploadByUsing";
    private static final String UploadByUsingAction="http://tempuri.org/uploadByUsing";

    private static void sendRequest(final String method, final String action, final Map<String,Object> propertyMap, final SoapCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpTransportSE httpTransportSE=null;
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
                    httpTransportSE=new HttpTransportSE(WebServiceUrl);
                    httpTransportSE.call(action,envelope);
                    SoapObject object=(SoapObject)envelope.bodyIn;
                    listener.onFinish(object);
                }
                catch (Exception e){
                    listener.onError(e);
                }
            }
        }).start();
    }
    public static void getUsingSealInfo(final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String sealCode=pref.getString("sealCode","");
        map.put("usingSealCode",sealCode);
        map.put("hardwareCode","hardwareCode");
        sendRequest(UsingSealInfoMethod,UsingSealInfoAction,map,listener);
    }

    public static void uploadByUsing(final String filePath,final String position,final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        map.put("sealCode","sealCode");
        File file=new File(filePath);
        map.put("fileByte",getBytes(file));
        map.put("filename",file.getName());
        map.put("position",position);
        sendRequest(UploadByUsingMethod,UploadByUsingAction,map,listener);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
