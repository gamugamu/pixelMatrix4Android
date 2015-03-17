package com.example.abadie.myapplication;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

public class MainActivity extends ActionBarActivity{
    // Helper
    BluetoothManager mBtManager;
    // GUI
    private ListView mListBt;
    private ArrayAdapter<String> mAdapter;
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
        mAdapter.add(device.getAddress() + " - " + device.getName() + "\n");
        mAdapter.notifyDataSetChanged();
    }

    private void clearList(){
        mAdapter.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SETUP
    private void setupBTList(){
        mListBt  = (ListView) findViewById(R.id.listView);

        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile" };

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i)
            list.add(values[i]);

        final StableArrayAdapter adapter = new StableArrayAdapter(this, list);

       // mAdapter = new ArrayAdapter<String>(this, R.layout.listbt_layout);

        mListBt.setAdapter(adapter);

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

                if (action.equals(BTACTION.ACTION_FOUND.toString())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    MainActivity.this.didFoundBluetoothObject(device);
                }

                if (action.equals(BTACTION.ACTION_DISCOVERY_FINISHED.toString()))
                    MainActivity.this.undisplayWait();
            }
        });
    }

    private void unsetBTManager(){
        mBtManager.unregisterToBluetoothEvent();
    }

    private class StableArrayAdapter extends BaseAdapter{

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
        List<String> mList;
        LayoutInflater mLayoutInflater;


        public StableArrayAdapter(Context context,
                                  List<String> objects) {
            mList = objects;
            mLayoutInflater = LayoutInflater.from(context);

            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = this.mLayoutInflater.inflate(R.layout.listbt_layout, null);
            TextView view = (TextView)convertView.findViewById(R.id.title);
            view.setText("test " + mList.get(position));

            return convertView;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}