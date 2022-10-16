package com.example.vaccination.ui.mainscreen.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vaccination.R;
import com.example.vaccination.data.Counter;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.HospitalCollection;
import com.example.vaccination.data.Request;
import com.example.vaccination.data.VaccineStatus;
import com.example.vaccination.myInterface.FirebaseDataUpdated;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myInterface.MyRecyclerClickListener;
import com.example.vaccination.myInterface.MyTaskCallback;
import com.example.vaccination.myInterface.FirebaseQueryCallback;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyMessage;
import com.example.vaccination.ui.mainscreen.hospital.adapter.RequestAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class AdminRequest extends Fragment {

    private static final String TAG = "AdminRequest";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MyMessage message;
    private LoadingDialog loadingDialog;
    private List<Request> requestList;
    private RequestAdapter requestAdapter;
    private FirebaseUtils firebaseUtils;

    public AdminRequest() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        message = new MyMessage(getContext(), "Failed to perform given action");
        loadingDialog = new LoadingDialog(getContext(), "Loading");
        firebaseUtils = new FirebaseUtils(db);
        //Request[] requests= (Request[]) getArguments().getParcelableArray("requestList");
        init(view);
    }

    private void init(View root) {
        //setupRecycler(root, Arrays.asList(requests));
        loadingDialog.show();
        FirebaseQueryCallback firebaseQueryCallback = (success, list, query) -> {
            if (success) {
                requestList = (List<Request>) list;
                requestList = requestList.stream().filter(request -> request.getStatus()
                        .equalsIgnoreCase(VaccineStatus.WAITING.toString()))
                        .collect(Collectors.toList());
                setupRecycler(root, requestList);
            } else {
                message.showToastMessage(MyConstants.DB_FAILED);
            }
            loadingDialog.dismiss();
        };

        firebaseUtils.getAllRequestNodeData(firebaseQueryCallback);
        //getData(listener);

    }

    //make firebase util class with method "get data from node with uid" , move data , change
    // ststus,sort list data

    @Deprecated
    private void getData(FirebaseListData listener) {
        db.collection(DbNode.ADMIN.toString()).document(auth.getUid())
                .collection(HospitalCollection.requestList.toString()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Request> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Request request = document.toObject(Request.class);
                            requests.add(request);
                        }
                        listener.dataReceived(requests, true);
                    } else {
                        message.showToastMessage(MyConstants.DB_FAILED);
                        listener.dataReceived(null, false);
                    }

                })
                .addOnFailureListener(e -> {
                    message.showToastMessage(MyConstants.DB_FAILED);
                    listener.dataReceived(null, false);
                });
    }

    private void setupRecycler(View root, List<Request> requests) {
        MyRecyclerClickListener positiveClick = position -> {
            updateRequestStatusAdmin(true, requestList.get(position), position);
        };
        MyRecyclerClickListener negativeClick = position -> {
            updateRequestStatusAdmin(false, requestList.get(position), position);
        };


        requestAdapter = new RequestAdapter(requests, positiveClick, negativeClick);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewAdminRequest);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(requestAdapter);
    }


    private void updateRequestStatusAdmin(boolean approved, Request request, int position) {
        FirebaseDataUpdated updateRequestCounter = (taskSuccess,e) -> {
            if (taskSuccess) {
                requestList.remove(request);
                requestAdapter.notifyItemRemoved(position);
                message.showToastMessage("Vaccine Approved Successfully");
            } else {
                message.showToastMessage();
            }
        };

        FirebaseDataUpdated requestStatusUpdated = (status,e) -> {
            if (status) {
                if (approved) {
                    updateHospitalRequestCounter(updateRequestCounter, 1, request.getHospitalId());
                } else {
                    moveFromRequestToRecord(request, position);
                }
            } else {
                Log.d(TAG, "updateRequestStatusAdmin: "+e);
                message.showToastMessage();
            }
        };

        if (approved) {
            request.setStatus(VaccineStatus.APPROVED.toString());
        } else {
            request.setStatus(VaccineStatus.DENIED.toString());
        }

        firebaseUtils.updateRequestStatus(request, requestStatusUpdated);
    }

    private void moveFromRequestToRecord(Request request, int position) {

        FirebaseDataUpdated adminRequestCounterDecremented = (status,e) -> {
            if (status) {
                requestList.remove(request);
                requestAdapter.notifyItemRemoved(position);
                message.showToastMessage("Request Denied Successfully");
            } else {
                message.showToastMessage();
            }
        };


        FirebaseDataUpdated hospitalRequestIncremented = (status,e) -> {
            if (status) {
                requestCounterAdmin(adminRequestCounterDecremented, -1);
            } else {
                message.showToastMessage();
            }
        };


        MyTaskCallback requestMovedCallback = status -> {
            if (status) {
                incrementHospitalRequestCounter(request.getHospitalId(), hospitalRequestIncremented);
            } else {
                message.showToastMessage();
            }
        };

        firebaseUtils.moveFromRequestToRecord(request, requestMovedCallback);


    }

    /*private void addRequestToHospital(Request request, MyTaskCallback listener) {
        DocumentReference ref = db.collection(DbNode.HOSPITAL.toString())
                .document(request.getHospitalId())
                .collection(HospitalCollection.requestList.toString())
                .document();
        request.setRequestId(ref.getId());
        ref.set(request).addOnCompleteListener(task -> {
            listener.result(task.isSuccessful());
        }).addOnFailureListener(e -> {
            listener.result(false);
        });
    }*/

    private void incrementHospitalRequestCounter(String uid, FirebaseDataUpdated dataUpdated) {
        db.collection(DbNode.HOSPITAL.toString()).document(uid)
                .update("requestCounter", FieldValue.increment(1))
                .addOnCompleteListener(task -> {
                    dataUpdated.dataUpdated(task.isSuccessful(),null);
                }).addOnFailureListener(e -> {
            dataUpdated.dataUpdated(false,e);
        });
    }

   /* private void moveFromRequestToRecordAdmin(Request request, MyTaskCallback callback) {

        MyTaskCallback deleteDoc = taskSuccess -> {
            if (taskSuccess) {
                db.collection(DbNode.ADMIN.toString()).document(auth.getUid())
                        .collection(HospitalCollection.recordList.toString())
                        .document(request.getRequestId()).set(request)
                        .addOnCompleteListener(task -> {
                            callback.result(task.isSuccessful());
                        });
            } else {
                message.showToastMessage();
            }
        };
        db.collection(DbNode.HOSPITAL.toString()).document(auth.getUid())
                .collection(HospitalCollection.requestList.toString())
                .document(request.getRequestId()).delete()
                .addOnCompleteListener(task -> {
                    deleteDoc.result(task.isSuccessful());
                });
    }*/

    //increment vaccineCounter
    private void UserVaccinatedCounter(FirebaseDataUpdated dataUpdated) {
        db.document(DbNode.COUNTER.toString())
                .update(Counter.USERCOUNTER.toString(), FieldValue.increment(1))
                .addOnCompleteListener(task -> {
                    dataUpdated.dataUpdated(task.isSuccessful(),null);
                }).addOnFailureListener(e -> {
            dataUpdated.dataUpdated(false,e);
        });
    }

    //decrement requestCounter
    private void requestCounterAdmin(FirebaseDataUpdated dataUpdated, int value) {
        db.collection(DbNode.INDEX.toString()).document(DbNode.COUNTER.toString())
                .update(Counter.REQUESTCOUNTER.toString(), FieldValue.increment(value))
                .addOnCompleteListener(task -> {
                    dataUpdated.dataUpdated(task.isSuccessful(),null);
                }).addOnFailureListener(e -> {
            dataUpdated.dataUpdated(false,e);
        });
    }

    private void updateHospitalRequestCounter(FirebaseDataUpdated dataUpdated, int value,
                                              String hospitalUid) {

        db.collection(DbNode.HOSPITAL.toString())
                .document(hospitalUid)
                .update("requestCounter", FieldValue.increment(value))
                .addOnCompleteListener(task -> {
                    dataUpdated.dataUpdated(task.isSuccessful(),null);
                }).addOnFailureListener(e -> {
            dataUpdated.dataUpdated(false,e);
        });
    }

}