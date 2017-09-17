package com.inktech.autoseal.util;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.db.FileUploadRecord;
import com.inktech.autoseal.db.PhotoPosition;
import com.inktech.autoseal.db.SealType;
import com.inktech.autoseal.db.UploadStatus;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Chaoyu on 2017/9/15.
 */

public class DbUtil {
    public static void uploadSuccess(String method,String sealCode,String filePath,String position){
        if(existInDb(filePath)){
            updateStatus(filePath,UploadStatus.Uploaded);
        }else {
            saveRecord(method,sealCode,filePath,position,UploadStatus.Uploaded);
        }
        File file=new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }
    public static void uploadFail(String method,String sealCode,String filePath,String position){
        if(existInDb(filePath)){
            updateStatus(filePath,UploadStatus.ToBeUpload);
        }else{
            saveRecord(method,sealCode,filePath,position,UploadStatus.ToBeUpload);
        }
    }

    private static void saveRecord(String method, String sealCode, String filePath, String position, UploadStatus status){
        if(!method.contains("uploadBy")){
            return;
        }
        FileUploadRecord record=new FileUploadRecord();
        record.setSealCode(sealCode);
        record.setFilePath(filePath);
        record.setStatus(status);
        if(Constants.User.equals(position)){
            record.setPosition(PhotoPosition.User);
        }else if(Constants.Documents.equals(position)){
            record.setPosition(PhotoPosition.Documents);
        }
        if(WebServiceUtil.uploadByUsing.equals(method)){
            record.setSealType(SealType.uploadByUsing);
        }else if(WebServiceUtil.uploadByOut.equals(method)){
            record.setSealType(SealType.uploadByOut);
        }else if(WebServiceUtil.uploadByUrgentUsing.equals(method)){
            record.setSealType(SealType.uploadByUrgentUsing);
        }else if(WebServiceUtil.uploadByUrgentOut.equals(method)){
            record.setSealType(SealType.uploadByUrgentOut);
        }
        record.setTimeStamp(new Date());
        record.save();
    }

    private static boolean existInDb(String filePath){
        List<FileUploadRecord> records= DataSupport.where("filePath = ?",filePath).find(FileUploadRecord.class);
        return records.size()>0;
    }
    private static void updateStatus(String filePath,UploadStatus status){
        FileUploadRecord record=new FileUploadRecord();
        record.setStatus(status);
        record.setTimeStamp(new Date());
        record.updateAll("filePath = ?",filePath);
    }

    public static List<FileUploadRecord> getTobeUploadFileList(){
        try {
            List<FileUploadRecord> list=DataSupport.where("status = 0 ").find(FileUploadRecord.class);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<FileUploadRecord>();
        }
    }
}
