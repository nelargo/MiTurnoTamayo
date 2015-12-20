package com.madgoatstd.miturno.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by DonFelipes on 20/12/2015.
 */
public class SharedPref {
    public static String NAME = "MITURNO";
    public static String CAMPO_MIQUEUE = "mi_queue";


    Context mContext;
    public SharedPreferences sh;

    public SharedPref(Context ctx){
        super();
        mContext = ctx;
        sh = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void putInt(String KEY, int data){
        SharedPreferences.Editor e = sh.edit();
        e.putInt(KEY, data);
        e.apply();
    }

    public int getInt(String KEY){
        int value = sh.getInt(KEY, -1);
        return value;
    }


    public void rmvInt(String KEY){
        SharedPreferences.Editor e = sh.edit();
        e.putInt(KEY, -1);
        e.apply();
    }

}
