package com.inktech.autoseal.util;

import com.inktech.autoseal.model.SealInfo;
import com.inktech.autoseal.model.SealInfoResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

/**
 * Created by Chaoyu on 2017/9/12.
 */

public class XmlParseUtil {
    public static SealInfoResult parseXML2SealInfoResult(String xmlData){
        SealInfoResult sealInfoResult=null;
        SealInfo sealInfo=null;
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        sealInfoResult=new SealInfoResult();
                        break;
                    case XmlPullParser.START_TAG:
                        if("sealCount".equals(nodeName)){
                            sealInfoResult.setSealCount(Integer.parseInt(xmlPullParser.nextText()));
                        }else if("seal".equals(nodeName)){
                            sealInfo=new SealInfo();
                        }else if("type".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setType(type);
                        }else if("count".equals(nodeName)){
                            String count=xmlPullParser.nextText();
                            sealInfo.setCount(Integer.parseInt(count));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if("seal".equals(nodeName)){
                            sealInfoResult.addSealList(sealInfo);
                            sealInfo=null;
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return sealInfoResult;
    }
}
