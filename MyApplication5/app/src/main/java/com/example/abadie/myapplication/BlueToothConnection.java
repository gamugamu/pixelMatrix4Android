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

// taken from http://androidcookbook.com/Recipe.seam;jsessionid=9B476BA317AA36E2CB0D6517ABE60A5E?recipeId=1665

class BluetoothConnection extends Thread {
    private final BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothSocket btSocket;
    private BluetoothAdapter madapter;
    private Thread mConnectionThread;

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
                uuid = device.getUuids()[0].getUuid();
                Log.d("############ UID", "UID" + uuid);
                tmp         = device.createInsecureRfcommSocketToServiceRecord(uuid);
                Method m    = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
                tmp         = (BluetoothSocket) m.invoke(device, 1);
                // Always cancel discovery because it will slow down a connection
                madapter.cancelDiscovery();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
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
/*
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            tmpIn    = btSocket.getInputStream();
            tmpOut   = btSocket.getOutputStream();
            buffer   = new byte[1024];
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
*/
        mConnectionThread.start();
    }

    public void run() {
        Log.d("############", "running ");

        // Keep listening to the InputStream while connected
        while (true) {
            Log.d("############", "++++++++ ");
            int available = 0;

            try {
                available = mmInStream.available();
            } catch (IOException e) {}
            Log.d("############", "AVAILABLE ?" + available);

           // if (available > 0) {
                try {
                if(mmInStream != null){
                    Log.d("############", "WILL INITIATE READ");
                    mmInStream.read(buffer);
                    Log.d("############", "Received : " + (new String(buffer)));
                }

                // Send the obtained bytes to the UI Activity
            } catch (IOException e) {
                //an exception here marks connection loss
                //send message to UI Activity
                break;
            }
/*
            try {
                Thread.sleep(10);
                this.write("hello".getBytes());
            } catch (InterruptedException e) {}
            */
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
}