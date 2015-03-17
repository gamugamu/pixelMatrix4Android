package com.example.abadie.myapplication;

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
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PixelMatrixManagerActivity extends ActionBarActivity{

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
        mBtManager.unregisterToStreamBtEvent();
        super.onDestroy();
    }

    private void setUpBtDevice(){
        mBtManager = BluetoothManager.getInstance(this.getApplicationContext());
        mBtManager.registerToStreamBtEvent(new BluetoothStreamReceiver() {
            @Override
            public void onStreamReceive(String outputStream) {
                PixelMatrixManagerActivity.this.didReadStream(outputStream);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void didReadStream(final String output){
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
                    // without '\n' the firmwar will never send
                    // confirmation output.
                    String message = v.getText().toString();
                    message += '\n';
                    Log.d("############", "SCAN " + message);
                    mBtManager.write(message.getBytes());

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
