package com.example.vaccination.ui.authentication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vaccination.R;
import com.example.vaccination.myInterface.FirebaseEmailVerificationListener;
import com.example.vaccination.ui.authentication.signin.SignIn;
import com.example.vaccination.ui.authentication.signin.SignInDirections;
import com.example.vaccination.ui.authentication.signup.SignUp;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EmailVerification extends Fragment {
    private FirebaseAuth auth;
    private CoordinatorLayout coordinatorLayout;
    public EmailVerification() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_email_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View root) {
        coordinatorLayout = root.findViewById(R.id.emailVerificationCoordRoot);
        FirebaseEmailVerificationListener firebaseEmailVerificationListener = success -> {
            if (success) {
                String message = "Email Verification link sent";
                setSnackBar(coordinatorLayout, message);
            } else {
                Toast.makeText(getContext(), "Failed to Send Email Verification", Toast.LENGTH_SHORT).show();
            }
        };

        root.findViewById(R.id.buttonSendVerification).setOnClickListener(view -> {
            /*if (!Patterns.EMAIL_ADDRESS.matcher(textInputLayout.getEditText().getText())
            .matches())
                textInputLayout.setError("Invalid Email");
            else {*/
            sendEmailVerification(firebaseEmailVerificationListener,root);
            //}
        });


        root.findViewById(R.id.textViewLoginLinkVerification).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), SignIn.class));
            //finish();
        });

        root.findViewById(R.id.textViewRegisterLinkVerification).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), SignUp.class));
            //finish();
        });
    }

    private void sendEmailVerification(FirebaseEmailVerificationListener verificationListener,View view) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task ->
                    verificationListener.onResult(task.isSuccessful()))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(getContext(), "Failed to get Current User", Toast.LENGTH_LONG).show();
            NavDirections action =
                    EmailVerificationDirections.actionEmailVerificationToSignIn();
            Navigation.findNavController(view).navigate(action);
            //finish();
        }
    }

    private void setSnackBar(View root, String title) {
        Snackbar.make(root, title, Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", view -> {
                    NavDirections action =
                            EmailVerificationDirections.actionEmailVerificationToSignIn();
                    Navigation.findNavController(view).navigate(action);
                    //finish();
                })
                .show();
    }
}