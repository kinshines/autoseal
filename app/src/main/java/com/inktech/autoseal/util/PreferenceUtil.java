package com.inktech.autoseal.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.constant.MyApplication;
import com.inktech.autoseal.model.OutSealInfoItem;
import com.inktech.autoseal.model.OutSealInfoItemOffline;
import com.inktech.autoseal.model.UsingSealInfoItemOffline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/15.
 */

public class PreferenceUtil {

    /**
     * 保存List
     * @param tag
     * @param datalist
     */
    public static <T> void setDataList(String tag, ArrayList<T> datalist) {
        if (null == datalist)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        SharedPreferences.Editor editor=prefs.edit();
        editor.putString(tag, strJson);
        editor.apply();
    }

    /**
     * 获取List
     * @return
     */
    public static ArrayList<UsingSealInfoItemOffline> getUsingSealInfoItemOfflineList() {
        ArrayList<UsingSealInfoItemOffline> datalist=new ArrayList<>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String strJson = prefs.getString(Constants.OfflineUsingSealCode, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<ArrayList<UsingSealInfoItemOffline>>() {
        }.getType());
        return datalist;
    }

    /**
     * 获取List
     * @return
     */
    public static ArrayList<OutSealInfoItemOffline> getOutSealInfoItemOfflineList() {
        return getOutSealInfoItemOfflineListByKey(Constants.OfflineOutSealCode);
    }

    public static void setSealCode(String sealCode){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString("sealCode",sealCode);
        editor.apply();
    }

    private static ArrayList<OutSealInfoItemOffline> getOutSealRecordList(){
        return getOutSealInfoItemOfflineListByKey(Constants.OutSealRecord);
    }

    private static ArrayList<OutSealInfoItemOffline> getOutSealInfoItemOfflineListByKey(String key){
        ArrayList<OutSealInfoItemOffline> datalist=new ArrayList<>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String strJson = prefs.getString(key, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<ArrayList<OutSealInfoItemOffline>>() {
        }.getType());
        return datalist;
    }

    public static ArrayList<OutSealInfoItemOffline> queryOutSealRecordList(String sealCode){
        ArrayList<OutSealInfoItemOffline> list=getOutSealRecordList();
        ArrayList<OutSealInfoItemOffline> datalist=new ArrayList<>();
        for (OutSealInfoItemOffline item:list){
            if(sealCode.equals(item.getSealCode())){
                datalist.add(item);
            }
        }
        return datalist;
    }

    public static void addOutSealRecord(String sealCode,String sealType,String sealName){
        ArrayList<OutSealInfoItemOffline> list=getOutSealRecordList();
        OutSealInfoItemOffline item=new OutSealInfoItemOffline();
        item.setSealCode(sealCode);
        item.setSealName(sealName);
        item.setType(sealType);
        list.add(item);
        setDataList(Constants.OutSealRecord,list);
    }

    public static void removeOutSealRecord(String sealCode,String sealType){
        ArrayList<OutSealInfoItemOffline> list=getOutSealRecordList();
        OutSealInfoItemOffline target=null;
        for (OutSealInfoItemOffline item:list ){
            if(sealCode.equals(item.getSealCode())&&sealType.equals(item.getType())){
                target=item;
                break;
            }
        }
        list.remove(target);
        setDataList(Constants.OutSealRecord,list);
    }

    public static boolean hasOutSealType(String sealType){
        ArrayList<OutSealInfoItemOffline> list=getOutSealRecordList();
        for (OutSealInfoItemOffline item:list){
            if(sealType.equals(item.getType())){
                return true;
            }
        }
        return false;
    }

    public static String getHardwareCode(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        return prefs.getString(Constants.HardwareCode,"");
    }

    private static void setHardwareCode(String hardwareCode){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString(Constants.HardwareCode,hardwareCode);
        editor.apply();
    }
    public static String getBluetoothPairCode(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        return prefs.getString(Constants.BluetoothPairCode,"");
    }
    private static void setBluetoothPairCode(String pairCode){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString(Constants.BluetoothPairCode,pairCode);
        editor.apply();
    }

    public static boolean checkSerialValide(String clearText){
        boolean isValide=false;
        if(clearText.startsWith("SD1")&&clearText.length()>=7){
            String subNum=clearText.substring(3,7);
            if(TextUtils.isDigitsOnly(subNum)){
                isValide=true;
                PreferenceUtil.setHardwareCode("SD1"+subNum);
            }
            if(clearText.length()>7){
                String bluetoothPairCode=clearText.substring(7);
                setBluetoothPairCode(bluetoothPairCode);
            }
        }
        return isValide;
    }
}
