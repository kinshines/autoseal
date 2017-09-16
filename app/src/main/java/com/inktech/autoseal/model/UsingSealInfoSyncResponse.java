package com.inktech.autoseal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class UsingSealInfoSyncResponse {

    public UsingSealInfoSyncResponse(){
        sealCount=0;
        sealList=new ArrayList<>();
    }
    Integer sealCount;
    ArrayList<UsingSealInfoItemOffline> sealList;

    public Integer getSealCount() {
        return sealCount;
    }

    public void setSealCount(Integer sealCount) {
        this.sealCount = sealCount;
    }

    public ArrayList<UsingSealInfoItemOffline> getSealList() {
        return sealList;
    }

    public void setSealList(ArrayList<UsingSealInfoItemOffline> sealList) {
        this.sealList = sealList;
    }

    public void addSealToList(UsingSealInfoItemOffline sealInfo){
        sealList.add(sealInfo);
    }
}
