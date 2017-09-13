package com.inktech.autoseal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class OutSealInfoSyncResponse {
    public OutSealInfoSyncResponse(){
        sealCount=0;
        sealList= new ArrayList<>();
    }
    private Integer sealCount;
    private List<OutSealInfoItemOffline> sealList;

    public Integer getSealCount() {
        return sealCount;
    }

    public void setSealCount(Integer sealCount) {
        this.sealCount = sealCount;
    }

    public List<OutSealInfoItemOffline> getSealList() {
        return sealList;
    }

    public void setSealList(List<OutSealInfoItemOffline> sealList) {
        this.sealList = sealList;
    }

    public void addSealToList(OutSealInfoItemOffline sealInfo){
        sealList.add(sealInfo);
    }
}
