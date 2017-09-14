package com.inktech.autoseal.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Chaoyu on 2017/9/15.
 */

public class DateUtil {
    public static String getShortDate(){
        Date now=new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dataString=formatter.format(now);
        return dataString;
    }
}
