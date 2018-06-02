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
    public static void uploadSuccess(String method,String sealCode,String filePath,Integer position,String sealName,Integer uploadTimes){
        uploadTimes++;
        if(existInDb(filePath)){
            updateStatus(filePath,Constants.Uploaded,uploadTimes);
        }else {
            saveRecord(method,sealCode,filePath,position,sealName,Constants.Uploaded,uploadTimes);
        }
        File file=new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }

    public static void uploadFail(String method,String sealCode,String filePath,Integer position,String sealName,Integer uploadTimes){
        uploadTimes++;
        if(existInDb(filePath)){
            updateStatus(filePath,Constants.ToBeUpload,uploadTimes);
        }else{
            saveRecord(method,sealCode,filePath,position,sealName,Constants.ToBeUpload,uploadTimes);
        }
        File file=new File(filePath);
        if(file.exists()&&uploadTimes>Constants.MaxUploadTimes){
            file.delete();
        }
    }

    private static void saveRecord(String method, String sealCode, String filePath, Integer position, String sealName, Integer status,Integer uploadTimes){
        if(!method.contains("uploadBy")){
            return;
        }
        FileUploadRecord record=new FileUploadRecord();
        record.setSealCode(sealCode);
        record.setFilePath(filePath);
        record.setStatus(status);
        record.setPosition(position);
        record.setSealName(sealName);
        record.setUploadTimes(uploadTimes);
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

    private static void updateStatus(String filePath,Integer status,Integer uploadTimes){
        FileUploadRecord record=new FileUploadRecord();
        record.setStatus(status);
        record.setUploadTimes(uploadTimes);
        record.setTimeStamp(new Date());
        record.updateAll("filePath = ?",filePath);
    }

    public static void updateUploadedStatus(String filePath){
        FileUploadRecord record=new FileUploadRecord();
        record.setStatus(Constants.Uploaded);
        record.setTimeStamp(new Date());
        record.updateAll("filePath = ?",filePath);
    }

    public static List<FileUploadRecord> getTobeUploadFileList(){
        try {
            List<FileUploadRecord> list=DataSupport.where("status = 1 and uploadTimes <= "+Constants.MaxUploadTimes).find(FileUploadRecord.class);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
