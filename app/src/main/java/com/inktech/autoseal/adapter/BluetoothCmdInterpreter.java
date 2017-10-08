package com.inktech.autoseal.adapter;

import com.inktech.autoseal.constant.Constants;

/**
 * Created by Chaoyu on 2017/9/10.
 */

public class BluetoothCmdInterpreter {
    private static final String UsingSendPrefix ="EEB1";
    private static final String UsingReceivePrefix ="EEB2";
    public static final String Suffix="FFFCFFFF";
    public static final String UsingFeedbackReceivedCmd = UsingReceivePrefix +"0000"+Suffix;
    public static final String UsingFeedbackSealOver = UsingReceivePrefix +"0100"+Suffix;

    private static final String OutSendPrefix="EEB3";
    private static final String OutReceivePrefix="EEB4";
    public static final String OutFeedbackReceivedCmd = OutReceivePrefix +"0000"+Suffix;
    public static final String OutFeedbackSealOver = OutReceivePrefix +"0100"+Suffix;

    private static final String ReturnSendPrefix="EEB5";
    private static final String ReturnReceivePrefix="EEB6";
    public static final String ReturnFeedbackSealOver = ReturnReceivePrefix +"0100"+Suffix;

    public static String usingSend(String sealType, boolean isEnd){
        String command= UsingSendPrefix;
        switch (sealType){
            case Constants.gz:
                command+="00";
                break;
            case Constants.frz:
                command+="01";
                break;
            case Constants.cwz:
                command+="02";
                break;
            case Constants.htz:
                command+="03";
                break;
            case Constants.fpz:
                command+="04";
                break;
        }
        command+=isEnd?"00":"01";
        return command+Suffix;
    }

    public static String cancelUsingSend(String sealType){
        String command= UsingSendPrefix;
        switch (sealType){
            case Constants.gz:
                command+="00";
                break;
            case Constants.frz:
                command+="01";
                break;
            case Constants.cwz:
                command+="02";
                break;
            case Constants.htz:
                command+="03";
                break;
            case Constants.fpz:
                command+="04";
                break;
        }
        command+="02";
        return command+Suffix;
    }

    public static String outSend(String sealType){
        String command= OutSendPrefix;
        switch (sealType){
            case Constants.gz:
                command+="00";
                break;
            case Constants.frz:
                command+="01";
                break;
            case Constants.cwz:
                command+="02";
                break;
            case Constants.htz:
                command+="03";
                break;
            case Constants.fpz:
                command+="04";
                break;
        }

        command+="00";
        return command+Suffix;
    }

    public static String returnSend(String sealType,boolean isConfirmed){
        String command= ReturnSendPrefix;
        switch (sealType){
            case Constants.gz:
                command+="00";
                break;
            case Constants.frz:
                command+="01";
                break;
            case Constants.cwz:
                command+="02";
                break;
            case Constants.htz:
                command+="03";
                break;
            case Constants.fpz:
                command+="04";
                break;
        }

        command+=isConfirmed?"01":"00";
        return command+Suffix;
    }

    public static byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }

    public static String bytesToHexString(byte[] bytes,int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

}
