package com.example.vaccination.myInterface;

import com.google.firebase.firestore.Query;

import java.util.List;

public interface FirebaseQueryCallback {
    void onResult(boolean success, List<?> list, Query query);
}
