package com.example.vaccination.ui.authentication.signup;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.vaccination.R;
import com.example.vaccination.data.Counter;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Index;
import com.example.vaccination.myutils.EditTextValidator;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.ui.authentication.signin.SignInDirections;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class SignUp extends Fragment {

    private CoordinatorLayout coordinatorLayout;
    private LoadingDialog loadingDialog;
    MaterialButtonToggleGroup materialButtonToggleGroup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fireStore;
    //private String userType;

    public static SignUp newInstance() {
        return new SignUp();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_up_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getContext(), "Creating Account...");
        loadingDialog.setCancelable(false);
        init(view);
    }


    private void init(View root) {
        TextInputLayout email = root.findViewById(R.id.inputEmail);
        TextInputLayout password = root.findViewById(R.id.inputPassword);
        TextInputLayout confirmPassword = root.findViewById(R.id.inputConfirmPassword);
        materialButtonToggleGroup = root.findViewById(R.id.materialButtonToggleGroup2);
        coordinatorLayout = root.findViewById(R.id.signUpCoordRoot);

        email.getEditText().addTextChangedListener(new EditTextValidator(email.getEditText()) {
            @Override
            public void validate(TextView textView, String text) {
                if (!Patterns.EMAIL_ADDRESS.matcher(text).matches())
                    textView.setError("Invalid Email");
            }
        });
        root.findViewById(R.id.textViewLoginLink).setOnClickListener(view -> {
            NavDirections action = SignUpDirections.actionSignUpToSignIn();
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
        });
        root.findViewById(R.id.buttonRegister).setOnClickListener(view -> {
            loadingDialog.show();
            if (validate(email, password, confirmPassword))
                signUp(email, password);
            else
                loadingDialog.dismiss();

        });

    }

    private void signUp(TextInputLayout email, TextInputLayout password) {
        // setUserAccount(materialButtonToggleGroup);
        String usernameString = getText(email);
        String passwordString = getText(password);


        FirebaseEmailVerificationListener firebaseEmailVerificationListener = success -> {
            if (success) {
                String message = "Email Verification sent to " + usernameString;
                setSnackBar(coordinatorLayout, message, true);
                createEmptyCollection(DbNode.USER.toString(), usernameString);
                // NavDirections action=SignUpDirections.actionSignUpToSignIn();
                // Navigation.findNavController(getActivity(),R.id.nav_host_fragment).popBackStack();


                //deprecated
                /*if (userType.equals(DbNode.USER.toString())) {
                    createEmptyCollection(DbNode.USER.toString());


                } else if (userType.equals(DbNode.HOSPITAL.toString())) {
                    createEmptyCollection(DbNode.HOSPITAL.toString());

                } else if (userType.equals(DbNode.ADMIN.toString())) {
                    createEmptyCollection(DbNode.ADMIN.toString());
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

                }*/
            } else {
                Toast.makeText(getContext(), "Failed to Send Email Verification", Toast.LENGTH_SHORT).show();
            }
        };


        mAuth.createUserWithEmailAndPassword(usernameString, passwordString)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        sendEmailVerification(firebaseEmailVerificationListener);
                    } else {
                        Toast.makeText(getContext(), "" + task.getException().getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCanceledListener(() -> {
                    Toast.makeText(getContext(), MyConstants.CANCELLED_BY_USER,
                            Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                });
    }

    private void sendEmailVerification(FirebaseEmailVerificationListener verificationListener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task ->
                    verificationListener.onResult(task.isSuccessful()))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(getContext(), "Failed to get Current User", Toast.LENGTH_LONG).show();
            // TODO: 17-09-2021 "User is Null"
        }
    }

    private boolean validate(TextInputLayout email, TextInputLayout password,
                             TextInputLayout confirmPassword) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern p = Pattern.compile(regex);

        boolean test = ((confirmPassword.getEditText().getText()).toString().trim()).equals((password.getEditText().getText()).toString().trim());
        //String passwordStr = getText(password).get();

        if (getText(email) != null && !getText(email).isEmpty()) {
            setError(email, null, false);
            if (getText(password) != null && !getText(password).isEmpty()) {
                setError(password, null, false);
                if (getText(confirmPassword) != null && !getText(confirmPassword).isEmpty()) {
                    setError(confirmPassword, null, false);
                    if (test)
                        return true;
                    else {
                        Toast.makeText(getContext(), MyConstants.PASSWORD_NOT_MATCH, Toast.LENGTH_SHORT).show();
                        setSnackBar(coordinatorLayout, MyConstants.PASSWORD_NOT_MATCH, false);
                        return false;
                    }
                } else {
                    setError(confirmPassword, MyConstants.EMPTY_FIELD, true);
                    return false;
                }
            } else {
                setError(password, MyConstants.EMPTY_FIELD, true);
                return false;
            }
        } else {
            setError(email, MyConstants.EMPTY_FIELD, true);
            return false;
        }


    }


    /*private void setUserAccount(MaterialButtonToggleGroup group) {
        if (group.getCheckedButtonId() == R.id.buttonHospitalSignUp) {
            userType = DbNode.HOSPITAL.toString();
        } else {
            userType = DbNode.USER.toString();

        }
    }*/

    private void createEmptyCollection(String userType, String email) {
        Index index = new Index();
        index.setUid(mAuth.getUid());
        index.setUserType(userType);
        index.setEmail(email);
        index.setProfileCompleted(false);
        fireStore = FirebaseFirestore.getInstance();
        fireStore.collection(DbNode.INDEX.toString()).document(DbNode.COUNTER.toString())
                .update(Counter.USERCOUNTER.toString(), FieldValue.increment(1));
        fireStore.collection(DbNode.INDEX.toString()).document(index.getUid()).set(index);
    }

    private void setSnackBar(View root, String title, boolean navigate) {
        //navigate only if its true
        final Snackbar snackbar = Snackbar.make(root, title, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", view -> {
            //navigate to email verification
            if (navigate) {
               /* NavDirections action =
                        SignUpDirections.actionSignUpToSignIn();
                Navigation.findNavController(view).navigate(action);*/
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack();
            } else {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }


    private String getText(TextInputLayout til) {
        if (til.getEditText() != null) {
            return til.getEditText().getText().toString();
        } else {
            til.setError(MyConstants.EMPTY_FIELD);
            return null;
        }

    }


    private void setError(TextInputLayout textInputLayout) {
        if (getText(textInputLayout) != null) {
            textInputLayout.setError(MyConstants.EMPTY_FIELD);
        } else {
            textInputLayout.setError(null);
        }

    }

    private void setError(TextInputLayout textInputLayout, String error, boolean setError) {
        if (setError) {
            textInputLayout.setError(error);
        } else {
            textInputLayout.setError(null);
        }

    }


}