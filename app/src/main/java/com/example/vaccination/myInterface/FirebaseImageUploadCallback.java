package com.example.vaccination.myInterface;

import android.net.Uri;

public interface FirebaseImageUploadCallback {
    void imageUploaded(Uri downloadUri,boolean success);
}
