package com.madgoatstd.miturno.Demonio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DonFelipes on 20/12/2015.
 */
public class QueueService extends Service {
    private static final String TAG = "QUEUE_STATUS";
    private static long MIN_PERIOD = 1000*2;
    private static final long MIN_DELAY = 1000 * 1;


    public static final String
            ACTION_LOCATION_BROADCAST = QueueService.class.getName() + "QueueBroadcast",
            EXTRA_TODO = "extra_todo",
            EXTRA_RESULT = "extra_result";

    Timer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mTimer = new Timer();
        this.mTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            URL tw = new URL("http://madgoatstd.com/MiTurno/getall.php");
                            URLConnection tc = tw.openConnection();
                            BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

                            String line = in.readLine();
                            Log.d(TAG, line);
                            sendBroadcastMessage(line);

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            sendBroadcastMessage("null");
                        }
                    }
                }
                , MIN_DELAY, MIN_PERIOD);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendBroadcastMessage(String todo) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_TODO, todo);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);        
    }
}
