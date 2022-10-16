package com.example.vaccination.ui.mainscreen.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vaccination.R;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.User;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseQueryCallback;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyCurrentUser;
import com.example.vaccination.myutils.MyMessage;
import com.example.vaccination.ui.mainscreen.user.adapter.HospitalAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class AllHospitals extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUtils firebaseUtils;
    private MyMessage message;
    private LoadingDialog loadingDialog;

    public AllHospitals() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_hospitals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUtils = new FirebaseUtils(db);
        message = new MyMessage(getContext(), MyConstants.DB_FAILED);
        loadingDialog = new LoadingDialog(getContext(), "Loading...");
        init(view);
    }

    private void init(View root) {


        FirebaseQueryCallback loadAllHospitalCallback = (success, list, query) -> {
            if (success) {
                List<Hospital> hospitalList = (List<Hospital>) list;
                setUpRecyclerView(root, hospitalList);
            } else {
                message.showToastMessage();
            }
        };

        FirebaseDataReceived getUserCallback = (object, success) -> {
            if (success) {
                User user = (User) object;
                MyCurrentUser.setUser(user);
                firebaseUtils.getAllHospitalWithDistance(user, loadAllHospitalCallback);
            } else {
                message.showToastMessage();
            }
        };

        firebaseUtils.getUser(auth.getUid(),getUserCallback);

    }


    private void setUpRecyclerView(View root, List<Hospital> list) {
        HospitalAdapter hospitalAdapter = new HospitalAdapter(getContext(), list, null);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewAllHospitalsUser);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(hospitalAdapter);
    }
}