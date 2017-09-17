package com.inktech.autoseal.model;

import java.util.ArrayList;

/**
 * Created by Chaoyu on 2017/9/17.
 */

public class OutSealSummary {
    private static ArrayList<String> overall=new ArrayList<>();
    private static String currentSealType="";
    private static String currentSealCode="";

    private static void addSealType(String sealType){
        overall.add(sealType);
    }

    public static int completeOnce(){
        overall.remove(0);
        return overall.size();
    }

    public static String getCurrentSealType(){
        if(overall.size()>0){
            currentSealType=overall.get(0);
            return currentSealType;
        }
        return "";
    }

    public static String getSealTypeChinese(){
        return UsingSealSummary.translateSealTypeToChinese(currentSealType);
    }

    private static void init(){
        overall.clear();
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
        String chineseType= UsingSealSummary.translateSealTypeToChinese(type);
        addSealType(type);
        return chineseType;
    }
}
