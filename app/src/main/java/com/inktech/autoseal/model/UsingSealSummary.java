package com.inktech.autoseal.model;

import android.text.TextUtils;

import com.inktech.autoseal.constant.Constants;

import java.util.HashMap;

/**
 * Created by Chaoyu on 2017/8/28.
 */

public class UsingSealSummary {
    private static HashMap<String,Integer>overall=new HashMap<>();
    private static HashMap<String,Integer> process=new HashMap<>();
    private static HashMap<String,String> sealNameMap=new HashMap<>();
    private static String currentSealType="";
    private static String currentSealCode="";

    public static void addMap(String sealType,int totalCount){
        overall.put(sealType,totalCount);
    }

    public static int completeOnce(){
        int processCount=process.get(currentSealType);
        process.put(currentSealType,processCount+1);
        return processCount+1;
    }

    public static String getCurrentSealType(){
        currentSealType=trySealType(Constants.gz);
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType(Constants.frz);
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType(Constants.cwz);
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType(Constants.htz);
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType(Constants.fpz);
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        return "";
    }

    public static String getSealTypeChinese(){
        return sealNameMap.get(currentSealType);
    }

    public static int getCurrentSealCount(){
        return process.get(currentSealType)+1;
    }

    public static boolean isCurrentSealEnd(){
        return getCurrentSealCount()==overall.get(currentSealType);
    }

    private static String trySealType(String sealType){
        if(!overall.containsKey(sealType))
            return "";
        if(!process.containsKey(sealType)){
            process.put(sealType,0);
            return sealType;
        }
        if(overall.get(sealType).equals(process.get(sealType))){
            return "";
        }else {
            return sealType;
        }
    }

    private static void init(){
        overall.clear();
        process.clear();
        sealNameMap.clear();
    }

    public static String getCurrentSealCode(){
        return currentSealCode;
    }
    public static void setCurrentSealCode(String code){
        currentSealCode=code;
        init();
    }

    public static String translateUsingSealItemToChinese(UsingSealInfoItem seal){
        String type=seal.getType();
        int count=seal.getCount();
        String chineseType= seal.getSealName();
        addMap(type,count);
        sealNameMap.put(type,chineseType);
        return chineseType+"：盖章 "+count+" 次";
    }
}
