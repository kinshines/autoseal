package com.inktech.autoseal.model;

/**
 * Created by Chaoyu on 2017/9/10.
 */

public class BluetoothCommandInterpreter {
    public static final String SendPrefix="EEB1";
    public static final String ReceivePrefix="EEB2";
    public static final String Suffix="FFFCFFFF";
    public static final String FeedbackReceivedCommand=ReceivePrefix+"0000"+Suffix;
    public static final String FeedbackSealOver=ReceivePrefix+"0100"+Suffix;

    public static String Send(String sealType,boolean isEnd){
        String command=SendPrefix;
        switch (sealType){
            case "gz":
                command+="00";
                break;
            case "frz":
                command+="01";
                break;
            case "cwz":
                command+="02";
                break;
            case "htz":
                command+="03";
                break;
            case "fpz":
                command+="04";
                break;
        }

        command+=isEnd?"00":"01";
        return command+Suffix;
    }

}
