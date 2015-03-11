package com.example.abadie.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements IBluetoothManagerable{
    // Helper
    BluetoothManager mBtManager;
    // GUI
    private ListView mListBt;
    private ArrayAdapter<String> mAdapter;
    private ProgressDialog mProgessDialog;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface IBluetoothManagerable
    public void willStartFindingModule(){
        mAdapter.clear();
    }

    public void didFoundBluetoothObject(BluetoothDevice device){
        mAdapter.add(device.getAddress() + " - " + device.getName() + "\n");
        mAdapter.notifyDataSetChanged();
    }

    public void didFindingFoundBluetoothObject(){
        this.undisplayWait();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LifeCycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        mBtManager = BluetoothManager.getInstance();
        mBtManager.setManagearable(this);
        this.setupBTList();
    }

    @Override
    public void onDestroy(){
        mBtManager.onDestroy();
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
        this.displayWait();
        mBtManager.findBTModule();
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
    private void setupBTList(){
        mListBt  = (ListView) findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        mListBt.setAdapter(mAdapter);

        mListBt.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                BluetoothDevice btDevice = mBtManager.getBluetoothDeviceFromDiscoveryList(arg2);

                if(btDevice != null){
                   BluetoothManager btManager =   MainActivity.this.mBtManager;
                   if (btDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                         btManager.unpairDevice(btDevice);
                     } else {
                         btManager.pairDevice(btDevice);
                    }
                }
            }
        });
    }
}