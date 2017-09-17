package com.inktech.autoseal.util;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.db.FileUploadRecord;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/15.
 */

public class DbUtil {
    public static void uploadSuccess(String method,String sealCode,String filePath,Integer position){
        if(existInDb(filePath)){
            updateStatus(filePath,Constants.Uploaded);
        }else {
            saveRecord(method,sealCode,filePath,position,Constants.Uploaded);
        }
        File file=new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }
    public static void uploadFail(String method,String sealCode,String filePath,Integer position){
        if(existInDb(filePath)){
            updateStatus(filePath,Constants.ToBeUpload);
        }else{
            saveRecord(method,sealCode,filePath,position,Constants.ToBeUpload);
        }
    }

    private static void saveRecord(String method, String sealCode, String filePath, Integer position, Integer status){
        if(!method.contains("uploadBy")){
            return;
        }
        FileUploadRecord record=new FileUploadRecord();
        record.setSealCode(sealCode);
        record.setFilePath(filePath);
        record.setStatus(status);
        record.setPosition(position);
        if(WebServiceUtil.uploadByUsing.equals(method)){
            record.setSealType(Constants.uploadByUsing);
        }else if(WebServiceUtil.uploadByOut.equals(method)){
            record.setSealType(Constants.uploadByOut);
        }else if(WebServiceUtil.uploadByUrgentUsing.equals(method)){
            record.setSealType(Constants.uploadByUrgentUsing);
        }else if(WebServiceUtil.uploadByUrgentOut.equals(method)){
            record.setSealType(Constants.uploadByUrgentOut);
        }
        record.setTimeStamp(new Date());
        record.save();
    }

    private static boolean existInDb(String filePath){
        List<FileUploadRecord> records= DataSupport.where("filePath = ?",filePath).find(FileUploadRecord.class);
        return records.size()>0;
    }
    private static void updateStatus(String filePath,Integer status){
        FileUploadRecord record=new FileUploadRecord();
        record.setStatus(status);
        record.setTimeStamp(new Date());
        record.updateAll("filePath = ?",filePath);
    }

    public static List<FileUploadRecord> getTobeUploadFileList(){
        try {
            List<FileUploadRecord> list=DataSupport.where("status = 1 ").find(FileUploadRecord.class);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<FileUploadRecord>();
        }
    }
}
