package com.inktech.autoseal.util;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.inktech.autoseal.constant.MyApplication;
import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.db.FileUploadRecord;
import com.inktech.autoseal.db.PhotoPosition;
import com.inktech.autoseal.db.SealType;

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
    public static final String UpdateUsingSealCode="updateUsingSealCode";

    public static final String UploadByUsing ="uploadByUsing";
    public static final String UploadByOut="uploadByOut";
    public static final String UploadByUrgentUsing="uploadByUrgentUsing";
    public static final String UploadByUrgentOut="uploadByUrgentOut";

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
                String position="";
                if(propertyMap.containsKey("position")){
                    position=propertyMap.get("position").toString();
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
                    httpTransportSE=new HttpTransportSE(WebServiceUrl);
                    String action=AddressNameSpace+method;
                    httpTransportSE.call(action,envelope);
                    SoapObject object=(SoapObject)envelope.bodyIn;
                    String xml=object.getPropertySafelyAsString(method+"Result");
                    listener.onFinish(xml,method,sealCode,filePath,position);
                }
                catch (Exception e){
                    listener.onError(e,method,sealCode,filePath,position);
                }
            }
        }).start();
    }
    public static void getUsingSealInfo(final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String sealCode=pref.getString("sealCode","");
        map.put("usingSealCode","UCJ3EHZOXTO0");
        map.put("hardwareCode","GF199510");
        sendRequest(UsingSealInfo,map,listener);
    }

    public static void uploadByUsing(final String filePath,final String position,final SoapCallbackListener listener){
        String sealCode="UCJ3EHZOXTO0";
        uploadBy(sealCode,UploadByUsing,filePath,position,listener);
    }

    private static void uploadBy(String sealCode,final String method,final String filePath,final String position,final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        map.put("sealCode",sealCode);
        File file=new File(filePath);
        map.put("fileByte",getBytes(file));
        map.put("filename",file.getName());
        map.put("postion",position);
        sendRequest(method,map,listener,filePath);
    }

    public static void updateUsingSealCode(final SoapCallbackListener listener){
        Map<String,Object> map=new HashMap<>();
        map.put("hardwareCode","YZCS001");
        sendRequest(UpdateUsingSealCode,map,listener);
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

    private static String getServerIp(){
        SharedPreferences pref  = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String savedIp=pref.getString("serverIP","124.128.33.110");
        return "http://"+savedIp+":10003/WebService/WebService.asmx";
    }

    public static void uploadByRecord(FileUploadRecord record,final SoapCallbackListener listener){
        String position= PhotoPosition.User.equals(record.getPosition())?"用印人":"文档";
        uploadBy(record.getSealCode(),record.getSealType().toString(),record.getFilePath(),position,listener);
    }

}
