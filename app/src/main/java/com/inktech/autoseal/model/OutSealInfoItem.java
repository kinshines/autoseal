package com.inktech.autoseal.model;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class OutSealInfoItem {
    public OutSealInfoItem(){
        type="";
    }
    public OutSealInfoItem(String type){
        this.type=type;
    }
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
