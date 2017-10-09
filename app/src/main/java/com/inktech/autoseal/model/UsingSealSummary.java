package com.inktech.autoseal.model;

import android.text.TextUtils;

import com.inktech.autoseal.constant.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chaoyu on 2017/8/28.
 */

public class UsingSealSummary {
    private static HashMap<String,Integer>overallMap=new HashMap<>();
    private static HashMap<String,Integer> processMap=new HashMap<>();
    private static HashMap<String,String> sealNameMap=new HashMap<>();
    private static ArrayList<String> canceledMap=new ArrayList<>();
    private static String currentSealType="";
    private static String currentSealCode="";

    public static void addMap(String sealType,int totalCount){
        overallMap.put(sealType,totalCount);
    }

    public static int completeOnce(){
        int processCount=0;
        if(processMap.containsKey(currentSealType)){
            processCount=processMap.get(currentSealType);
        }
        processCount=processCount+1;
        processMap.put(currentSealType,processCount);
        return processCount;
    }

    public static Integer getRemainCount(String sealType){
        if(processMap.containsKey(sealType)){
            return overallMap.get(sealType)-processMap.get(sealType);
        }
        return overallMap.get(sealType);
    }

    public static String getSealTypeChinese(String sealType){
        return sealNameMap.get(sealType);
    }

    private static void init(){
        currentSealType="";
        overallMap.clear();
        processMap.clear();
        sealNameMap.clear();
        canceledMap.clear();
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

    public static void setCurrentSealType(String sealType){
        currentSealType=sealType;
    }

    public static HashMap<String,Integer> getOverallMap(){
        return overallMap;
    }

    public static boolean isAllCompleted(){
        for(Map.Entry<String,Integer> entry: overallMap.entrySet()){
            if(!entry.getValue().equals(processMap.get(entry.getKey()))){
                return false;
            }
        }
        return true;
    }

    public static void cancelSeal(String sealType){
        processMap.put(sealType,overallMap.get(sealType));
        canceledMap.add(sealType);
    }
    public static boolean isCanceled(String sealType){
        return canceledMap.contains(sealType);
    }

    public static String getCurrentSealType(){
        return currentSealType;
    }

    public static boolean isCurrentCompleted(){
        if(TextUtils.isEmpty(currentSealType))
            return true;
        if(!processMap.containsKey(currentSealType))
            return false;
        return overallMap.get(currentSealType).equals(processMap.get(currentSealType));
    }
}
