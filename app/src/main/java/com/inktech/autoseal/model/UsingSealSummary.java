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
        currentSealType=trySealType("仓位1");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("仓位2");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("仓位3");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("仓位4");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("仓位5");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        return "";
    }

    public static String getSealTypeChinese(){
        return translateSealTypeToChinese(currentSealType);
    }

    public static int getCurrentSealCount(){
        return process.get(currentSealType)+1;
    }

    public static boolean isCurrentSealEnd(){
        return getCurrentSealCount()==overall.get(currentSealType);
    }

    public static String translateSealTypeToChinese(String sealType){
        if(Constants.gz.equals(sealType))
            return "公章";
        if(Constants.frz.equals(sealType))
            return "法人章";
        if(Constants.cwz.equals(sealType))
            return "财务章";
        if(Constants.htz.equals(sealType))
            return "合同专用章";
        if(Constants.fpz.equals(sealType))
            return "发票专用章";
        return sealType;
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
        String chineseType= translateSealTypeToChinese(type);
        addMap(type,count);
        return chineseType+"：盖章 "+count+" 次";
    }
}
