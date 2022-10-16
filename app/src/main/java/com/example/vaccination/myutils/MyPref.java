package com.example.vaccination.myutils;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPref {
    private final Context context;
    private final SharedPreferences sharedPref;

    public MyPref(Context context, String id) {
        this.context = context;
        sharedPref = context.getSharedPreferences(id, Context.MODE_PRIVATE);
    }


    public void addPref(String id,String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(id, value);
        editor.apply();
    }

    public String readPref(String id,String defaultValue){
        return sharedPref.getString(id,defaultValue);
    }



}
