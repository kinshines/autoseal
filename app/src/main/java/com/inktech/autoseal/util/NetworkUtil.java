package com.inktech.autoseal.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.inktech.autoseal.constant.MyApplication;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class NetworkUtil {
    public static boolean isNetworkConnected(){

        ConnectivityManager manager = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null){
            return true;
        }

        return false;
    }
}
