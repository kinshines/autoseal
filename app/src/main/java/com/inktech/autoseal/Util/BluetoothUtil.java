package com.inktech.autoseal.Util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * Created by Chaoyu on 2017/8/11.
 */

public class BluetoothUtil {
    public static final int CONNECT_FAILED = 0;
    public static final int CONNECT_SUCCESS = 1;
    public static final int WRITE_FAILED = 2;
    public static final int READ_FAILED = 3;
    public static final int DATA = 4;


    private BluetoothDevice device;
    private Handler handler;
    private static BluetoothSocket socket;

    public BluetoothUtil(BluetoothDevice device, Handler handler) {
        this.device = device;
        this.handler = handler;
    }

    public void connect() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                BluetoothSocket tmp = null;
                Method method;
                try {
                    method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                } catch (Exception e) {
                    Log.e("CreateSocket", e.toString());
                }
                socket = tmp;
                try {
                    socket.connect();
                } catch (Exception e) {
                    setState(CONNECT_FAILED);
                    Log.e("Connect", e.toString());
                }
                setState(CONNECT_SUCCESS);
            }
        });
        thread.start();
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("Close", e.toString());
        }
    }

    private void setState(int state) {
        Message msg = handler.obtainMessage();
        msg.what = state;
        handler.sendMessage(msg);
    }
}
