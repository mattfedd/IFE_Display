package com.example.matthew.ife_display;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "Main";

    //commands to send to the HC-06 device -- these can be anything
    private static final String CMD_SEND_GENERAL_DATA = "general#";

    private TextView statusTextBox;
    private TextView speedTextBox;
    private TextView mphTextBox;
    private TextView socTextBox;
    private TextView dataOutTextBox;
    private TextView dataInTextBox;

    /*
        TODO : some custom drawables for pretty display of data

        probably a bar for the soc

        maybe a radial dial or just fancy MPH meter for speed

        what other data is there
     */

    private MessageParser parser;

    private Bluetooth bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        statusTextBox = (TextView) findViewById(R.id.status);
        speedTextBox = (TextView) findViewById(R.id.speed);
        mphTextBox = (TextView) findViewById(R.id.mph);
        socTextBox = (TextView) findViewById(R.id.soc);
        dataOutTextBox = (TextView) findViewById(R.id.dataOut);
        dataInTextBox = (TextView) findViewById(R.id.dataIn);

        Typeface speedFont = Typeface.createFromAsset(getAssets(), "fonts/impact.ttf");
        speedTextBox.setTypeface(speedFont, Typeface.ITALIC);
        mphTextBox.setTypeface(speedFont, Typeface.ITALIC);
        socTextBox.setTypeface(speedFont, Typeface.ITALIC);

        parser = new MessageParser();

//        speedTextBox.setGravity(Gravity.RIGHT);
//        speedTextBox.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);

        bt = new Bluetooth(this, h);

        //after onCreate() is called, onStart() and then onResume() are automatically called.
        //onResume() is overridden below
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        bt.stop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        connectService();
    }

    private final Handler timed_h = new Handler();
    private final Runnable r = new Runnable()
    {
        public void run()
        {
            Log.d(TAG, "Running the runnable");
            bt.sendMessage(CMD_SEND_GENERAL_DATA);
            socTextBox.setText(Integer.toString(parser.soc) + "%");
            speedTextBox.setText(Integer.toString(parser.speed) + " ");
            timed_h.postDelayed(r, 75);
        }
    };

    public void connectService(){
        try {
            statusTextBox.setText("Connecting...");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                Log.d(TAG, "BT started - listening");

                for (int attempts=0; attempts<5; attempts++) {
                    bt.connectDevice("HC-06");

                    Thread.sleep(1000);

                    if (bt.getState() == Bluetooth.STATE_CONNECTED || bt.getState() == Bluetooth.STATE_CONNECTING) {
                        statusTextBox.setText("Connected");
                        Log.d(TAG, "Posting delay");
                        timed_h.postDelayed(r, 5000);
                        return;
                    } else {
                        statusTextBox.setText("Attempt " + attempts + ", trying to connect");
                    }
                }
                statusTextBox.setText("Failed all attempts to connect.");
            } else {
                Log.w(TAG, "BT started - bluetooth is not enabled");
                statusTextBox.setText("Bluetooth Not enabled");
            }
        } catch(Exception e){
            Log.e(TAG, "Unable to start bt ",e);
            statusTextBox.setText("Unable to connect " + e);
        }
    }

    private final Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String data;
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    data = new String((byte[])msg.obj);
                    Log.d(TAG, "MESSAGE_WRITE:" + data);
                    dataOutTextBox.setText(data);
                    break;
                case Bluetooth.MESSAGE_READ:
                    data = new String((byte[])msg.obj);
                    data = data.substring((Integer)msg.arg1, (Integer)msg.arg2+1);
                    Log.d(TAG, "MESSAGE_READ A:" + Arrays.toString((byte[])msg.obj));
                    Log.d(TAG, "MESSAGE_READ B:" + msg.arg1 + " " + data);
                    parser.addMessage(data);
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MESSAGE_DEVICE_NAME "+msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST "+msg);
                    break;
            }
        }
    };


}
