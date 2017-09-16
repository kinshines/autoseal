package com.inktech.autoseal.model;

import com.inktech.autoseal.util.DateUtil;

import java.io.Serializable;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class OutSealInfoItemOffline extends OutSealInfoItem implements Serializable {
    private String sealCode;
    private String timeStamp;

    public OutSealInfoItemOffline(){
        timeStamp= DateUtil.getShortDate();
    }

    public String getSealCode() {
        return sealCode;
    }

    public void setSealCode(String sealCode) {
        this.sealCode = sealCode;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
