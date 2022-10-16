package com.example.vaccination.ui.mainscreen.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.HospitalCollection;
import com.example.vaccination.data.Request;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myInterface.FirebaseQueryCallback;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyMessage;
import com.example.vaccination.ui.mainscreen.hospital.adapter.RecordAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class AdminRecords extends Fragment {

    //private FirebaseAuth auth;
    private MyMessage message;
    private LoadingDialog loadingDialog;
    private FirebaseUtils firebaseUtils;

    public AdminRecords() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //auth = FirebaseAuth.getInstance();
        firebaseUtils = new FirebaseUtils(db);
        message = new MyMessage(getContext(), "Failed to perform given action");
        loadingDialog = new LoadingDialog(getContext(), "Loading");
        loadingDialog.show();
        init(view);
    }

    private void init(View root) {
        FirebaseQueryCallback queryCallback = (success, list, query) -> {
            if (success) {
                List<Request> requests = (List<Request>) list;
                setupRecycler(root, requests);
            } else {
                message.showToastMessage();
            }
            loadingDialog.dismiss();
        };
        //firebaseUtils.getRecordNodeData(,auth.getUid(), firebaseListData);
        firebaseUtils.getAllRecordNodeData(queryCallback);
    }

    /*@Deprecated
    private void getData(FirebaseListData listener) {
        db.collection(DbNode.ADMIN.toString()).document(auth.getUid())
                .collection(HospitalCollection.recordList.toString())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Request> requests = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Request request = document.toObject(Request.class);
                    requests.add(request);
                }
                listener.dataReceived(requests, true);
            } else {
                listener.dataReceived(null, true);
            }

        }).addOnFailureListener(e -> listener.dataReceived(null, true));
    }
*/

    private void setupRecycler(View root, List<Request> list) {
        RecordAdapter recordAdapter = new RecordAdapter(getContext(), list);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewAdminRecord);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recordAdapter);
    }
}