package com.inktech.autoseal.model;

import java.util.UUID;

/**
 * Created by Chaoyu on 2017/9/10.
 */

public interface Constants {
    String TAG = "Arduino - Android";
    int REQUEST_ENABLE_BT = 1;
    int REQUEST_QR_SCAN=2;

    // message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_SNACKBAR = 4;

    int MESSAGE_FILE_UPLOAD_SUCCEED=5;
    int MESSAGE_FILE_UPLOAD_FAIL=6;

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_ERROR = 1;
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device


    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Key names received from the BluetoothChatService Handler
    String EXTRA_DEVICE  = "EXTRA_DEVICE";
    String SNACKBAR = "toast";

    String ACTION_NOTEBOOK="ACTION_NOTEBOOK";
    String ACTION_DAILY_ONE="ACTION_DAILY_ONE";
}
