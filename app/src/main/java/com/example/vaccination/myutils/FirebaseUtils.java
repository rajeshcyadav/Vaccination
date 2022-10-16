package com.example.vaccination.myutils;

import android.util.Log;

import com.example.vaccination.data.AdminCounter;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.Request;
import com.example.vaccination.data.User;
import com.example.vaccination.data.Vaccine;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseDataUpdated;
import com.example.vaccination.myInterface.FirebaseDataUploaded;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myInterface.FirebaseQueryCallback;
import com.example.vaccination.myInterface.MyTaskCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("CodeBlock2Expr")
public final class FirebaseUtils {

    private static final String TAG = "FirebaseUtils";
    private final FirebaseFirestore db;

    public FirebaseUtils(FirebaseFirestore db) {
        this.db = db;
    }

    public void getRequestNodeData(String value, String field, FirebaseListData firebaseListData) {
        db.collection(DbNode.REQUEST.toString()).whereEqualTo(field, value)
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

    public void getAllRequestNodeData(FirebaseQueryCallback queryCallback) {
        Query first = db.collection(DbNode.REQUEST.toString()).limit(100);
        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Request> requestList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    requestList.add(document.toObject(Request.class));
                }
                if (task.getResult().size() > 0) {
                    DocumentSnapshot lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);

                    Query next = db.collection(DbNode.REQUEST.toString())
                            .startAfter(lastVisible)
                            .limit(100);

                    queryCallback.onResult(true, requestList, next);
                } else {
                    queryCallback.onResult(true, requestList, null);
                }
            } else {
                queryCallback.onResult(false, null, null);
            }
        }).addOnFailureListener(e -> queryCallback.onResult(false, null, null));

    }

    public void moveFromRequestToRecord(Request request, MyTaskCallback callback) {
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

    public void updateVaccineCount(String vaccineUid, long value, FirebaseDataUpdated firebaseDataUpdated) {
        db.collection(DbNode.VACCINE.toString()).document(vaccineUid)
                .update("vaccinatedCounter", FieldValue.increment(value))
                .addOnCompleteListener(task -> {
                    firebaseDataUpdated.dataUpdated(task.isSuccessful(), null);
                }).addOnFailureListener(e -> {
            firebaseDataUpdated.dataUpdated(false, e);
        });

    }

    public void vaccinatedUserCounter(String hospitalUid, FirebaseDataUpdated dataUpdated) {
        db.collection(DbNode.HOSPITAL.toString()).document(hospitalUid)
                .update("vaccinatedCounter", FieldValue.increment(1))
                .addOnCompleteListener(task -> {
                    dataUpdated.dataUpdated(task.isSuccessful(), null);
                }).addOnFailureListener(e -> {
            dataUpdated.dataUpdated(false, e);
        });
    }


    public void vaccineRequestCounter(String hospitalUid, FirebaseDataUpdated dataUpdated) {
        db.collection(DbNode.HOSPITAL.toString()).document(hospitalUid)
                .update("requestCounter", FieldValue.increment(-1))
                .addOnCompleteListener(task -> {
                    dataUpdated.dataUpdated(task.isSuccessful(), null);
                }).addOnFailureListener(e -> {
            dataUpdated.dataUpdated(false, e);
        });
    }

    public void getVaccineStock(String vaccineUid, FirebaseDataReceived firebaseDataReceived) {
        db.collection(DbNode.VACCINE.toString()).document(vaccineUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Vaccine vaccine = task.getResult().toObject(Vaccine.class);
                        firebaseDataReceived.dataReceived(vaccine.getStock(), true);
                    } else {
                        firebaseDataReceived.dataReceived(null, false);
                    }
                }).addOnFailureListener(e -> {
            firebaseDataReceived.dataReceived(e, false);
        });
    }

    public void isVaccineAvailable(String vaccineUid, MyTaskCallback taskCallback) {
        FirebaseDataReceived firebaseDataReceived = (count, success) -> {
            if (success) {
                long vaccineCount = (long) count;
                taskCallback.result(vaccineCount > 0);
            } else
                taskCallback.result(false);
        };

        getVaccineStock(vaccineUid, firebaseDataReceived);
    }


    public void getRecordNodeData(String field, String userId, FirebaseListData listData) {
        db.collection(DbNode.RECORD.toString()).whereEqualTo(field, userId)
                .limit(100).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Request> requestList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Request request = document.toObject(Request.class);
                            requestList.add(request);
                        }
                        listData.dataReceived(requestList, true);
                    } else {
                        listData.dataReceived(null, false);
                    }
                }).addOnFailureListener(e -> {
            Log.d(TAG, "getRecordNodeData: "+e);
            listData.dataReceived(null, false);
        });
    }

    public void getAllRecordNodeData(FirebaseQueryCallback queryCallback) {
        Query first = db.collection(DbNode.RECORD.toString()).limit(100);
        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Request> requestList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    requestList.add(document.toObject(Request.class));
                }
                if (task.getResult().size() > 0) {
                    DocumentSnapshot lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);

                    Query next = db.collection(DbNode.REQUEST.toString())
                            .startAfter(lastVisible)
                            .limit(100);

                    queryCallback.onResult(true, requestList, next);
                } else {
                    queryCallback.onResult(true, requestList, null);
                }
            } else {
                queryCallback.onResult(false, null, null);
            }
        }).addOnFailureListener(e -> queryCallback.onResult(false, null, null));

    }

    public void getVaccine(String uid, FirebaseDataReceived firebaseDataReceived) {
        db.collection(DbNode.VACCINE.toString()).document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Vaccine vaccine = task.getResult().toObject(Vaccine.class);
                        firebaseDataReceived.dataReceived(vaccine, true);
                    } else {
                        firebaseDataReceived.dataReceived(null, false);
                    }
                }).addOnFailureListener(e -> {
            firebaseDataReceived.dataReceived(null, false);
        });
    }

    public void addRequest(Request request, FirebaseDataUploaded firebaseDataUploaded) {
        db.collection(DbNode.REQUEST.toString()).document(request.getRequestId()).set(request)
                .addOnCompleteListener(task -> {
                    firebaseDataUploaded.dataUploaded(task.isSuccessful());
                }).addOnFailureListener(e -> {
            firebaseDataUploaded.dataUploaded(false);
        });
    }

    public void addVaccine(Vaccine vaccine, FirebaseDataUploaded firebaseDataUploaded) {
        db.collection(DbNode.REQUEST.toString()).document(vaccine.getUid()).set(vaccine)
                .addOnCompleteListener(task -> {
                    firebaseDataUploaded.dataUploaded(task.isSuccessful());
                }).addOnFailureListener(e -> {
            firebaseDataUploaded.dataUploaded(false);
        });
    }

    public void getAdminCounterData(FirebaseDataReceived firebaseDataReceived) {
        db.collection(DbNode.INDEX.toString()).document(DbNode.COUNTER.toString()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AdminCounter counter = task.getResult().toObject(AdminCounter.class);
                        firebaseDataReceived.dataReceived(counter, true);
                    } else {
                        firebaseDataReceived.dataReceived(null, false);
                    }
                }).addOnFailureListener(e -> firebaseDataReceived.dataReceived(e, false));
    }

    public void updateRequestStatus(Request request, FirebaseDataUpdated firebaseDataUpdated) {
        Log.d(TAG, "updateRequestStatus: request model \n" + request.toString());
        db.collection(DbNode.REQUEST.toString()).document(request.getRequestId())
                .update("status", request.getStatus())
                .addOnCompleteListener(task ->
                        firebaseDataUpdated.dataUpdated(task.isSuccessful(), null))
                .addOnFailureListener(e -> firebaseDataUpdated.dataUpdated(false, e));

    }

    public void updateRequest(String field, String value, String uid, FirebaseDataUpdated firebaseDataUpdated) {
        db.collection(DbNode.REQUEST.toString()).document(uid)
                .update(field, value)
                .addOnCompleteListener(task ->
                        firebaseDataUpdated.dataUpdated(task.isSuccessful(), null))
                .addOnFailureListener(e -> firebaseDataUpdated.dataUpdated(false, e));

    }


    public void signOut(FirebaseAuth auth) {
        auth.signOut();
    }

    public void updateAdminCounter(String field, long value, FirebaseDataUpdated firebaseDataUpdated) {
        db.collection(DbNode.INDEX.toString()).document(DbNode.COUNTER.toString()).update(field,
                FieldValue.increment(value))
                .addOnCompleteListener(task ->
                        firebaseDataUpdated.dataUpdated(task.isSuccessful(), null))
                .addOnFailureListener(e -> firebaseDataUpdated.dataUpdated(false, e));
    }

    public void verify(AdminCounter adminCounter) {
        if (adminCounter.getVALUE() < 1000)
            throw new Error();
    }

    public void getAllHospital(FirebaseQueryCallback queryCallback) {
        Query first = db.collection(DbNode.HOSPITAL.toString()).limit(50);
        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Hospital> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.toObject(Hospital.class));
                }
                if (task.getResult().size() > 0) {
                    DocumentSnapshot lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);

                    Query next = db.collection(DbNode.REQUEST.toString())
                            .startAfter(lastVisible)
                            .limit(100);

                    queryCallback.onResult(true, list, next);
                } else {
                    queryCallback.onResult(true, list, null);
                }
            } else {
                queryCallback.onResult(false, null, null);
            }
        }).addOnFailureListener(e -> queryCallback.onResult(false, null, null));
    }

    public void getUser(String uid, FirebaseDataReceived firebaseDataReceived) {
        db.collection(DbNode.USER.toString()).document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        firebaseDataReceived.dataReceived(user, true);
                    } else {
                        firebaseDataReceived.dataReceived(null, false);
                    }
                }).addOnFailureListener(e -> {
            firebaseDataReceived.dataReceived(null, false);
        });
    }

    public void getAllHospitalWithDistance(User user, FirebaseQueryCallback queryCallback) {
        Query first = db.collection(DbNode.HOSPITAL.toString()).limit(50);
        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Hospital> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Hospital hospital = document.toObject(Hospital.class);
                    hospital.setDistanceFromUser(Haversine.getHaversine(user.getLatitude(), user.getLongitude(),
                            hospital.getLatitude(), hospital.getLongitude()));
                    list.add(hospital);
                }
                if (task.getResult().size() > 0) {
                    DocumentSnapshot lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);

                    Query next = db.collection(DbNode.REQUEST.toString())
                            .startAfter(lastVisible)
                            .limit(100);

                    queryCallback.onResult(true, list, next);
                } else {
                    queryCallback.onResult(true, list, null);
                }
            } else {
                queryCallback.onResult(false, null, null);
            }
        }).addOnFailureListener(e -> queryCallback.onResult(false, null, null));
    }

    public void getAllVaccineBySearch(String field,String value,FirebaseQueryCallback queryCallback) {
        Query first = db.collection(DbNode.VACCINE.toString()).limit(50).whereEqualTo(field,value);
        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Vaccine> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.toObject(Vaccine.class));
                }
                if (task.getResult().size() > 0) {
                    DocumentSnapshot lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);

                    Query next = db.collection(DbNode.REQUEST.toString())
                            .startAfter(lastVisible)
                            .limit(100);

                    queryCallback.onResult(true, list, next);
                } else {
                    queryCallback.onResult(true, list, null);
                }
            } else {
                queryCallback.onResult(false, null, null);
            }
        }).addOnFailureListener(e -> queryCallback.onResult(false, null, null));
    }


    public void getAllVaccineBySearch(String value,FirebaseQueryCallback queryCallback) {
        Query first = db.collection(DbNode.VACCINE.toString()).limit(50).whereEqualTo("hospitalUid",value);
        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Vaccine> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.toObject(Vaccine.class));
                }
                if (task.getResult().size() > 0) {
                    DocumentSnapshot lastVisible = task.getResult().getDocuments()
                            .get(task.getResult().size() - 1);

                    Query next = db.collection(DbNode.REQUEST.toString())
                            .startAfter(lastVisible)
                            .limit(100);

                    queryCallback.onResult(true, list, next);
                } else {
                    queryCallback.onResult(true, list, null);
                }
            } else {
                queryCallback.onResult(false, null, null);
            }
        }).addOnFailureListener(e -> queryCallback.onResult(false, null, null));
    }

}

