package com.example.abadie.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends ActionBarActivity {
    private ListView mListBt;
    ArrayAdapter<String> mAdapter;
    int REQUEST_ENABLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupBTList();
        this.findBTModule();
    }

    private void setupBTList(){
        mListBt  = (ListView) findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        mListBt.setAdapter(mAdapter);
    }

    //region LOGIC
    private void findBTModule(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()){
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
        }else
            this.startBTDiscovery();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        if(resultCode == REQUEST_ENABLE_BT){

        }
        /*
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

         */
    }

    private void startBTDiscovery(){
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    mAdapter.add(device.getAddress() + " - " + device.getName() + "\n");
                    mAdapter.notifyDataSetChanged();
                    mBluetoothAdapter.cancelDiscovery();
                }
            }
        };

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        mBluetoothAdapter.startDiscovery();
    }
}