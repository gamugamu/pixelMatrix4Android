package com.example.abadie.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.github.johnpersano.supertoasts.SuperToast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by abadie on 10/03/2015.
 */
public class BluetoothManager implements IBluetoothStreamReader{

    int REQUEST_ENABLE_BT = 1;

    private ArrayList<BluetoothDevice> listDevice = new ArrayList<BluetoothDevice>(); // list all detected device

    private BluetoothDevice mCurrentPairedDevice;                    // current device checked, or null if not exist
    private Context mApplicationContext;                             // let the btManager listen the bt event
    private BroadcastReceiver btBroadCastClient;                     // listen to BT Pairing device
    private BluetoothStreamReceiver btBroadCastStreamReceiver;       // listen to stream BT device

    private BluetoothConnection btSocketManager;    // helper to make bt pairing device.
    private BroadcastReceiver mReceiver;            // internal listening to BT Pairing device. Bond to btBroadCastClient

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SINGLETON
    private static BluetoothManager sInstance;
    private BluetoothManager(Context context){
        mApplicationContext = context.getApplicationContext();
    }

    public static synchronized BluetoothManager getInstance(Context context){
        if(sInstance == null){
            sInstance = new BluetoothManager(context);
        }
        return sInstance;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC

    ////////////// DISCOVERY
    public void findBTModule(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listDevice.clear();

        if (!mBluetoothAdapter.isEnabled()){
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)mApplicationContext).startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
        }else{
            this.startBTDiscovery();
        }
    }

    public BluetoothDevice getBluetoothDeviceFromDiscoveryList(Integer index){
        if(listDevice.size() > index)
            return listDevice.get(index);
        else
            return null;
    }

    ////////////// PAIRING
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

    ////////////// READ / WRITE
    public void write(byte[] buffer){
        if(btSocketManager != null)
          btSocketManager.write(buffer);
    }

    public void  registerToStreamBtEvent(BluetoothStreamReceiver receiver){
        btBroadCastStreamReceiver = receiver;
    }

    public void  unregisterToStreamBtEvent(){
        btBroadCastStreamReceiver = null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTER / SETTER
    public BluetoothDevice getCurrentPairedDevice() {
        return mCurrentPairedDevice;
    }

    public ArrayList<BluetoothDevice> getListedBTDevice(){
        return listDevice;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // IBLUETOOTHSTREAMREADER
    public void bluetoothDidFailedInitialisation(String error){
        // TODO ici
        Log.d("############ ", "bluetoothDidFailedInitialisation");

        if(btBroadCastStreamReceiver != null)
            btBroadCastStreamReceiver.bluetoothDidFailedConnect(error);
    }

    public void bluetoothDidReadStream(String output){
        Log.d("############", "Received : " + output);

        if(btBroadCastStreamReceiver != null)
            btBroadCastStreamReceiver.onStreamReceive(output);
    }

    // REGISTER
    public void registerToBluetoothEvent(BroadcastReceiver receiver){
        btBroadCastClient = receiver;
    }

    public void unregisterToBluetoothEvent(){
        btBroadCastClient = null;
        this.unregisterBluetoothCall();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SETUP
    private void setUpBtManagerAndlistenSPPBluetooth(BluetoothDevice device, BluetoothAdapter adapter){
        btSocketManager = new BluetoothConnection(device, adapter, this);
    }

    private void startBTDiscovery(){
       this.unregisterBluetoothCall();

        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    listDevice.add(device);
                    intent.setAction(BTACTION.ACTION_FOUND.toString());
                    btBroadCastClient.onReceive(context, intent);
                }

                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    intent.setAction(BTACTION.ACTION_DISCOVERY_STARTED.toString());
                    btBroadCastClient.onReceive(context, intent);
                }

                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    mBluetoothAdapter.cancelDiscovery();
                    intent.setAction(BTACTION.ACTION_DISCOVERY_FINISHED.toString());
                    btBroadCastClient.onReceive(context, intent);
                }

                else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                    Log.d("############", "STATE " + state + " " + prevState);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        Log.d("############", "PAIRED ");
                        BluetoothDevice btDevice      = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        BluetoothAdapter btAdaptater  = BluetoothAdapter.getDefaultAdapter();

                        BluetoothManager.this.scanBt(btAdaptater);
                        BluetoothManager.this.setUpBtManagerAndlistenSPPBluetooth(btDevice, btAdaptater);

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                        Log.d("############", "UNPAIRED ");
                    } else{
                        Log.d("############", "UNKNOW ");
                    }

                    btBroadCastClient.onReceive(context, intent);
                }
            }
        };

        mApplicationContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        mApplicationContext.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        mApplicationContext.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        mApplicationContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        mBluetoothAdapter.startDiscovery();
    }

    // LOGIC
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
        device.createBond();

        BluetoothAdapter btAdaptater  = BluetoothAdapter.getDefaultAdapter();
        BluetoothManager.this.setUpBtManagerAndlistenSPPBluetooth(device, btAdaptater);

        this.scanBt(BluetoothAdapter.getDefaultAdapter());
    }

    private void unregisterBluetoothCall(){
        try{
            mApplicationContext.unregisterReceiver(mReceiver);
        }
        catch (IllegalArgumentException e) { }
    }
}
