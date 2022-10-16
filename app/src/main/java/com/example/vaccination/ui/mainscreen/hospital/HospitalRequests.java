package com.example.vaccination.ui.mainscreen.hospital;

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
import com.example.vaccination.data.VaccineStatus;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseDataUpdated;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myInterface.MyRecyclerClickListener;
import com.example.vaccination.myInterface.MyTaskCallback;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyMessage;
import com.example.vaccination.ui.mainscreen.hospital.adapter.RequestAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SuppressWarnings({"ConstantConditions", "unchecked"})
public class HospitalRequests extends Fragment {

    private FirebaseAuth auth;
    private MyMessage message;
    private LoadingDialog loadingDialog;
    private FirebaseUtils firebaseUtils;

    private List<Request> requestList;
    private RequestAdapter requestAdapter;

    public HospitalRequests() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hospital_requests, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getContext(), "Loading");
        message = new MyMessage(getContext(), "Failed to perform given action");
        firebaseUtils = new FirebaseUtils(db);
        init(view);
    }

    private void init(View root) {
        loadingDialog.show();

        FirebaseListData listener = (list, success) -> {
            if (success) {
                requestList = (List<Request>) list;
                requestList = requestList.stream().filter(request -> request.getStatus()
                        .equalsIgnoreCase(VaccineStatus.APPROVED.toString()))
                        .collect(Collectors.toList());
                setupRecycler(root, requestList);
            } else {
                message.showToastMessage(MyConstants.DB_FAILED);
            }
            loadingDialog.dismiss();
        };

        firebaseUtils.getRequestNodeData(auth.getUid(), "hospitalId", listener);


    }

    private void setupRecycler(View root, List<Request> requests) {
        MyRecyclerClickListener positiveClick = position -> updateVaccineStatusHospital(true, requestList.get(position), position);
        MyRecyclerClickListener negativeClick = position -> updateVaccineStatusHospital(false, requestList.get(position), position);

        requestAdapter = new RequestAdapter(requests, positiveClick, negativeClick);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewHospitalRequest);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(requestAdapter);
    }


    private void updateVaccineStatusHospital(boolean approved, Request request, int position) {
        FirebaseDataUpdated vaccineRequestUpdated = (status,e) -> {
            if (status) {
                if (approved) {
                    request.setStatus(VaccineStatus.COMPLETED.toString());
                } else {
                    request.setStatus(VaccineStatus.DENIED.toString());
                }
                moveFromRequestToRecord(request, position);
            } else
                message.showToastMessage();
        };
        FirebaseDataUpdated vaccinatedUserUpdated = (status,e) -> {
            if (status) {
                firebaseUtils.vaccineRequestCounter(request.getHospitalId(), vaccineRequestUpdated);
            } else
                message.showToastMessage();
        };

        FirebaseDataUpdated vaccineCountUpdated = (status,e) -> {
            if (status) {
                firebaseUtils.vaccinatedUserCounter(request.getHospitalId(), vaccinatedUserUpdated);
            } else
                message.showToastMessage();
        };

        FirebaseDataReceived vaccineStockCallback = (count, isSuccess) -> {
            if (isSuccess) {
                long vaccineCount = (long) count;
                if (vaccineCount > 0) {
                    firebaseUtils.updateVaccineCount(request.getVaccineId(), -1, vaccineCountUpdated);
                } else {
                    message.showToastMessage("Vaccine Not in stock");
                }
            }
        };
        firebaseUtils.getVaccineStock(request.getVaccineId(), vaccineStockCallback);
    }

    private void moveFromRequestToRecord(Request request, int position) {
        MyTaskCallback moveTaskCallback = taskSuccess -> {
            if (taskSuccess) {
                requestList.remove(request);
                requestAdapter.notifyItemRemoved(position);
            } else {
                message.showToastMessage();
            }
        };

        firebaseUtils.moveFromRequestToRecord(request, moveTaskCallback);
    }

}