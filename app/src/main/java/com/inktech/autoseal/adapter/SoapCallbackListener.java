package com.inktech.autoseal.adapter;

/**
 * Created by Chaoyu on 2017/8/27.
 */

public interface SoapCallbackListener {
    void onFinish(String xml,String method,String sealCode,String filePath);
    void onError(Exception e,String method,String sealCode,String filePath);
}
