package com.inktech.autoseal.model;

import java.io.Serializable;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class OutSealInfoItemOffline extends OutSealInfoItem implements Serializable {
    private String sealCode;

    public String getSealCode() {
        return sealCode;
    }

    public void setSealCode(String sealCode) {
        this.sealCode = sealCode;
    }
}
