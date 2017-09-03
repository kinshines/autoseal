package com.inktech.autoseal.Util;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by Chaoyu on 2017/8/27.
 */

public interface SoapCallbackListener {
    void onFinish(SoapObject soapObject);
    void onError(Exception e);
}
