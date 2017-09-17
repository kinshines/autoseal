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
    private ArrayList<OutSealInfoItemOffline> sealList;

    public Integer getSealCount() {
        return sealCount;
    }

    public void setSealCount(Integer sealCount) {
        this.sealCount = sealCount;
    }

    public ArrayList<OutSealInfoItemOffline> getSealList() {
        return sealList;
    }

    public void setSealList(ArrayList<OutSealInfoItemOffline> sealList) {
        this.sealList = sealList;
    }

    public void addSealToList(OutSealInfoItemOffline sealInfo){
        sealList.add(sealInfo);
    }
}
