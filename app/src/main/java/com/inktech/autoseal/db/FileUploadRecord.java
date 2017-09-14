package com.inktech.autoseal.db;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class FileUploadRecord extends DataSupport {
    private String sealCode;
    private String filePath;
    private PhotoPosition position;
    private SealType sealType;
    private UploadStatus status;
    private Date timeStamp;

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

    public PhotoPosition getPosition() {
        return position;
    }

    public void setPosition(PhotoPosition position) {
        this.position = position;
    }

    public SealType getSealType() {
        return sealType;
    }

    public void setSealType(SealType sealType) {
        this.sealType = sealType;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}

