package com.example.abadie.myapplication;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PixelMatrixManagerActivity extends ActionBarActivity implements IBluetoothStreamReader, IBluetoothManagerable{

    BluetoothManager mBtManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pixel_matrix_manager);

        this.setUpBtDevice();
        this.setUpInputText();
    }

    @Override
    public void onDestroy() {
        mBtManager.setmBtSteamReader(null);
        mBtManager.unregisterToBluetoothEvent(this);
        super.onDestroy();
    }

    private void setUpBtDevice(){
        mBtManager = BluetoothManager.getInstance(this.getApplicationContext());
        mBtManager.setmBtSteamReader(this);
        mBtManager.registerToBluetoothEvent(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface IBluetoothManagerable
    public void willStartFindingModule(){

    }

    public void didFoundBluetoothObject(BluetoothDevice device){

    }

    public void didEndFindingBluetoothObject(){
    }


    public void bluetoothDidReadStream(final String output){
        Log.d("############", "XReceived : " + output);

        new Thread(){
            public void run(){
                runOnUiThread(new Runnable(){
                    public void run(){
                        //Do your UI operations like dialog opening or Toast here
                        TextView textView = (TextView) findViewById(R.id.textView1);
                        textView.setText(output);
                    }
                });
            }
        }.start();
    }

        private void setUpInputText(){
        EditText editText = (EditText) findViewById(R.id.bt_input);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = true;

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d("############", "SCAN " + v.getText());
                    handled = false;
                }
                return handled;
            }
        });
    }

    private void setUpGif(){
        try {
            GifImageView gifImageView = new GifImageView(this);
            GifDrawable gifFromResource = null;
            gifFromResource = new GifDrawable( getResources(), R.drawable.dancingbanana);
            gifImageView.setImageDrawable(gifFromResource);
            this.setContentView(gifImageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pixel_matrix_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pixel_matrix_manager, container, false);
            return rootView;
        }
    }
}
