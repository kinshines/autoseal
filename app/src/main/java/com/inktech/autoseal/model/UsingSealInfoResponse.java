package com.inktech.autoseal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/12.
 */

public class UsingSealInfoResponse {
    public UsingSealInfoResponse(){
        sealCount=0;
        sealList= new ArrayList<>();
    }
    private Integer sealCount;
    private List<UsingSealInfoItem> sealList;

    public Integer getSealCount() {
        return sealCount;
    }

    public void setSealCount(Integer sealCount) {
        this.sealCount = sealCount;
    }

    public List<UsingSealInfoItem> getSealList() {
        return sealList;
    }

    public void setSealList(List<UsingSealInfoItem> sealList) {
        this.sealList = sealList;
    }

    public void addSealToList(UsingSealInfoItem sealInfo){
        sealList.add(sealInfo);
    }
}

