/**
 * Created by abadie on 06/03/2015.
 */

package com.example.abadie.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

interface IBluetoothStreamReader{
    public void bluetoothDidReadStream(String output);
}

class BluetoothConnection extends Thread{
    private final BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothAdapter madapter;
    private Thread mConnectionThread;
    private IBluetoothStreamReader mBtSteamReader;

    byte[] buffer;

    // Unique UUID for this application, you may use different
    private static final UUID MY_UUID = UUID
            .fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    public BluetoothConnection(BluetoothDevice device, BluetoothAdapter adapter) {

        BluetoothSocket tmp = null;
        madapter = adapter;

        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        try {
            boolean temp = device.fetchUuidsWithSdp();
            UUID uuid = null;
            if( temp && device != null){
                if(device.getUuids() != null && device.getUuids().length > 0){
                uuid = device.getUuids()[0].getUuid();
                Log.d("############ UID", "UID" + uuid);
                tmp         = device.createRfcommSocketToServiceRecord(uuid);
                Method m    = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
                tmp         = (BluetoothSocket) m.invoke(device, 1);
                // Always cancel discovery because it will slow down a connection
                madapter.cancelDiscovery();
                }
            }

        } catch (IOException | InvocationTargetException |
                NoSuchMethodException | IllegalAccessException e){
            e.printStackTrace();
        }
        mmSocket = tmp;

        //now make the socket connection in separate thread to avoid FC
        mConnectionThread  = new Thread(new Runnable() {

            @Override
            public void run() {
                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                    BluetoothConnection.this.mmInStream     = mmSocket.getInputStream();
                    BluetoothConnection.this.mmOutStream    = mmSocket.getOutputStream();
                    BluetoothConnection.this.buffer         = new byte[1024];
                    BluetoothConnection.this.run();
                    Log.d("############", "CONNECTION" + BluetoothConnection.this.getName());

                } catch (IOException e) {
                    //connection to device failed so close the socket
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        mConnectionThread.start();
    }

    public void run() {
        Log.d("############", "running ");
        byte[] buffer = new byte[512];
        int bytes;
        StringBuilder readMessage = new StringBuilder();

        // Keep listening to the InputStream while connected
        while (true) {
                try {
                    Log.d("############", "WILL INITIATE READ");
                    bytes = mmInStream.read(buffer);
                    String readed = new String(buffer, 0, bytes);
                    readMessage.append(readed);


                     if(mBtSteamReader != null) {
                         mBtSteamReader.bluetoothDidReadStream(readMessage.toString());

                     }
                     if (readed.contains("\n")) {
                         readMessage.setLength(0);
                  }

                // Send the obtained bytes to the UI Activity
            } catch (IOException e) {
                //an exception here marks connection loss
                //send message to UI Activity
                break;
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            //write the data to socket stream
            Log.d("############", "BLOCK ? " + buffer.length + " - " + new String(buffer));

            if(mmOutStream != null && buffer != null) {
                Log.d("############", "WRITing ..");
                mmOutStream.write(buffer);
                Log.d("############", "END WRITing");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IBluetoothStreamReader getmBtSteamReader() {
        return mBtSteamReader;
    }

    public void setmBtSteamReader(IBluetoothStreamReader mBtSteamListener) {
        this.mBtSteamReader = mBtSteamListener;
    }
}