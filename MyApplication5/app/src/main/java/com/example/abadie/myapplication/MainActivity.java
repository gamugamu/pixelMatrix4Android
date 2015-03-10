package com.example.abadie.myapplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {
    // Helper
    private BluetoothConnection btSocketManager;
    private ArrayList<BluetoothDevice> listDevice = new ArrayList<BluetoothDevice>();

    // GUI
    private ListView mListBt;
    private ArrayAdapter<String> mAdapter;
    private ProgressDialog mProgessDialog;
    int REQUEST_ENABLE_BT = 1;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LifeCycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupBTList();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);

        super.onDestroy();
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
        this.findBTModule();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LOGIC
    private void findBTModule(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()){
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
        }else{
            this.displayWait();
            this.startBTDiscovery();
            this.registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        }
    }

    private void startBTDiscovery(){
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    listDevice.add(device);
                    mAdapter.add(device.getAddress() + " - " + device.getName() + "\n");
                    mAdapter.notifyDataSetChanged();
                }

                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    mBluetoothAdapter.cancelDiscovery();
                    MainActivity.this.undisplayWait();
                }
            }
        };

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        mBluetoothAdapter.startDiscovery();
    }


    private void scanBt(BluetoothAdapter mBluetoothAdapter){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.d("############", "SCAN " + pairedDevices.size() );

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                Log.d("############", "Items " + pairedDevices);
            }
        }

    }

    private void makePairingDevice(BluetoothDevice device){
        try {
            device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
            device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        this.scanBt(BluetoothAdapter.getDefaultAdapter());
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            btSocketManager.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                Log.d("############", "module " + MainActivity.this.listDevice.get(arg2));
                BluetoothDevice btDevice            = MainActivity.this.listDevice.get(arg2);
                BluetoothAdapter mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();

                if (btDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(btDevice);
                } else {
                    MainActivity.this.pairDevice(btDevice);
                }
            }
        });
    }



    private void setUpBtManagerAndlistenSPPBluetooth(BluetoothDevice device, BluetoothAdapter adapter){
        btSocketManager = new BluetoothConnection(device, adapter);
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                Log.d("############", "STATE " + state + " " + prevState);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    // showToast("Paired");
                    Log.d("############", "PAIRED ");
                    BluetoothDevice btDevice        = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothAdapter btAdaptater     = BluetoothAdapter.getDefaultAdapter();

                    MainActivity.this.scanBt(btAdaptater);
                    MainActivity.this.setUpBtManagerAndlistenSPPBluetooth(btDevice, btAdaptater);

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    // showToast("Unpaired");
                    Log.d("############", "UNPAIRED ");
                }
            }
        }
    };
}