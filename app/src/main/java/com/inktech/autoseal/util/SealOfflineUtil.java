package com.inktech.autoseal.util;

import android.support.annotation.Nullable;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.model.UsingSealInfoItemOffline;

import java.util.ArrayList;

/**
 * Created by Chaoyu on 2017/9/17.
 */

public class SealOfflineUtil {
    public static boolean needUpdateUsingSeal(){
        ArrayList<UsingSealInfoItemOffline> list= PreferenceUtil.getUsingSealInfoItemOfflineList(Constants.OfflineUsingSealCode);
        if(list.size()==0)
            return true;
        if(!hasAllTypeCode(list))
            return true;
        return false;
    }

    public static void saveOfflineUsingSealList(ArrayList<UsingSealInfoItemOffline> list){
        PreferenceUtil.setDataList(Constants.OfflineUsingSealCode,list);
    }

    @Nullable
    public static UsingSealInfoItemOffline validateUsingSealCode(String sealCode){
        ArrayList<UsingSealInfoItemOffline> list= PreferenceUtil.getUsingSealInfoItemOfflineList(Constants.OfflineUsingSealCode);
        String today=DateUtil.getShortDate();
        for(UsingSealInfoItemOffline item:list){
            if(sealCode.equals(item.getSealCode())&&today.equals(item.getTimeStamp()))
                return item;
        }
        return null;
    }

    private static boolean hasAllTypeCode(ArrayList<UsingSealInfoItemOffline> list){
        return hasCodeForSealType(Constants.gz,list)
                &&hasCodeForSealType(Constants.frz,list)
                &&hasCodeForSealType(Constants.cwz,list)
                &&hasCodeForSealType(Constants.htz,list)
                &&hasCodeForSealType(Constants.fpz,list);
    }
    private static boolean hasCodeForSealType(String sealType,ArrayList<UsingSealInfoItemOffline> list){
        boolean hasCode=false;
        String today=DateUtil.getShortDate();
        for(UsingSealInfoItemOffline item:list){
            if(sealType.equals(item.getType())&&today.equals(item.getTimeStamp())){
                hasCode=true;
                break;
            }
        }
        return hasCode;
    }

    public static void removeSealInfoItemOffline(UsingSealInfoItemOffline item){
        ArrayList<UsingSealInfoItemOffline> list= PreferenceUtil.getUsingSealInfoItemOfflineList(Constants.OfflineUsingSealCode);
        list.remove(item);
        saveOfflineUsingSealList(list);
    }
}
