package com.example.abadie.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

interface IBluetoothManagerable{

    public void willStartFindingModule();
    public void didFoundBluetoothObject(BluetoothDevice device);
    public void didFindingFoundBluetoothObject();
}
/**
 * Created by abadie on 10/03/2015.
 */
public class BluetoothManager{
    int REQUEST_ENABLE_BT = 1;

    private BluetoothConnection btSocketManager;
    private ArrayList<BluetoothDevice> listDevice = new ArrayList<BluetoothDevice>();
    private BroadcastReceiver mReceiver;
    private Activity mBbtClient;
    private IBluetoothManagerable mBtManagerable;
    private BluetoothDevice mCurrentPairedDevice;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SINGLETON

    private static BluetoothManager sInstance;
    private BluetoothManager(){}

    public static synchronized BluetoothManager getInstance(){
        if(sInstance == null){
            sInstance = new BluetoothManager();
        }
        return sInstance;
    }

    public void setManagearable(Activity managerable){
        //TODO cancel last managerable
        mBbtClient      = managerable;
        mBtManagerable  = (IBluetoothManagerable)managerable;
    }

    // PUBLIC
    public void findBTModule(){
        if(mReceiver != null)
            mBbtClient.unregisterReceiver(mReceiver);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listDevice.clear();

        if (!mBluetoothAdapter.isEnabled()){
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mBbtClient.startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
        }else{
            this.startBTDiscovery();
            mBbtClient.registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        }
    }

    public BluetoothDevice getBluetoothDeviceFromDiscoveryList(Integer index){
        if(listDevice.size() > index)
            return listDevice.get(index);
        else
            return null;
    }

    public void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            mCurrentPairedDevice = device;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            btSocketManager.cancel();
            mCurrentPairedDevice = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTER / SETTER
    public BluetoothDevice getCurrentPairedDevice() {
        return mCurrentPairedDevice;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    public void onDestroy(){
        try{
            if(mReceiver != null)
                mBbtClient.unregisterReceiver(mReceiver);

            if(mPairReceiver != null)
                mBbtClient.unregisterReceiver(mPairReceiver);
        }
        catch (IllegalArgumentException e) { }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SETUP
    private void setUpBtManagerAndlistenSPPBluetooth(BluetoothDevice device, BluetoothAdapter adapter){
        btSocketManager = new BluetoothConnection(device, adapter);
    }

    private void startBTDiscovery(){
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtManagerable.willStartFindingModule();

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    listDevice.add(device);
                    mBtManagerable.didFoundBluetoothObject(device);
                }

                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    mBluetoothAdapter.cancelDiscovery();
                    mBtManagerable.didFindingFoundBluetoothObject();
                }
            }
        };

        mBbtClient.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        mBbtClient.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        mBbtClient.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

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

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                Log.d("############", "STATE " + state + " " + prevState);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Log.d("############", "PAIRED ");
                    BluetoothDevice btDevice        = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothAdapter btAdaptater     = BluetoothAdapter.getDefaultAdapter();

                    BluetoothManager.this.scanBt(btAdaptater);
                    BluetoothManager.this.setUpBtManagerAndlistenSPPBluetooth(btDevice, btAdaptater);

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Log.d("############", "UNPAIRED ");
                }
            }
        }
    };
}