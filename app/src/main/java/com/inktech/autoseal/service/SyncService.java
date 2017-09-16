package com.inktech.autoseal.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.db.FileUploadRecord;
import com.inktech.autoseal.model.UploadFileResponse;
import com.inktech.autoseal.model.UsingSealInfoItemOffline;
import com.inktech.autoseal.model.UsingSealInfoSyncResponse;
import com.inktech.autoseal.util.DbUtil;
import com.inktech.autoseal.util.SealOfflineUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.util.XmlParseUtil;

import java.util.ArrayList;
import java.util.List;

public class SyncService extends Service {
    private static final String TAG = "SyncService";
    private static final int SyncInterval=60*60*1000;
    public SyncService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateUsingSealInfoIfNeed();
        uploadLocalFile();
        AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime= SystemClock.elapsedRealtime()+SyncInterval;
        Intent i=new Intent(this,SyncService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateUsingSealInfoIfNeed(){
        if(SealOfflineUtil.needUpdateUsingSeal()){
            updateUsingSealCode();
        }
    }

    private void updateUsingSealCode(){
        WebServiceUtil.updateUsingSealCode(new SoapCallbackListener() {
            @Override
            public void onFinish(String xml, String method, String sealCode, String filePath, String position) {
                UsingSealInfoSyncResponse response= XmlParseUtil.pullUsingSealInfoSyncResponse(xml);
                if(response.getSealCount()>0){
                    ArrayList<UsingSealInfoItemOffline> list=response.getSealList();
                    SealOfflineUtil.saveOfflineUsingSealList(list);
                }
            }

            @Override
            public void onError(Exception e, String method, String sealCode, String filePath, String position) {
                e.printStackTrace();
            }
        });
    }

    private void uploadLocalFile(){
        List<FileUploadRecord> list=DbUtil.getTobeUploadFileList();
        if(list.size()==0)
            return;
        for (FileUploadRecord record:list){
            WebServiceUtil.uploadByRecord(record, new SoapCallbackListener() {
                @Override
                public void onFinish(String xml, String method, String sealCode, String filePath, String position) {
                    UploadFileResponse response=XmlParseUtil.pullUploadFileResponse(xml);
                    if(response.getStatus()==1){
                        DbUtil.uploadSuccess(method,sealCode,filePath,position);
                    }else{
                        DbUtil.uploadFail(method,sealCode,filePath,position);
                    }
                }

                @Override
                public void onError(Exception e, String method, String sealCode, String filePath, String position) {
                    e.printStackTrace();
                    DbUtil.uploadFail(method,sealCode,filePath,position);
                }
            });
        }
    }
}
