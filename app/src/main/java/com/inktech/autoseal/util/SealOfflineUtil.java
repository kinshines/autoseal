package com.inktech.autoseal.util;

import android.support.annotation.Nullable;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.model.OutSealInfoItemOffline;
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
        return validateUsingSealCode(list,sealCode);
    }

    @Nullable
    private static UsingSealInfoItemOffline validateUsingSealCode(ArrayList<UsingSealInfoItemOffline> list,String sealCode){
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

    public static void removeUsingSealInfoItemOffline(String sealCode){
        ArrayList<UsingSealInfoItemOffline> list= PreferenceUtil.getUsingSealInfoItemOfflineList(Constants.OfflineUsingSealCode);
        UsingSealInfoItemOffline item=validateUsingSealCode(list,sealCode);
        if(item==null)
            return;
        list.remove(item);
        saveOfflineUsingSealList(list);
    }

    public static boolean needUpdateOutSeal(){
        ArrayList<OutSealInfoItemOffline> list= PreferenceUtil.getOutSealInfoItemOfflineList(Constants.OfflineOutSealCode);
        if(list.size()==0)
            return true;
        if(!hasAllTypeOutCode(list))
            return true;
        return false;
    }

    public static void saveOfflineOutSealList(ArrayList<OutSealInfoItemOffline> list){
        PreferenceUtil.setDataList(Constants.OfflineOutSealCode,list);
    }

    @Nullable
    public static OutSealInfoItemOffline validateOutSealCode(String sealCode){
        ArrayList<OutSealInfoItemOffline> list= PreferenceUtil.getOutSealInfoItemOfflineList(Constants.OfflineOutSealCode);
        return validateOutSealCode(list,sealCode);
    }

    @Nullable
    private static OutSealInfoItemOffline validateOutSealCode(ArrayList<OutSealInfoItemOffline> list,String sealCode){
        String today=DateUtil.getShortDate();
        for(OutSealInfoItemOffline item:list){
            if(sealCode.equals(item.getSealCode())&&today.equals(item.getTimeStamp()))
                return item;
        }
        return null;
    }

    private static boolean hasAllTypeOutCode(ArrayList<OutSealInfoItemOffline> list){
        return hasOutCodeForSealType(Constants.gz,list)
                &&hasOutCodeForSealType(Constants.frz,list)
                &&hasOutCodeForSealType(Constants.cwz,list)
                &&hasOutCodeForSealType(Constants.htz,list)
                &&hasOutCodeForSealType(Constants.fpz,list);
    }
    private static boolean hasOutCodeForSealType(String sealType,ArrayList<OutSealInfoItemOffline> list){
        boolean hasCode=false;
        String today=DateUtil.getShortDate();
        for(OutSealInfoItemOffline item:list){
            if(sealType.equals(item.getType())&&today.equals(item.getTimeStamp())){
                hasCode=true;
                break;
            }
        }
        return hasCode;
    }

    public static void removeOutSealInfoItemOffline(String sealCode){
        ArrayList<OutSealInfoItemOffline> list= PreferenceUtil.getOutSealInfoItemOfflineList(Constants.OfflineUsingSealCode);
        OutSealInfoItemOffline item=validateOutSealCode(list,sealCode);
        if(item==null)
            return;
        list.remove(item);
        saveOfflineOutSealList(list);
    }
}
