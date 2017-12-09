package com.inktech.autoseal.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.db.FileUploadRecord;
import com.inktech.autoseal.model.OutSealInfoItemOffline;
import com.inktech.autoseal.model.OutSealInfoSyncResponse;
import com.inktech.autoseal.model.UploadFileResponse;
import com.inktech.autoseal.model.UsingSealInfoItemOffline;
import com.inktech.autoseal.model.UsingSealInfoSyncResponse;
import com.inktech.autoseal.util.DbUtil;
import com.inktech.autoseal.util.NetworkUtil;
import com.inktech.autoseal.util.SealOfflineUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.util.XmlParseUtil;

import java.util.ArrayList;
import java.util.List;

public class SyncService extends Service {
    private static final String TAG = "SyncService";
    private static final int SyncInterval=5*60*1000;
    public SyncService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: flags"+flags+",startId"+startId);
        syncUsingSealInfoIfNeed();
        syncOutSealInfoIfNeed();
        uploadLocalFile();
        AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime= SystemClock.elapsedRealtime()+SyncInterval;
        Intent i=new Intent(this,SyncService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void syncUsingSealInfoIfNeed(){
        //if(SealOfflineUtil.needUpdateUsingSeal()){
            syncUsingSealCode();
        //}
    }
    private void syncOutSealInfoIfNeed(){
        //if(SealOfflineUtil.needUpdateOutSeal()){
            syncOutSealCode();
        //}
    }

    private void syncUsingSealCode(){
        if(!NetworkUtil.isNetworkConnected())
            return;
        WebServiceUtil.updateUsingSealCode(new SoapCallbackListener() {
            @Override
            public void onFinish(String xml, String method, String sealCode, String filePath) {
                UsingSealInfoSyncResponse response= XmlParseUtil.pullUsingSealInfoSyncResponse(xml);
                if(response.getSealCount()>0){
                    ArrayList<UsingSealInfoItemOffline> list=response.getSealList();
                    SealOfflineUtil.saveOfflineUsingSealList(list);
                }
            }

            @Override
            public void onError(Exception e, String method, String sealCode, String filePath) {
                e.printStackTrace();
            }
        });
    }

    private void syncOutSealCode(){
        if(!NetworkUtil.isNetworkConnected())
            return;
        WebServiceUtil.updateOutSealCode(new SoapCallbackListener() {
            @Override
            public void onFinish(String xml, String method, String sealCode, String filePath) {
                OutSealInfoSyncResponse response= XmlParseUtil.pullOutSealInfoSyncResponse(xml);
                if(response.getSealCount()>0){
                    ArrayList<OutSealInfoItemOffline> list=response.getSealList();
                    SealOfflineUtil.saveOfflineOutSealList(list);
                }
            }

            @Override
            public void onError(Exception e, String method, String sealCode, String filePath) {
                e.printStackTrace();
            }
        });
    }

    private void uploadLocalFile() {
        if(!NetworkUtil.isNetworkConnected())
            return;
        List<FileUploadRecord> list=DbUtil.getTobeUploadFileList();
        if(list.size()==0)
            return;
        for (FileUploadRecord record:list){
            try {
                Thread.sleep(2*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebServiceUtil.uploadByRecord(record, new SoapCallbackListener() {
                @Override
                public void onFinish(String xml, String method, String sealCode, String filePath) {
                    UploadFileResponse response=XmlParseUtil.pullUploadFileResponse(xml);
                    if(response.getStatus()==1){
                        DbUtil.uploadSuccess(method,sealCode,filePath,record.getPosition(),record.getSealName());
                    }else{
                        DbUtil.uploadFail(method,sealCode,filePath,record.getPosition(),record.getSealName());
                    }
                }

                @Override
                public void onError(Exception e, String method, String sealCode, String filePath) {
                    e.printStackTrace();
                    DbUtil.uploadFail(method,sealCode,filePath,record.getPosition(),record.getSealName());
                }
            });

        }
    }
}
