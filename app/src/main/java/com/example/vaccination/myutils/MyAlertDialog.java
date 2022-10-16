package com.example.vaccination.myutils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class MyAlertDialog extends DialogFragment {
    MyAlertDialogListener listener;

    public interface MyAlertDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    public static MyAlertDialog newInstance(String title) {
        MyAlertDialog frag = new MyAlertDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MyAlertDialogListener) context;
        } catch (ClassCastException ignored) {

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setPositiveButton("Yes", (dialog, id) -> {
                    listener.onDialogPositiveClick(MyAlertDialog.this);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    listener.onDialogNegativeClick(MyAlertDialog.this);
                });
        return builder.create();
    }

}
