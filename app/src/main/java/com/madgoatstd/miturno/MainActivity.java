package com.madgoatstd.miturno;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.madgoatstd.miturno.Demonio.QueueService;
import com.madgoatstd.miturno.Preferences.SharedPref;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;


public class MainActivity extends Activity implements View.OnClickListener {

    Button solicitarButton, mEliminar;
    TextView mQueue, mNumero, mNumeroTitle, mWait, mLast, mMean;
    Context mContext;
    Activity mActivity;
    SharedPref preferences;
    DecimalFormat df;
    float TIME_MAX_TO_ADVISE = 15;
    float TIME_MIN_TO_ADVISE = 2;
    int MAX_NUM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        mContext = this;
        mActivity = this;
        preferences = new SharedPref(mContext);

        solicitarButton = (Button) findViewById(R.id.solicitar);
        mEliminar = (Button) findViewById(R.id.mEliminar);
        mNumero = (TextView) findViewById(R.id.mNumero);
        mNumeroTitle = (TextView) findViewById(R.id.mNumeroTitle);
        mWait = (TextView) findViewById(R.id.mWait);
        mLast = (TextView) findViewById(R.id.mLast);
        mMean = (TextView) findViewById(R.id.mMean);
        mQueue = (TextView) findViewById(R.id.mQueue);


        solicitarButton.setOnClickListener(this);
        mEliminar.setOnClickListener(this);

        int value = preferences.getInt(SharedPref.CAMPO_MIQUEUE);
        if (value != -1) {
            mEliminar.setVisibility(View.VISIBLE);
            mNumero.setVisibility(View.VISIBLE);
            mNumero.setText("" + value);
            solicitarButton.setVisibility(View.GONE);
        }

        Intent queueservice = new Intent(this, QueueService.class);
        startService(queueservice);


        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String response = intent.getStringExtra(QueueService.EXTRA_TODO);

                        if (response.equals("null")) {
                            //Error
                        } else {
                            String[] datos = response.split(";");


                            try {
                                int queue = Integer.parseInt(datos[0]);
                                int last = Integer.parseInt(datos[1]);
                                float mean = Float.parseFloat(datos[2]);
                                float calculo = (last - queue) * mean;
                                int position = preferences.getInt(SharedPref.CAMPO_MIQUEUE);
                                boolean notify = preferences.getBool(SharedPref.CAMPO_NOTIFICACIONES);



                                mQueue.setText("" + queue);
                                mLast.setText("" + last);
                                mMean.setText("" + df.format(mean));

                                if(last == 0)
                                    mEliminar.callOnClick();

                                if(position > queue)
                                    mWait.setText(df.format(calculo));
                                else
                                    mWait.setText("-");

                                if (notify && calculo > TIME_MIN_TO_ADVISE && calculo <= TIME_MAX_TO_ADVISE) {
                                    show_notification(calculo, queue, position, 0);
                                }else if(notify && position == queue){
                                    show_notification(calculo, queue, position, 1);
                                    preferences.putBool(SharedPref.CAMPO_NOTIFICACIONES, false);
                                    preferences.rmvInt(SharedPref.CAMPO_MIQUEUE);
                                }else if(notify && (queue - position) == MAX_NUM){
                                    show_notification(calculo, queue, position, 2);
                                }


                            } catch (Exception e) {
                                e.printStackTrace();

                                mWait.setText("Desconocido");
                            }
                        }
                    }
                }, new IntentFilter(QueueService.ACTION_LOCATION_BROADCAST)
        );


    }

    private void show_notification(float calculo, int queue, int position, int isTurn) {
        int turn = preferences.getInt("TURNO");
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Intent intent = getIntent();//new Intent(this, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(isTurn == 0) {
            n = new Notification.Builder(this)
                    .setContentTitle("Posición " + position + " de " + queue)
                    .setContentText("Quedan " + calculo + "minutos para su turno.")
                    .setSmallIcon(R.drawable.ic_alert)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[] { 1000, 1000})
                    .setSound(alarmSound)
                    .build();
        }else if(isTurn == 1){
            n = new Notification.Builder(this)
                    .setContentTitle("Es su turno!")
                    .setSmallIcon(R.drawable.ic_alert)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setSound(alarmSound)
                    .build();

        }else{
            n = new Notification.Builder(this)
                    .setContentTitle("Quedan "+(queue - position)+ "números para su turno")
                    .setSmallIcon(R.drawable.ic_alert)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[] { 1000, 1000})
                    .setSound(alarmSound)
                    .build();

        }


        if(turn != isTurn) {
            notificationManager.notify(0, n);
            preferences.putInt("TURNO", isTurn);
        }


    }

    public String fetch() {
        try {
            URL tw = new URL("http://madgoatstd.com/MiTurno/solicitar.php");
            URLConnection tc = tw.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

            String line;
            line = in.readLine();

            if (line != null) {
                String[] datos = line.split(";");
                if (datos[0].equals("0") || datos.equals("10")) return line;
                else return null;
            } else return null;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mEliminar:
                mNumero.setText("-1");
                mNumero.setVisibility(View.GONE);
                solicitarButton.setVisibility(View.VISIBLE);
                mEliminar.setVisibility(View.GONE);
                preferences.rmvInt(SharedPref.CAMPO_MIQUEUE);
                preferences.putBool(SharedPref.CAMPO_NOTIFICACIONES, false);
                break;
            case R.id.solicitar:
                getNumber get = new getNumber();
                get.execute();
                break;
            default:
                break;
        }
    }

    private class getNumber extends AsyncTask<String, String, String> {
        ProgressDialog d;

        public getNumber() {
        }

        @Override
        protected void onPreExecute() {
            d = new ProgressDialog(mContext);
            d.setMessage("Solicitando Número");
            d.setCancelable(false);
            d.setCanceledOnTouchOutside(false);
            d.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return fetch();
        }

        @Override
        protected void onPostExecute(String s) {
            d.dismiss();
            if (s == null) {
                AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
                b.setMessage("Problema en la conexión.\nPor favor reintente.");
                b.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                b.show();
            } else {
                String[] datos = s.split(";");
                if (datos[0].equals("0")) {
                    mNumero.setText(datos[1]);
                    int value = Integer.parseInt(datos[1]);
                    preferences.putInt(SharedPref.CAMPO_MIQUEUE, value);
                    mNumero.setVisibility(View.VISIBLE);
                    mNumeroTitle.setVisibility(View.VISIBLE);
                    mEliminar.setVisibility(View.VISIBLE);
                    solicitarButton.setVisibility(View.GONE);
                    show_notification_question();
                }
                if (datos[0].equals("10")) {
                    AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
                    b.setMessage(datos[1]);
                    b.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    b.show();
                }
            }
        }
    }

    private void show_notification_question() {
        AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
        b.setMessage("¿Activar notificaciones?");
        b.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferences.putBool(SharedPref.CAMPO_NOTIFICACIONES, true);
                dialog.dismiss();
            }
        });
        b.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferences.putBool(SharedPref.CAMPO_NOTIFICACIONES, false);
                dialog.dismiss();
            }
        });
        b.show();
    }


}
