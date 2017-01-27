package com.example.matthew.ife_display;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "Main";

    //commands to send to the HC-06 device -- these can be anything
    private static final String CMD_START_SENDING_DATA = "A";
    private static final String CMD_STOP_SENDING_DATA = "B";
    private static final String CMD_SEND_SPEED_DATA = "speed#";
    private static final String CMD_SEND_SOC_DATA = "soc#";

    //states for what data to expect
    private static final int NONE = 0;
    private static final int SPEED = 1;
    private static final int SOC = 2;
    private static final int FAULT = 3;

    private int expecting = NONE;

    private String speedText = "Speed: ";
    private String SocText = "SoC: ";

    private float speed = 0.0f;
    private float soc = 0.0f;

    private static int counter = 0;

    private TextView statusTextBox;
    private TextView speedTextBox;
    private TextView socTextBox;
    private TextView dataOutTextBox;
    private TextView dataInTextBox;

    /*
        TODO : some custom drawables for pretty display of data

        probably a bar for the soc

        maybe a radial dial or just fancy MPH meter for speed

        what other data is there
     */

    private final Handler timed_h = new Handler();
    private final Runnable r = new Runnable()
    {
        public void run()
        {
            //do stuff
            //either send command to keep sending data, or request something, I guess
            counter++;
            Log.d(TAG, Integer.toString(counter));
            if(counter % 2 == 0)
            {
                bt.sendMessage(CMD_SEND_SPEED_DATA);//dataOutTextBox.setText(CMD_SEND_SPEED_DATA + Integer.toString(counter));
            }
            else
            {
                bt.sendMessage(CMD_SEND_SOC_DATA);//dataOutTextBox.setText(CMD_SEND_SOC_DATA + Integer.toString(counter));
            }
            timed_h.postDelayed(r, 1000);
        }
    };

    private Bluetooth bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextBox = (TextView) findViewById(R.id.status);
        speedTextBox = (TextView) findViewById(R.id.speed);
        socTextBox = (TextView) findViewById(R.id.soc);
        dataOutTextBox = (TextView) findViewById(R.id.dataOut);
        dataInTextBox = (TextView) findViewById(R.id.dataIn);

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
        timed_h.postDelayed(r, 1000);
    }

    public void connectService(){

        try {
            statusTextBox.setText("Connecting...");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                Log.d(TAG, "BT started - listening");
                if(bt.connectDevice("HC-06") && bt.sendMessage(CMD_SEND_SPEED_DATA)) //send test message
                {
                    statusTextBox.setText("Connected");
                }
                else
                {
                    statusTextBox.setText("Cannot connect to device.");
                }

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
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE " + msg.obj);
                    if(msg.obj.toString().contains(CMD_SEND_SPEED_DATA))
                    {
                        //we just sent the speed command
                        dataOutTextBox.setText(CMD_SEND_SPEED_DATA);
                    }
                    if(msg.obj.toString().contains(CMD_SEND_SOC_DATA))
                    {
                        //we just sent the soc command
                        dataOutTextBox.setText(CMD_SEND_SOC_DATA);
                    }
                    break;
                case Bluetooth.MESSAGE_READ:
                    Log.d(TAG, "MESSAGE_READ ");

                    //Filter message here based on the way it's sent from the HC-06
                    //Check for speed or soc data and update internal displays accordingly

                    /*
                    Something like
                    if(msg.contains("speed"))
                        do speed thing and invalidate the drawables
                    etc
                     */

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
