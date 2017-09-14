package com.inktech.autoseal.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.inktech.autoseal.adapter.SoapCallbackListener;
import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.db.FileUploadRecord;
import com.inktech.autoseal.model.UploadFileResponse;
import com.inktech.autoseal.model.UsingSealInfoItemOffline;
import com.inktech.autoseal.model.UsingSealInfoSyncResponse;
import com.inktech.autoseal.util.DateUtil;
import com.inktech.autoseal.util.DbUtil;
import com.inktech.autoseal.util.ListDataSaveUtil;
import com.inktech.autoseal.util.WebServiceUtil;
import com.inktech.autoseal.util.XmlParseUtil;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.Date;
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
        AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime= SystemClock.elapsedRealtime()+SyncInterval;
        Intent i=new Intent(this,SyncService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateUsingSealInfoIfNeed(){
        String tag=Constants.OfflineUsingSealCode+ DateUtil.getShortDate();
        List<UsingSealInfoItemOffline> list=ListDataSaveUtil.getDataList(tag);

        if(list.size()==0||!hasAllCode(list)){
            updateUsingSealCode();
            return;
        }
    }

    private boolean hasCodeForSealType(String sealType,List<UsingSealInfoItemOffline> list){
        boolean hasCode=false;
        for(UsingSealInfoItemOffline item:list){
            if(sealType.equals(item.getType())){
                hasCode=true;
                break;
            }
        }
        return hasCode;
    }

    private boolean hasAllCode(List<UsingSealInfoItemOffline> list){
        return hasCodeForSealType(Constants.gz,list)
                &&hasCodeForSealType(Constants.frz,list)
                &&hasCodeForSealType(Constants.cwz,list)
                &&hasCodeForSealType(Constants.htz,list)
                &&hasCodeForSealType(Constants.fpz,list);
    }

    private void updateUsingSealCode(){
        WebServiceUtil.updateUsingSealCode(new SoapCallbackListener() {
            @Override
            public void onFinish(String xml, String method, String sealCode, String filePath, String position) {
                UsingSealInfoSyncResponse response= XmlParseUtil.pullUsingSealInfoSyncResponse(xml);
                if(response.getSealCount()>0){
                    List<UsingSealInfoItemOffline> list=response.getSealList();
                    String tag=Constants.OfflineUsingSealCode+ DateUtil.getShortDate();
                    ListDataSaveUtil.setDataList(tag,list);
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
