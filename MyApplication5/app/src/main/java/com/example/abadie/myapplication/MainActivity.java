package com.example.abadie.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity{
    // Helper
    BluetoothManager mBtManager;
    // GUI
    private ListView mListBt;
    private SimpleImageAdapter mAdapter;
    private ProgressDialog mProgessDialog;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LifeCycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupBTList();
        this.setupBTManager();
    }

    @Override
    public void onDestroy(){
        this.unsetBTManager();
        super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GUI
    private void displayWait(){
        mProgessDialog = ProgressDialog.show(this, "", "Please Wait", false);
    }

    private void undisplayWait(){
        mProgessDialog.dismiss();
    }

    // GUI Action button
    public void onButtonBluetoothScanTapped(View v){
        mBtManager.findBTModule();
    }

    public void onTestTapped(View v){
        this.navigateToPixelMatrixManagerActivity();
    }

    private void displayBTDevice(ArrayList<BluetoothDevice> list){
        for (BluetoothDevice device : list){
            this.didFoundBluetoothObject(device);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LOGIC
    private void navigateToPixelMatrixManagerActivity(){
        Intent myIntent = new Intent(MainActivity.this, PixelMatrixManagerActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    private void didFoundBluetoothObject(BluetoothDevice device){
        mAdapter.add(device);
    }

    private void clearList(){
        mAdapter.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SETUP
    private void setupBTList(){
        mListBt  = (ListView) findViewById(R.id.listView);

        mAdapter = new SimpleImageAdapter(this);
        mListBt.setAdapter(mAdapter);

        mListBt.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                BluetoothDevice btDevice = mBtManager.getBluetoothDeviceFromDiscoveryList(arg2);

                if (btDevice != null) {
                    BluetoothManager btManager = MainActivity.this.mBtManager;
                    if (btDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        btManager.unpairDevice(btDevice);
                    } else {
                        btManager.pairDevice(btDevice);
                    }
                }
            }
        });
    }

    private void setupBTManager(){
        mBtManager = BluetoothManager.getInstance(this.getApplicationContext());
        mBtManager.registerToBluetoothEvent(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(BTACTION.ACTION_DISCOVERY_STARTED.toString())) {
                    MainActivity.this.clearList();
                    MainActivity.this.displayWait();
                }

                else if (action.equals(BTACTION.ACTION_FOUND.toString())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    MainActivity.this.didFoundBluetoothObject(device);
                }

                else if (action.equals(BTACTION.ACTION_DISCOVERY_FINISHED.toString())) {
                    MainActivity.this.undisplayWait();
                }

                else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    mAdapter.deviceStateChanged(device, state);
                }
        }
        });
    }

    private void unsetBTManager(){
        mBtManager.unregisterToBluetoothEvent();
    }
}