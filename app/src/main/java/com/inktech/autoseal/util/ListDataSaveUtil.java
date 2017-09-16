package com.inktech.autoseal.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inktech.autoseal.constant.MyApplication;
import com.inktech.autoseal.model.UsingSealInfoItemOffline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/15.
 */

public class ListDataSaveUtil {

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
     * @param tag
     * @return
     */
    public static ArrayList<UsingSealInfoItemOffline> getUsingSealInfoItemOfflineList(String tag) {
        ArrayList<UsingSealInfoItemOffline> datalist=new ArrayList<>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String strJson = prefs.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<ArrayList<UsingSealInfoItemOffline>>() {
        }.getType());
        return datalist;

    }
}
