package com.inktech.autoseal.model;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Chaoyu on 2017/8/28.
 */

public class SealSummary {
    private static HashMap<String,Integer>overall=new HashMap<>();
    private static HashMap<String,Integer> process=new HashMap<>();
    private static String currentSealType="";
    public static void addMap(String sealType,int totalCount){
        overall.put(sealType,totalCount);
    }

    public static int completeOnce(){
        int processCount=process.get(currentSealType);
        process.put(currentSealType,processCount+1);
        return processCount+1;
    }

    public static String getCurrentSealType(){
        currentSealType=trySealType("gz");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("frz");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("cwz");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("htz");
        if(!TextUtils.isEmpty(currentSealType))
            return currentSealType;
        currentSealType=trySealType("fpz");
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

    public static String translateSealTypeToChinese(String sealType){
        if("gz".equals(sealType))
            return "公章";
        if("frz".equals(sealType))
            return "法人章";
        if("cwz".equals(sealType))
            return "财务章";
        if("htz".equals(sealType))
            return "合同专用章";
        if("fpz".equals(sealType))
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
}