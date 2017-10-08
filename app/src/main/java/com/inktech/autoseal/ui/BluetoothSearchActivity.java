package com.inktech.autoseal.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inktech.autoseal.adapter.BluetoothDevicesAdapter;
import com.inktech.autoseal.constant.Constants;

import java.util.Set;

import com.inktech.autoseal.R;
import com.inktech.autoseal.util.ClsUtils;
import com.inktech.autoseal.util.PreferenceUtil;

public class BluetoothSearchActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;

    BluetoothDevicesAdapter bluetoothDevicesAdapter;

    Toolbar toolbar;
    ListView devicesListView;
    TextView emptyListTextView;
    ProgressBar toolbarProgressCircle;
    CoordinatorLayout coordinatorLayout;
    Button seachButton;
    String WebServiceMethod="";

    private void enableBluetooth() {
        setStatus("Enabling Bluetooth");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_search);

        Intent bluetoothIntent=getIntent();
        WebServiceMethod=bluetoothIntent.getStringExtra(Constants.web_service_method);

        initViews();
        setSupportActionBar(toolbar);

        setStatus("None");

        bluetoothDevicesAdapter = new BluetoothDevicesAdapter(this);

        devicesListView.setAdapter(bluetoothDevicesAdapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                setStatus("Asking to connect");
                final BluetoothDevice device = bluetoothDevicesAdapter.getItem(position);

                new AlertDialog.Builder(BluetoothSearchActivity.this)
                        .setCancelable(false)
                        .setTitle("Connect")
                        .setMessage("Do you want to connect to: " + device.getName() + " - " + device.getAddress())
                        .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                Log.d(Constants.TAG, "Opening new Activity");
                                bluetoothAdapter.cancelDiscovery();
                                toolbarProgressCircle.setVisibility(View.INVISIBLE);

                                Intent intent = new Intent(BluetoothSearchActivity.this, SealProcessActivity.class);

                                intent.putExtra(Constants.EXTRA_DEVICE, device);
                                intent.putExtra(Constants.web_service_method,WebServiceMethod);

                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                setStatus("Cancelled connection");
                                Log.d(Constants.TAG, "Cancelled ");
                            }
                        }).show();
            }
        });
        devicesListView.setEmptyView(emptyListTextView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(Constants.TAG, "Device has no bluetooth");
            new AlertDialog.Builder(BluetoothSearchActivity.this)
                    .setCancelable(false)
                    .setTitle("No Bluetooth")
                    .setMessage("Your device has no bluetooth")
                    .setPositiveButton("Close app", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            Log.d(Constants.TAG, "App closed");
                            finish();
                        }
                    }).show();
        }

        Set<BluetoothDevice> bondedDevices=bluetoothAdapter.getBondedDevices();

        if(bondedDevices.size()>0){
            for(BluetoothDevice device:bondedDevices){
                Intent intent = new Intent(BluetoothSearchActivity.this, SealProcessActivity.class);
                intent.putExtra(Constants.EXTRA_DEVICE, device);
                intent.putExtra(Constants.web_service_method,WebServiceMethod);
                startActivity(intent);
                finish();
                break;
            }
        }
    }

    private void initViews(){
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        devicesListView=(ListView)findViewById(R.id.devices_list_view);
        emptyListTextView=(TextView) findViewById(R.id.empty_list_item);
        toolbarProgressCircle=(ProgressBar) findViewById(R.id.toolbar_progress_bar);
        coordinatorLayout=(CoordinatorLayout) findViewById(R.id.coordinator_layout_main);
        seachButton=(Button) findViewById(R.id.search_button);
        seachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled()) {
                    // Bluetooth enabled
                    startSearching();
                } else {
                    enableBluetooth();
                }
            }
        });
    }

    @Override protected void onStart() {
        super.onStart();

        Log.d(Constants.TAG, "Registering receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override protected void onStop() {
        super.onStop();
        Log.d(Constants.TAG, "Receiver unregistered");
        unregisterReceiver(mReceiver);
    }


    private void setStatus(String status) {
        toolbar.setSubtitle(status);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startSearching();
            } else {
                setStatus("Error");
                Snackbar.make(coordinatorLayout, "Failed to enable bluetooth", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                enableBluetooth();
                            }
                        }).show();
            }
        }

    }

    private void startSearching() {
        if (bluetoothAdapter.startDiscovery()) {
            toolbarProgressCircle.setVisibility(View.VISIBLE);
            setStatus("Searching for devices");
        } else {
            setStatus("Error");
            Snackbar.make(coordinatorLayout, "Failed to start searching", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Try Again", new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            startSearching();
                        }
                    }).show();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.fetchUuidsWithSdp();

                if (bluetoothDevicesAdapter.getPosition(device) == -1) {
                    // -1 is returned when the item is not in the adapter
                    bluetoothDevicesAdapter.add(device);
                    bluetoothDevicesAdapter.notifyDataSetChanged();
                }

                if(device.getName().contains("HC-05")){
                    if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                        try {
                            //通过工具类ClsUtils,调用createBond方法
                            ClsUtils.createBond(device.getClass(), device);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                toolbarProgressCircle.setVisibility(View.INVISIBLE);
                setStatus("None");

            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Snackbar.make(coordinatorLayout, "Bluetooth turned off", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Turn on", new View.OnClickListener() {
                                    @Override public void onClick(View v) {
                                        enableBluetooth();
                                    }
                                }).show();
                        break;
                }
            } else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName().contains("HC-05")){
                    try {

                        //1.确认配对
                        ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                        //2.终止有序广播
                        Log.i("order...", "isOrderedBroadcast:"+isOrderedBroadcast()+",isInitialStickyBroadcast:"+isInitialStickyBroadcast());
                        abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                        //3.调用setPin方法进行配对...
                        boolean ret = ClsUtils.setPin(device.getClass(), device, PreferenceUtil.getBluetoothPairCode());
                        //ClsUtils.cancelPairingUserInput(btDevice.getClass(),btDevice);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    };
}
