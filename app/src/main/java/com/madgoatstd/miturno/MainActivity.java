package com.madgoatstd.miturno;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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


public class MainActivity extends Activity implements View.OnClickListener {

    Button solicitarButton, mEliminar;
    TextView mQueue, mNumero, mNumeroTitle, mWait, mLast, mMean;
    Context mContext;
    Activity mActivity;
    SharedPref preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mActivity = this;
        preferences = new SharedPref(mContext);

        solicitarButton = (Button) findViewById(R.id.solicitar);
        mEliminar = (Button) findViewById(R.id.mEliminar);
        mNumero = (TextView)findViewById(R.id.mNumero);
        mNumeroTitle = (TextView)findViewById(R.id.mNumeroTitle);
        mWait = (TextView)findViewById(R.id.mWait);
        mLast = (TextView)findViewById(R.id.mLast);
        mMean = (TextView)findViewById(R.id.mMean);
        mQueue = (TextView)findViewById(R.id.mQueue);


        solicitarButton.setOnClickListener(this);
        mEliminar.setOnClickListener(this);

        int value = preferences.getInt(SharedPref.CAMPO_MIQUEUE);
        if(value != -1){
            mEliminar.setVisibility(View.VISIBLE);
            mNumero.setVisibility(View.VISIBLE);
            mNumero.setText(""+value);
            solicitarButton.setVisibility(View.GONE);
        }

        Intent queueservice = new Intent(this, QueueService.class);
        startService(queueservice);


        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String response = intent.getStringExtra(QueueService.EXTRA_TODO);

                        if(response.equals("null")){
                            //Error
                        }else{
                            String[] datos = response.split(";");
                            mQueue.setText(datos[0]);
                            mLast.setText(datos[1]);
                            mMean.setText(datos[2]);

                            try{
                                int queue = Integer.parseInt(datos[0]);
                                int last = Integer.parseInt(datos[1]);
                                float mean = Float.parseFloat(datos[2]);
                                float calculo = (last -queue)*mean;
                                mWait.setText(calculo+"");

                            }catch (Exception e){
                                e.printStackTrace();

                                mWait.setText("Desconocido");
                            }
                        }
                    }
                }, new IntentFilter(QueueService.ACTION_LOCATION_BROADCAST)
        );




    }

    public String fetch() {
        try {
            URL tw = new URL("http://madgoatstd.com/MiTurno/solicitar.php");
            URLConnection tc = tw.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

            String line;
            line = in.readLine();

            if(line != null){
                String[] datos = line.split(";");
                if(datos[0].equals("0") || datos.equals("10"))return line;
                else return null;
            }else return null;

        }

        catch (Exception e) {
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
                break;
            case R.id.solicitar:
                getNumber get = new getNumber();
                get.execute();
                break;
            default:
                break;
        }
    }

    private class getNumber extends AsyncTask<String, String, String>{
        ProgressDialog d;

        public getNumber() {
        }

        @Override
        protected void onPreExecute() {
            d=new ProgressDialog(mContext);
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
            if(s == null){
                AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
                b.setMessage("Problema en la conexión.\nPor favor reintente.");
                b.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                b.show();
            }else{
                String[] datos = s.split(";");
                if(datos[0].equals("0")){
                    mNumero.setText(datos[1]);
                    int value = Integer.parseInt(datos[1]);
                    preferences.putInt(SharedPref.CAMPO_MIQUEUE, value);
                    mNumero.setVisibility(View.VISIBLE);
                    mNumeroTitle.setVisibility(View.VISIBLE);
                    mEliminar.setVisibility(View.VISIBLE);
                    solicitarButton.setVisibility(View.GONE);
                }
                if(datos[0].equals("10")){
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


}
