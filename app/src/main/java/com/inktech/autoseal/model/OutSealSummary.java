package com.inktech.autoseal.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Chaoyu on 2017/9/17.
 */

public class OutSealSummary {
    private static ArrayList<String> overallMap=new ArrayList<>();
    private static ArrayList<String> processMap=new ArrayList<>();
    private static HashMap<String,String> sealNameMap=new HashMap<>();
    private static ArrayList<String> canceledMap=new ArrayList<>();
    private static String currentSealType="";
    private static String currentSealCode="";

    private static void addSealType(String sealType){
        overallMap.add(sealType);
    }

    public static void completeOnce(){
        processMap.add(currentSealType);
    }

    public static boolean sealHasTakenOut(String sealType){
        return processMap.contains(sealType);
    }

    public static String getSealTypeChinese(String sealType){
        return sealNameMap.get(sealType);
    }

    private static void init(){
        overallMap.clear();
        processMap.clear();
        canceledMap.clear();
    }
    public static void setCurrentSealType(String sealType){
        currentSealType=sealType;
    }
    public static String getCurrentSealCode(){
        return currentSealCode;
    }
    public static void setCurrentSealCode(String code){
        currentSealCode=code;
        init();
    }

    public static String translateOutSealItemToChinese(OutSealInfoItem seal){
        String type=seal.getType();
        String chineseType= seal.getSealName();
        addSealType(type);
        sealNameMap.put(type,chineseType);
        return chineseType;
    }

    public static ArrayList<String> getOverallMap(){
        return overallMap;
    }

    public static boolean isAllCompleted(){
        for(String key:overallMap){
            if(!processMap.contains(key)){
                return false;
            }
        }
        return true;
    }

    public static void cancelSeal(String sealType){
        processMap.add(sealType);
        canceledMap.add(sealType);
    }
    public static boolean isCanceled(String sealType){
        return canceledMap.contains(sealType);
    }
}

