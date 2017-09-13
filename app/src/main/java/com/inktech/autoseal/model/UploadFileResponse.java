package com.inktech.autoseal.model;

/**
 * Created by Chaoyu on 2017/9/14.
 */

public class UploadFileResponse {
    private Integer status;
    private String message;

    public UploadFileResponse(){
        status=0;
        message="";
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
