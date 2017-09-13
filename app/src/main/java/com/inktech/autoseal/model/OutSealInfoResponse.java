package com.inktech.autoseal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class OutSealInfoResponse {
    public OutSealInfoResponse(){
        sealCount=0;
        sealList= new ArrayList<>();
    }
    private Integer sealCount;
    private List<OutSealInfoItem> sealList;

    public Integer getSealCount() {
        return sealCount;
    }

    public void setSealCount(Integer sealCount) {
        this.sealCount = sealCount;
    }

    public List<OutSealInfoItem> getSealList() {
        return sealList;
    }

    public void setSealList(List<OutSealInfoItem> sealList) {
        this.sealList = sealList;
    }

    public void addSealToList(OutSealInfoItem sealInfo){
        sealList.add(sealInfo);
    }
}
