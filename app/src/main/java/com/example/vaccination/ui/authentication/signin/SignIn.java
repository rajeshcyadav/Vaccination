package com.example.vaccination.ui.authentication.signin;

import static com.example.vaccination.myutils.MyConstants.CANCELLED_BY_USER;
import static com.example.vaccination.myutils.MyConstants.EMPTY_FIELD;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Index;
import com.example.vaccination.myInterface.MyFirebaseAuthorizationCallback;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyPref;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.regex.Pattern;

public class SignIn extends Fragment {

    private static final String TAG = "MySignIn";
    private SignInViewModel mViewModel;
    private FirebaseAuth mAuth;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private LoadingDialog loadingDialog;
    private String userType;
    private FirebaseFirestore db;
    private CoordinatorLayout coordinatorLayout;
    MyPref myPref;

    public static SignIn newInstance() {
        return new SignIn();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_in_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getContext(), "SignIn...");
        loadingDialog.setCancelable(false);
        myPref = new MyPref(getContext(), getString(R.string.pref));

        init(view);

    }

    private void init(View root) {
        TextInputLayout email = root.findViewById(R.id.inputEmail);
        TextInputLayout password = root.findViewById(R.id.inputPassword);
        root.findViewById(R.id.textViewRegisterLink).setOnClickListener(
                view -> {
                    NavDirections action =
                            SignInDirections.actionSignInToSignUp();
                    Navigation.findNavController(view).navigate(action);
                });
        root.findViewById(R.id.buttonLogin).setOnClickListener(view -> {
            if (validate(email, password))
                signIn(email, password);
        });
        root.findViewById(R.id.textView4).setOnClickListener(view -> {
            NavDirections action =
                    SignInDirections.actionSignInToForgotPassword();
            Navigation.findNavController(view).navigate(action);
        });

        coordinatorLayout = root.findViewById(R.id.signInCoordRoot);

        materialButtonToggleGroup =
                root.findViewById(R.id.materialButtonToggleGroup);

        if (isUserAvailable()) {
            String userType = myPref.readPref(getString(R.string.prefUsertype), null);
            Log.d(TAG, "init: " + userType);
            if (userType != null) {
                navigate(userType);
            }
        }


    }

    private boolean validate(TextInputLayout email, TextInputLayout password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern p = Pattern.compile(regex);

        if (email != null) {
            if (TextUtils.isEmpty(email.getEditText().getText())) {
                email.setError(EMPTY_FIELD);
                return false;
            } else
                email.setError(null);

        } else if (password != null) {
            if (TextUtils.isEmpty(password.getEditText().getText())) {
                password.setError(EMPTY_FIELD);
                return false;
            } else
                password.setError(null);
        }
        return true;

    }


    private void signIn(TextInputLayout email, TextInputLayout password) {
        loadingDialog.show();
        MyFirebaseAuthorizationCallback callback = (result, profileSetup) -> {
            NavDirections action = null;
            MyPref myPref = new MyPref(getContext(), getString(R.string.pref));
            if (result) {
                if (userType.equals(DbNode.USER.toString())) {
                    if (profileSetup) {
                        action = SignInDirections.actionSignInToUserMain();
                    } else {
                        action = SignInDirections.actionSignInToUserProfile();
                    }

                    myPref.addPref(getString(R.string.prefUsertype), DbNode.USER.toString());

                } else if (userType.equals(DbNode.HOSPITAL.toString())) {
                    if (profileSetup) {
                        action = SignInDirections.actionSignInToHospitalMain();
                    } else {
                        action = SignInDirections.actionSignInToHospitalProfile();
                    }

                    myPref.addPref(getString(R.string.prefUsertype), DbNode.HOSPITAL.toString());

                } else if (userType.equals(DbNode.ADMIN.toString())) {
                    if (profileSetup) {
                        action = SignInDirections.actionSignInToAdminMain();
                    } else {
                        action = SignInDirections.actionSignInToUserProfile().setUserType(DbNode.ADMIN);
                    }

                    myPref.addPref(getString(R.string.prefUsertype), DbNode.ADMIN.toString());

                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

                }

                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);

            } else {
                //inversePref(userType);
                Toast.makeText(getContext(), getContext().getResources().getText(R.string.NotAuthorisedSignin),
                        Toast.LENGTH_SHORT).show();
            }
        };


        setUserAccount(materialButtonToggleGroup);
        String emailString = email.getEditText().getText().toString();
        String passwordString = password.getEditText().getText().toString();

        mAuth.signInWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (isVerified()) {
                            //isAuthorized(userType, callback);
                            isUserAuthorized(userType, callback);
                        } else {
                            Toast.makeText(getContext(), "Please check your email for Verification Link",
                                    Toast.LENGTH_LONG).show();
                            String title = "Email not Verified";
                            setSnackBar(coordinatorLayout, title);
                        }

                    } else {
                        Toast.makeText(getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                })
                .addOnCanceledListener(() -> {
                    Toast.makeText(getContext(), CANCELLED_BY_USER, Toast.LENGTH_SHORT).show();
                });

    }

    private void setUserAccount(MaterialButtonToggleGroup group) {
        if (group.getCheckedButtonId() == R.id.buttonHospitalSignIn) {
            userType = DbNode.HOSPITAL.toString();
        } else if (group.getCheckedButtonId() == R.id.buttonAdminSignIn) {
            userType = DbNode.ADMIN.toString();
        } else {
            userType = DbNode.USER.toString();

        }
    }

  /*  private void isAuthorized(String userType, MyFirebaseAuthorizationCallback callback) {
        if (mAuth.getCurrentUser().getUid() == null) {
            Toast.makeText(getContext(), "Failed to verify", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection(userType).document(mAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                callback.isAuthorised(document.exists());
            } else {

            }
        }).addOnCanceledListener(() -> Toast.makeText(getContext(), "Log in Cancelled",
                        Toast.LENGTH_SHORT).show());
    }*/


    private void isUserAuthorized(String userType, MyFirebaseAuthorizationCallback callback) {
        db.collection(DbNode.INDEX.toString()).whereEqualTo("uid", mAuth.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Index index = document.toObject(Index.class);
                            callback.isAuthorised(index.getUserType().equalsIgnoreCase(userType), index.isProfileCompleted());
                            break;
                        }
                    } else {
                        callback.isAuthorised(false, false);
                    }
                }).addOnFailureListener(e -> {
            callback.isAuthorised(false, false);
        });
    }

    private boolean isVerified() {
        //TODO: 20-09-2021 Change me if you want to enable Email verification
        //return mAuth.getCurrentUser().isEmailVerified();
        return true;
    }

    private void setSnackBar(View root, String title) {
        Snackbar.make(root, title, Snackbar.LENGTH_INDEFINITE)
                .setAction("Verify", view -> {
                    NavDirections action =
                            SignInDirections.actionSignInToEmailVerification();
                    Navigation.findNavController(view).navigate(action);
                })
                .show();
    }

    private void addPref(String userType) {

        if (userType.equals(DbNode.ADMIN.toString()))
            myPref.addPref(getString(R.string.prefUsertype), DbNode.ADMIN.toString());
        else if (userType.equals(DbNode.HOSPITAL.toString()))
            myPref.addPref(getString(R.string.prefUsertype), DbNode.HOSPITAL.toString());
        else
            myPref.addPref(getString(R.string.prefUsertype), DbNode.USER.toString());


    }


    private boolean isUserAvailable() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return user != null;
    }

    private void navigate(String userType) {
        MyFirebaseAuthorizationCallback callback = (result, profileSetup) -> {
            NavDirections action = null;
            MyPref myPref = new MyPref(getContext(), getString(R.string.pref));
            if (result) {
                if (userType.equals(DbNode.USER.toString())) {
                    if (profileSetup) {
                        action = SignInDirections.actionSignInToUserMain();
                    } else {
                        action = SignInDirections.actionSignInToUserProfile();
                    }

                    myPref.addPref(getString(R.string.prefUsertype), DbNode.USER.toString());

                } else if (userType.equals(DbNode.HOSPITAL.toString())) {
                    if (profileSetup) {
                        action = SignInDirections.actionSignInToHospitalMain();
                    } else {
                        action = SignInDirections.actionSignInToHospitalProfile();
                    }

                    myPref.addPref(getString(R.string.prefUsertype), DbNode.HOSPITAL.toString());

                } else if (userType.equals(DbNode.ADMIN.toString())) {
                    if (profileSetup) {
                        action = SignInDirections.actionSignInToAdminMain();
                    } else {
                        action = SignInDirections.actionSignInToUserProfile().setUserType(DbNode.ADMIN);
                    }

                    myPref.addPref(getString(R.string.prefUsertype), DbNode.ADMIN.toString());

                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

                }

                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);


            } else {
                //inversePref(userType);
                Toast.makeText(getContext(), getContext().getResources().getText(R.string.NotAuthorisedSignin),
                        Toast.LENGTH_SHORT).show();
            }
        };
        isUserAuthorized(userType, callback);
    }


}