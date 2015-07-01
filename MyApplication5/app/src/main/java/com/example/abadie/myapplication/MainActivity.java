package com.example.abadie.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.johnpersano.supertoasts.SuperToast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity{
    // Helper
    BluetoothManager mBtManager;
    // GUI
    private ListView mListBt;
    private SimpleImageAdapter mAdapter;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LifeCycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupBTManager();
        this.setupBTList(mBtManager);
        mAdapter.redisplayList();
    }

    @Override
    public void onDestroy(){
        this.unsetBTManager();
        super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GUI
    private void displayWait(){
        SuperToast.create(this, "\"Détection de modules Bluetooth", SuperToast.Duration.EXTRA_LONG).show();
    }

    private void undisplayWait(String message){
       //fh
       // this
       // SuperToast.create(this, message, SuperToast.Duration.VERY_SHORT).show();
    }

    // GUI Action button
    public void onButtonBluetoothScanTapped(View v){
        mAdapter.redisplayList();
        mBtManager.findBTModule(this);
    }

    public void onTestTapped(View v){
        this.navigateToPixelMatrixManagerActivity();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LOGIC
    private void navigateToPixelMatrixManagerActivity(){
        Intent myIntent = new Intent(MainActivity.this, PixelMatrixManagerActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SETUP
    private void setupBTList(BluetoothManager btManager){
        mListBt  = (ListView) findViewById(R.id.listView);

        mAdapter = new SimpleImageAdapter(this);
        mAdapter.BTManager = btManager;

        mListBt.setAdapter(mAdapter);
        mListBt.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                BluetoothDevice btDevice = mBtManager.getBluetoothDeviceFromDiscoveryList(arg2);

                if (btDevice != null) {
                    BluetoothManager btManager = MainActivity.this.mBtManager;
                    int bondState = btDevice.getBondState();

                    if (bondState == BluetoothDevice.BOND_BONDED ||
                        bondState == BluetoothDevice.BOND_BONDING) {
                            btManager.unpairDevice(btDevice);
                    } else {
                        btManager.pairDevice(btDevice);
                    }
                }else {
                    Log.d("############ state failed", "");
                }
            }
        });
    }

    private void setupBTManager(){
        mBtManager = BluetoothManager.getInstance(this.getApplicationContext());
        mBtManager.registerToStreamBtEvent(new BluetoothStreamReceiver() {
            @Override
            public void onStreamReceive(String outputStream) {
            }

            public void bluetoothDidFailedConnect(String output){
                SuperToast.create(MainActivity.this, "\"bluetoothDidFailedConnect", SuperToast.Duration.EXTRA_LONG).show();
            }

            public void bluetoothDidSucceedConnect(String output){
                SuperToast.create(MainActivity.this, "\"bluetoothDidSucceedConnect", SuperToast.Duration.EXTRA_LONG).show();
            }
        });

        mBtManager.registerToBluetoothEvent(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(BTACTION.ACTION_DISCOVERY_STARTED.toString())) {
                    MainActivity.this.displayWait();
                }

                else if (action.equals(BTACTION.ACTION_FOUND.toString())) {
                    // BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                }

                else if (action.equals(BTACTION.ACTION_DISCOVERY_FINISHED.toString())) {
                    MainActivity.this.undisplayWait("Détection de modules bluetooth terminée");
                }

                else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                }

                mAdapter.redisplayList();
            }
        });
    }

    private void unsetBTManager(){
        mBtManager.unregisterToBluetoothEvent();
    }
}