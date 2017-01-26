package com.example.matthew.ife_display;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "Main";

    //commands to send to the HC-06 device -- these can be anything
    private static final String CMD_START_SENDING_DATA = "A";
    private static final String CMD_STOP_SENDING_DATA = "B";

    private String speedText = "Speed: ";
    private String SocText = "SoC: ";

    private float speed = 0.0f;
    private float soc = 0.0f;

    private TextView statusTextBox;
    private TextView speedTextBox;
    private TextView socTextBox;

    /*
        TODO : some custom drawables for pretty display of data

        probably a bar for the soc

        maybe a radial dial or just fancy MPH meter for speed

        what other data is there
     */

    private Bluetooth bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextBox = (TextView) findViewById(R.id.status);
        speedTextBox = (TextView) findViewById(R.id.speed);
        socTextBox = (TextView) findViewById(R.id.soc);

        bt = new Bluetooth(this, h);
        connectService();
    }

    public void connectService(){
        try {
            statusTextBox.setText("Connecting...");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                Log.d(TAG, "BT started - listening");
                if(bt.connectDevice("HC-06"))
                {
                    statusTextBox.setText("Connected");
                    bt.sendMessage(CMD_START_SENDING_DATA);
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
                    Log.d(TAG, "MESSAGE_WRITE ");
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
