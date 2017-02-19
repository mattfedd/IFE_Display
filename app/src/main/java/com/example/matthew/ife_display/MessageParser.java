package com.example.matthew.ife_display;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Matthew on 2/12/2017.
 */

public class MessageParser {
    private final static String TAG = "MessageParser";
    public String msg;

    public int rpm = 0;
    public int speed = 0;
    public int soc = 0;
    public int fault = 0;

    public void MessageParser()
    {
        msg = "";
    }

    public void addMessage(String data)
    {
        if(data.contains("!"))
        {
            //end of message, let's get to parsing
            Log.d(TAG, "Adding:" + data);
            msg += data;
            Log.d(TAG, "Total msg:"+msg);
            parse(msg);
            msg = "";
        }
        else
        {
            Log.d(TAG, "Adding:" + data);
            msg += data;
        }
    }


    public void parse(String data)
    {
        //test data: general# Speed = 60; RPM = 2200; SOC = 78; fault = 0;!

        if (data.indexOf(";") == -1) {
            Log.d(TAG, "Big error in the message format -- " + data);
        }

        String temp = data.substring(data.indexOf("#")+1, data.indexOf("!"));
        String info = "";
        int num = 0;

        while(true) {
            if (temp.indexOf(";") == -1) {
                break;
            }

            try {
                info = temp.substring(0, temp.indexOf(";") + 1);
                Log.d(TAG, "info: " + info);
                num = Integer.parseInt(info.substring(info.indexOf("=") + 2, info.indexOf(";")));
                Log.d(TAG, "num: " + Integer.toString(num));
            }
            catch (Exception e)
            {
                Log.d(TAG, "Some error in the message format, discarding message. " + data);
                break;
            }

            if(info.contains("Speed")) speed = num;
            else if(info.contains("RPM")) rpm = num;
            else if(info.contains("SOC")) soc = num;
            else if(info.contains("fault")) fault = num;

            temp = temp.substring(temp.indexOf(";")+1, temp.length());
            Log.d(TAG, "new temp: " + temp);
        }
    }
}
