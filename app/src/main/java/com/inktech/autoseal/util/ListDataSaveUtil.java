package com.inktech.autoseal.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inktech.autoseal.constant.MyApplication;

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
    public static <T> void setDataList(String tag, List<T> datalist) {
        if (null == datalist || datalist.size() <= 0)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        SharedPreferences.Editor editor=prefs.edit();
        editor.putString(tag, strJson);
        editor.commit();
    }

    /**
     * 获取List
     * @param tag
     * @return
     */
    public static <T> List<T> getDataList(String tag) {
        List<T> datalist=new ArrayList<T>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String strJson = prefs.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;

    }
}
