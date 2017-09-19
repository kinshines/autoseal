package com.inktech.autoseal.model;

import java.io.Serializable;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class OutSealInfoItem {
    public OutSealInfoItem(){
        type="";
    }
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String sealName;

    public String getSealName() {
        return sealName;
    }

    public void setSealName(String sealName) {
        this.sealName = sealName;
    }
}
