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
import com.example.vaccination.data.Request;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyMessage;
import com.example.vaccination.ui.mainscreen.hospital.adapter.RecordAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class UserRecord extends Fragment {

    private FirebaseAuth auth;
    private MyMessage message;
    private LoadingDialog loadingDialog;
    private FirebaseUtils firebaseUtils;

    public UserRecord() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUtils = new FirebaseUtils(db);
        message = new MyMessage(getContext(), "Failed to perform given action");
        loadingDialog = new LoadingDialog(getContext(), "Loading");
        loadingDialog.show();
        init(view);
    }

    private void init(View root) {
        FirebaseListData firebaseListData = (list, success) -> {
            if (success) {
                List<Request> requests = (List<Request>) list;
                setupRecycler(root, requests);
            } else {
                message.showToastMessage();
            }
            loadingDialog.dismiss();
        };
        //getData(firebaseListData);
        firebaseUtils.getRecordNodeData("userId", auth.getUid(), firebaseListData);
    }


    private void setupRecycler(View root, List<Request> list) {
        RecordAdapter recordAdapter = new RecordAdapter(getContext(), list);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewUserRecord);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recordAdapter);
    }
}