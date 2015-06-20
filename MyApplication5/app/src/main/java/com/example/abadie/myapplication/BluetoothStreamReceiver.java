package com.example.abadie.myapplication;

public abstract class BluetoothStreamReceiver{
    public abstract void onStreamReceive(String outputStream);
    public abstract void bluetoothDidFailedConnect(String output);
    public abstract void bluetoothDidSucceedConnect(String output);
}
