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
    List<UsingSealInfoItemOffline> sealList;

    public Integer getSealCount() {
        return sealCount;
    }

    public void setSealCount(Integer sealCount) {
        this.sealCount = sealCount;
    }

    public List<UsingSealInfoItemOffline> getSealList() {
        return sealList;
    }

    public void setSealList(List<UsingSealInfoItemOffline> sealList) {
        this.sealList = sealList;
    }

    public void addSealToList(UsingSealInfoItemOffline sealInfo){
        sealList.add(sealInfo);
    }
}
