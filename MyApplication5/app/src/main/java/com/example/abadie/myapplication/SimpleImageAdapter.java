package com.example.abadie.myapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleImageAdapter extends BaseAdapter {
    public List<BluetoothDevice> mList;
    private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
    private LayoutInflater mLayoutInflater;


    public SimpleImageAdapter(Context context){
        mList = new ArrayList<BluetoothDevice>();
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void add(BluetoothDevice item){
        mList.add(item);
        this.notifyDataSetChanged();
    }

    public void clear(){
        mList.clear();
        this.notifyDataSetChanged();
    }

    public void deviceStateChanged(BluetoothDevice item, int state){
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView         = this.mLayoutInflater.inflate(R.layout.listbt_layout, null);
        ImageView icon      = (ImageView)convertView.findViewById(R.id.icon);
        TextView title      = (TextView)convertView.findViewById(R.id.title);
        TextView subTitle   = (TextView)convertView.findViewById(R.id.subTitle);

        BluetoothDevice item = mList.get(position);
        title.setText(item.getName());
        subTitle.setText(item.getAddress());

        int btDeviceMode = item.getBondState();
        String colorFilter = "#ffffff";

        switch (btDeviceMode){
            case BluetoothDevice.BOND_NONE : {
                colorFilter = "#ff0000";
                break;
            }
            case BluetoothDevice.BOND_BONDING : {
                colorFilter = "#0000ff";
                break;
            }
            case BluetoothDevice.BOND_BONDED : {
                colorFilter = "#ffffff";
                break;
            }
            default : {
                colorFilter = "#ffff00";
            }
        }
        icon.setColorFilter(Color.parseColor(colorFilter), PorterDuff.Mode.MULTIPLY);
        //icon.setAlpha(item.getBondState() == BluetoothDevice.BOND_BONDED ? 1 : .2f);
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