package com.example.vaccination.myutils;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MyAuth {

    private final Context context;
    private final String appName;

    public MyAuth(Context context, String appName) {
        this.context = context;
        this.appName = appName;
    }

    public FirebaseAuth getAuth() {
        FirebaseAuth auth;
        String dbUrl = "https://console.firebase.google.com/u/0/project/vaccination-84819/firestore/data";
        String apiKey = "AIzaSyDIOSlSl6HmSmdimd-8VK_orukbwgrjrmY";
        String projectId = "vaccination-84819";
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl(dbUrl)
                .setApiKey(apiKey)
                .setApplicationId(projectId).build();

        try {
            FirebaseApp myApp = FirebaseApp.initializeApp(context, firebaseOptions, appName);
            auth = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e) {
            auth = FirebaseAuth.getInstance(FirebaseApp.getInstance(appName));
        }
        return auth;
    }
}
