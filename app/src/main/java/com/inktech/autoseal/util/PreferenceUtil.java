package com.inktech.autoseal.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.constant.MyApplication;
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
        if (null == datalist || datalist.size() == 0)
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
        ArrayList<OutSealInfoItemOffline> datalist=new ArrayList<>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String strJson = prefs.getString(Constants.OfflineOutSealCode, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<ArrayList<OutSealInfoItemOffline>>() {
        }.getType());
        return datalist;
    }

    public static void setSealCode(String sealCode){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        editor.putString("sealCode",sealCode);
        editor.apply();
    }
}
