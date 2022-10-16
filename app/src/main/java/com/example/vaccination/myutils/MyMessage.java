package com.example.vaccination.myutils;

import android.content.Context;
import android.widget.Toast;

public class MyMessage {
    private final Context context;
    private final String defaultMessage;

    public MyMessage(Context context, String defaultMessage) {
        this.context = context;
        this.defaultMessage = defaultMessage;
    }

    public void showToastMessage() {
        showToastMessage(context, defaultMessage);
    }

    public void showToastMessage(String message) {
        showToastMessage(context, message);
    }

    public void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
