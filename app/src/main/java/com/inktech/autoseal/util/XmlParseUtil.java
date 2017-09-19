package com.inktech.autoseal.util;

import com.inktech.autoseal.model.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

/**
 * Created by Chaoyu on 2017/9/12.
 */

public class XmlParseUtil {
    public static UsingSealInfoResponse pullUsingSealInfoResponse(String xmlData){
        UsingSealInfoResponse sealInfoResult=null;
        UsingSealInfoItem sealInfo=null;
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        sealInfoResult=new UsingSealInfoResponse();
                        break;
                    case XmlPullParser.START_TAG:
                        if("sealCount".equals(nodeName)){
                            sealInfoResult.setSealCount(Integer.parseInt(xmlPullParser.nextText()));
                        }else if("seal".equals(nodeName)){
                            sealInfo=new UsingSealInfoItem();
                        }else if("type".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setType(type);
                        }else if("sealName".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setSealName(type);
                        }else if("count".equals(nodeName)){
                            String count=xmlPullParser.nextText();
                            sealInfo.setCount(Integer.parseInt(count));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if("seal".equals(nodeName)){
                            sealInfoResult.addSealToList(sealInfo);
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

    public static UploadFileResponse pullUploadFileResponse(String xmlData){
        UploadFileResponse fileResponse=null;
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        fileResponse=new UploadFileResponse();
                        break;
                    case XmlPullParser.START_TAG:
                        if("status".equals(nodeName)){
                            fileResponse.setStatus(Integer.parseInt(xmlPullParser.nextText()));
                        }
                        else if("message".equals(nodeName)){
                            fileResponse.setMessage(xmlPullParser.nextText());
                        }
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return fileResponse;
    }

    public static UsingSealInfoSyncResponse pullUsingSealInfoSyncResponse(String xmlData){
        UsingSealInfoSyncResponse sealInfoResult=null;
        UsingSealInfoItemOffline sealInfo=null;
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        sealInfoResult=new UsingSealInfoSyncResponse();
                        break;
                    case XmlPullParser.START_TAG:
                        if("sealCount".equals(nodeName)){
                            sealInfoResult.setSealCount(Integer.parseInt(xmlPullParser.nextText()));
                        }else if("seal".equals(nodeName)){
                            sealInfo=new UsingSealInfoItemOffline();
                        }else if("type".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setType(type);
                        }else if("sealName".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setSealName(type);
                        }else if("count".equals(nodeName)){
                            String count=xmlPullParser.nextText();
                            sealInfo.setCount(Integer.parseInt(count));
                        }else if("sealCode".equals(nodeName)){
                            String sealCode=xmlPullParser.nextText();
                            sealInfo.setSealCode(sealCode);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if("seal".equals(nodeName)){
                            sealInfoResult.addSealToList(sealInfo);
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

    public static OutSealInfoResponse pullOutSealInfoResponse(String xmlData){
        OutSealInfoResponse sealInfoResult=null;
        OutSealInfoItem sealInfo=null;
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        sealInfoResult=new OutSealInfoResponse();
                        break;
                    case XmlPullParser.START_TAG:
                        if("sealCount".equals(nodeName)){
                            sealInfoResult.setSealCount(Integer.parseInt(xmlPullParser.nextText()));
                        }else if("seal".equals(nodeName)){
                            sealInfo=new OutSealInfoItem();
                        }else if("type".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setType(type);
                        }else if("sealName".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setSealName(type);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if("seal".equals(nodeName)){
                            sealInfoResult.addSealToList(sealInfo);
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

    public static OutSealInfoSyncResponse pullOutSealInfoSyncResponse(String xmlData){
        OutSealInfoSyncResponse sealInfoResult=null;
        OutSealInfoItemOffline sealInfo=null;
        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType=xmlPullParser.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                String nodeName=xmlPullParser.getName();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        sealInfoResult=new OutSealInfoSyncResponse();
                        break;
                    case XmlPullParser.START_TAG:
                        if("sealCount".equals(nodeName)){
                            sealInfoResult.setSealCount(Integer.parseInt(xmlPullParser.nextText()));
                        }else if("seal".equals(nodeName)){
                            sealInfo=new OutSealInfoItemOffline();
                        }else if("type".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setType(type);
                        }else if("sealName".equals(nodeName)){
                            String type=xmlPullParser.nextText();
                            sealInfo.setSealName(type);
                        }else if("sealCode".equals(nodeName)){
                            String sealCode=xmlPullParser.nextText();
                            sealInfo.setSealCode(sealCode);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if("seal".equals(nodeName)){
                            sealInfoResult.addSealToList(sealInfo);
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
