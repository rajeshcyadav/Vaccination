package com.example.vaccination.ui.authentication.forgotpassword;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.vaccination.R;
import com.example.vaccination.myutils.MyConstants;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends Fragment {

    public static ForgotPassword newInstance() {
        return new ForgotPassword();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.forgot_password_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View root){
        TextInputLayout textInputLayout=root.findViewById(R.id.inputEmailForgot);

        root.findViewById(R.id.textViewLoginLink3).setOnClickListener(view -> getActivity().onBackPressed());
        root.findViewById(R.id.textViewRegisterLink3).setOnClickListener(view -> {
            NavDirections action =
                    ForgotPasswordDirections.actionForgotPasswordToSignIn();
            Navigation.findNavController(view).navigate(action);
        });

        root.findViewById(R.id.buttonSendPassword).setOnClickListener(view -> {
            if (!Patterns.EMAIL_ADDRESS.matcher(textInputLayout.getEditText().getText()).matches()) {
                textInputLayout.setError(MyConstants.EMAIL_ERROR);
            }
            else
            {
                sendPasswordResetEmail(textInputLayout.getEditText().getText().toString());
            }
        });

    }

    private void sendPasswordResetEmail(String email)
    {
        FirebaseAuth auth=FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> Toast.makeText(getContext(), "Password Reset Link sent to "+email, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    String errorMessage=e.getMessage();
                    switch(errorMessage)
                    {
                        case "ERROR_QUOTA_EXCEEDED":
                        {
                            Toast.makeText(getContext(), "Quota Exceeded", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        case "ERROR_RETRY_LIMIT_EXCEEDED":
                        {
                            Toast.makeText(getContext(), "Please Wait before Requesting Another Email",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case "ERROR_CANCELED":
                        {
                            Toast.makeText(getContext(), "Email not Sent", Toast.LENGTH_SHORT).show();
                            break;
                        }


                    }
                });
    }
}