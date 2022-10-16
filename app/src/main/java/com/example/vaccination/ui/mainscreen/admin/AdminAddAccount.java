package com.example.vaccination.ui.mainscreen.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vaccination.R;
import com.example.vaccination.data.Counter;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Index;
import com.example.vaccination.myInterface.FirebaseDataUpdated;
import com.example.vaccination.myInterface.MyTaskCallback;
import com.example.vaccination.myInterface.MyTaskWithData;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyAuth;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


public class AdminAddAccount extends Fragment {

    private FirebaseAuth auth;
    private FirebaseAuth tempAuth;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;
    private MyMessage message;


    public AdminAddAccount() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_admin_add_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tempAuth = new MyAuth(getContext(), "eVaccination").getAuth();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getContext(), "Creating Account");
        loadingDialog.setCancelable(false);
        message = new MyMessage(getContext(), MyConstants.DB_FAILED);
        init(view);
    }

    @SuppressLint("SetTextI18n")
    private void init(View root) {
        TextInputLayout textInputLayoutEmail = root.findViewById(R.id.outlinedTextFieldEmail);
        TextInputLayout textInputLayoutPassword = root.findViewById(R.id.outlinedTextFieldPassword);
        TextInputLayout textInputLayoutConfirmPassword = root.findViewById(R.id.outlinedTextFieldConfirmPassword);
        MaterialButtonToggleGroup mbtg = root.findViewById(R.id.materialButtonToggleGroupAdmin);
        Button buttonCreateUser = root.findViewById(R.id.buttonCreateUserAdmin);
        TextView title = root.findViewById(R.id.textViewCreateAccountTitle);
        TextInputLayout[] layouts = {textInputLayoutEmail, textInputLayoutPassword, textInputLayoutConfirmPassword};


        mbtg.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (checkedId == R.id.buttonHospitalAdmin) {
                title.setText("Create Hospital Account");
            } else {
                title.setText("Create User Account");
            }
        });

        MyTaskCallback createEmptyCollectionTask = taskSuccess -> {
            if (taskSuccess) {
                message.showToastMessage("Account created Successfully");
                clearInputs(layouts);
            } else {
                message.showToastMessage("Error Occurred While Creating Account");

            }
            loadingDialog.dismiss();
        };

        MyTaskWithData createUserTask = (taskSuccess,data) -> {
            if (taskSuccess) {

                tempAuth.signOut();
                createEmptyCollection(getTextFromToggle(mbtg),
                        getText(textInputLayoutEmail),
                        createEmptyCollectionTask,data);
            }

            if(loadingDialog.isShowing())
                loadingDialog.dismiss();

        };

        buttonCreateUser.setOnClickListener(view -> {
            if (!isEmpty(layouts)) {
                if (isValidEmail(textInputLayoutEmail.getEditText().getText())) {
                    if (isEqual(textInputLayoutConfirmPassword, textInputLayoutPassword)) {
                        loadingDialog.show();
                        createUser(getText(textInputLayoutEmail),
                                getText(textInputLayoutPassword),
                                createUserTask
                        );
                        Log.d("MyAdmin", "init: tempAuth "+tempAuth.getUid());
                        Log.d("MyAdmin", "init: tempAuth "+tempAuth.toString());
                    }
                }
            }
        });

    }

    private void clearInputs(TextInputLayout[] layouts) {
        for (TextInputLayout layout : layouts) {
            layout.getEditText().setText("");
        }
    }


    public String getTextFromToggle(MaterialButtonToggleGroup mbtg) {
        if (mbtg.getCheckedButtonId() == R.id.buttonHospitalAdmin) {
            return DbNode.HOSPITAL.toString();
        } else {
            return DbNode.USER.toString();
        }
    }

    public String getText(TextInputLayout t1) {
        return t1.getEditText().getText().toString();
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isEmpty(TextInputLayout[] layouts) {
        for (TextInputLayout layout : layouts) {
            if (layout.getEditText().getText().toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isEqual(TextInputLayout t1, TextInputLayout t2) {
        return t1.getEditText().getText().toString().equals(t1.getEditText().getText().toString());
    }

    private void createUser(String email, String password, MyTaskWithData listener) {
        tempAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.result(true,task.getResult().getUser().getUid());
            } else {
                listener.result(false,null);
            }
        }).addOnFailureListener(e -> {
            message.showToastMessage(e.getMessage());
            listener.result(false,null);
        });
    }

    private void createEmptyCollection(String userType, String email, MyTaskCallback listener,String uid) {
        Index index = new Index();
        index.setUid(uid);
        index.setUserType(userType);
        index.setEmail(email);
        index.setProfileCompleted(false);

        db.collection(DbNode.INDEX.toString()).document(index.getUid()).set(index)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){

                        db.collection(DbNode.INDEX.toString()).document(DbNode.COUNTER.toString())
                                .update(Counter.USERCOUNTER.toString(), FieldValue.increment(1))
                                .addOnCompleteListener(task1 -> {
                                    listener.result(true);
                                });

                    }
                    else{
                        listener.result(false);
                    }

                })
                .addOnFailureListener(e -> {
                    listener.result(false);
                });
    }
}