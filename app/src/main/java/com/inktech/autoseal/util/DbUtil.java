package com.inktech.autoseal.util;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.db.FileUploadRecord;
import com.inktech.autoseal.db.PhotoPosition;
import com.inktech.autoseal.db.SealType;
import com.inktech.autoseal.db.UploadStatus;

import org.litepal.crud.DataSupport;

import java.io.File;
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
        if(WebServiceUtil.UploadByUsing.equals(method)){
            record.setSealType(SealType.UploadByUsing);
        }else if(WebServiceUtil.UploadByOut.equals(method)){
            record.setSealType(SealType.UploadByOut);
        }else if(WebServiceUtil.UploadByUrgentUsing.equals(method)){
            record.setSealType(SealType.UploadByUrgentUsing);
        }else if(WebServiceUtil.UploadByUrgentOut.equals(method)){
            record.setSealType(SealType.UploadByUrgentOut);
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
        List<FileUploadRecord> list=DataSupport.where("status = 0 ").find(FileUploadRecord.class);
        return list;
    }
}
