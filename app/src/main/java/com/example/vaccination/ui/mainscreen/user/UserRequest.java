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
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Request;
import com.example.vaccination.data.UserType;
import com.example.vaccination.data.VaccineStatus;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myInterface.MyRecyclerClickListener;
import com.example.vaccination.myInterface.MyTaskCallback;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyMessage;
import com.example.vaccination.ui.mainscreen.hospital.adapter.RequestAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRequest extends Fragment {


    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MyMessage message;
    private LoadingDialog loadingDialog;

    private List<Request> requestList;
    private RequestAdapter requestAdapter;

    public UserRequest() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getContext(), "Loading");
        message = new MyMessage(getContext(), MyConstants.DB_FAILED);
        init(view);
    }

    private void init(View root) {
        loadingDialog.show();

        FirebaseListData listener = (list, success) -> {
            if (success) {
                requestList = (List<Request>) list;
                setupRecycler(root, requestList);
            } else {
                message.showToastMessage(MyConstants.DB_FAILED);
            }
            loadingDialog.dismiss();
        };


        getData(auth.getUid(), listener);

    }

    //updated
    private void getData(String userUid, FirebaseListData firebaseListData) {
        db.collection(DbNode.REQUEST.toString()).whereEqualTo("userId", userUid)
                .limit(100).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Request> requestList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    requestList.add(document.toObject(Request.class));
                }
                firebaseListData.dataReceived(requestList, true);
            } else {
                firebaseListData.dataReceived(null, false);
            }
        }).addOnFailureListener(e -> firebaseListData.dataReceived(null, false));
    }

    private void setupRecycler(View root, List<Request> requests) {
        MyRecyclerClickListener positiveClick = position -> {
        };
        MyRecyclerClickListener negativeClick = position -> updateVaccineStatusUser(false, requestList.get(position), position);


        requestAdapter = new RequestAdapter(requests, positiveClick, negativeClick, true);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewUserRequest);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(requestAdapter);
    }


    private void updateVaccineStatusUser(boolean approved, Request request, int position) {
        if (approved) {
            request.setStatus(VaccineStatus.COMPLETED.toString());
        } else {
            request.setStatus(VaccineStatus.CANCELLED.toString());
            request.setCancelledBy(UserType.User.toString());
        }
        moveFromRequestToRecord(request, position);


    }

    private void moveFromRequestToRecord(Request request, int position) {
        MyTaskCallback dataUpdated = status -> {
            if (status) {
                requestList.remove(request);
                requestAdapter.notifyItemRemoved(position);
            } else {
                message.showToastMessage("Some Error occurred while cancelling");
            }
        };
        moveFromRequestToRecord(request, dataUpdated);
    }


    private void moveFromRequestToRecord(Request request, MyTaskCallback callback) {
        MyTaskCallback deleteDoc = taskSuccess -> {
            if (taskSuccess) {
                db.collection(DbNode.RECORD.toString())
                        .document(request.getRequestId()).set(request)
                        .addOnCompleteListener(task -> {
                            callback.result(task.isSuccessful());
                        });
            } else
                callback.result(false);
        };
        db.collection(DbNode.REQUEST.toString())
                .document(request.getRequestId()).delete()
                .addOnCompleteListener(task -> {
                    deleteDoc.result(task.isSuccessful());
                });
    }


}