package com.inktech.autoseal.db;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class FileUploadRecord extends DataSupport {
    private String sealCode;
    private String filePath;
    private Integer position;
    private Integer sealType;
    private Integer status;
    private Date timeStamp;
    private String sealName;
    private Integer uploadTimes;

    public String getSealCode() {
        return sealCode;
    }

    public void setSealCode(String sealCode) {
        this.sealCode = sealCode;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getSealType() {
        return sealType;
    }

    public void setSealType(Integer sealType) {
        this.sealType = sealType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSealName() {
        return sealName;
    }

    public void setSealName(String sealName) {
        this.sealName = sealName;
    }

    public Integer getUploadTimes(){return uploadTimes;}

    public void setUploadTimes(Integer uploadTimes){this.uploadTimes=uploadTimes;}
}

