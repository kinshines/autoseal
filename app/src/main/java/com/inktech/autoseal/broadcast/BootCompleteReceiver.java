package com.inktech.autoseal.broadcast;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import com.inktech.autoseal.constant.Constants;
import com.inktech.autoseal.ui.MainActivity;

public class BootCompleteReceiver extends BroadcastReceiver {

    static final String action_boot ="android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(action_boot)){

            //开机后一般会停留在锁屏页面且短时间内没有进行解锁操作屏幕会进入休眠状态，此时就需要先唤醒屏幕和解锁屏幕
            //屏幕唤醒
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.SCREEN_DIM_WAKE_LOCK, "BootCompleteReceiver");//最后的参数是LogCat里用的Tag
            wl.acquire();
            wl.release();

            //屏幕解锁
            KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("BootCompleteReceiver");//参数是LogCat里用的Tag
            kl.disableKeyguard();

            Intent mBootIntent = new Intent(context, MainActivity.class);
            mBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mBootIntent.setAction(Constants.ACTION_USING_SEAL);
            context.startActivity(mBootIntent);
        }
    }
}
