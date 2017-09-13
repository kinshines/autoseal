package com.inktech.autoseal.constant;

import android.app.Application;
import android.content.Context;

/**
 * Created by Chaoyu on 2017/8/27.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
}
