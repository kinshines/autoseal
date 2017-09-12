package com.inktech.autoseal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/12.
 */

public class SealInfoResult {
    public SealInfoResult(){
        sealCount=0;
        sealList= new ArrayList<>();
    }
    Integer sealCount;
    List<SealInfo> sealList;

    public Integer getSealCount() {
        return sealCount;
    }

    public void setSealCount(Integer sealCount) {
        this.sealCount = sealCount;
    }

    public List<SealInfo> getSealList() {
        return sealList;
    }

    public void setSealList(List<SealInfo> sealList) {
        this.sealList = sealList;
    }

    public void addSealList(SealInfo sealInfo){
        sealList.add(sealInfo);
    }
}

