package com.example.vaccination.ui.authentication.signup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;


public class SignUpViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();


    protected LiveData<String> getToastMessageLiveData() {
        return this.toastMessageLiveData;
    }

    protected boolean validate(String email, String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern p = Pattern.compile(regex);

        if (email != null) {
            return !email.isEmpty();
        } else if (password != null) {
            return !password.isEmpty();
        }
        return true;
    }

    protected void signIn(String email, String password) {

        if (validate(email, password)) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                } else {

                }
            }).addOnCanceledListener(() -> {

            });
        }

    }

    private void sendEmailVerification(FirebaseEmailVerificationListener verificationListener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task ->
                    verificationListener.onResult(task.isSuccessful()))
                    .addOnFailureListener(e ->
                            toastMessageLiveData.setValue(e.getMessage()));
        } else {
            toastMessageLiveData.setValue("Failed to get user");
            // TODO: 17-09-2021 change fragment
            //startActivity(new Intent(this, SignIn.class));
           
        }
    }


}