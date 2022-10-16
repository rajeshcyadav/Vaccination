package com.example.vaccination.myutils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public abstract class EditTextValidator implements TextWatcher {

    TextView textView;
    public EditTextValidator(TextView textView) {
        this.textView = textView;
    }

    public abstract void validate(TextView textView, String text);

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String text = textView.getText().toString();
        validate(textView, text);
    }
}
