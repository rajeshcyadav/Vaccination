package com.example.vaccination.ui.mainscreen.hospital;

import static com.example.vaccination.myutils.MyUtil.animateTextView;
import static com.example.vaccination.myutils.MyUtil.getDayAndDate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.Request;
import com.example.vaccination.data.VaccineStatus;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.stream.Collectors;


public class HospitalMain extends Fragment {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;
    private List<Request> requestList;
    private FirebaseUtils firebaseUtils;

    public HospitalMain() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hospital_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUtils = new FirebaseUtils(db);
        loadingDialog = new LoadingDialog(getContext(), "Loading...");
        loadingDialog.create();
        init(view);
    }

    private void init(View root) {

        root.findViewById(R.id.materialCardViewRequest).setOnClickListener(v -> {
            NavDirections action =
                    HospitalMainDirections.actionHospitalMainToHospitalRequests();
            navigate(action);

        });
        root.findViewById(R.id.materialCardViewVaccine).setOnClickListener(v -> {
            NavDirections action = HospitalMainDirections.actionHospitalMainToHospitalVaccineList();
            navigate(action);
        });
        root.findViewById(R.id.materialCardViewRecords).setOnClickListener(v -> {
            NavDirections action = HospitalMainDirections.actionHospitalMainToHospitalRecords();
            navigate(action);
        });
        root.findViewById(R.id.materialCardViewProfile).setOnClickListener(v -> {
            NavDirections action = HospitalMainDirections.actionHospitalMainToHospitalProfile();
            navigate(action);
        });
        root.findViewById(R.id.buttonAddVaccineHospitalMain).setOnClickListener(v -> {
            NavDirections action = HospitalMainDirections.actionHospitalMainToEditVaccine();
            navigate(action);
        });

        FirebaseDataReceived listener = (object, success) -> {

            if (success) {
                populateUi(root, (Hospital) object);

                FirebaseListData listener2 = (list, success2) -> {
                    if (success2) {

                        requestList = (List<Request>) list;
                        requestList = requestList.stream().filter(request -> request.getStatus()
                                .equalsIgnoreCase(VaccineStatus.APPROVED.toString()))
                                .collect(Collectors.toList());
                        setRequestCounter(root,requestList.size());

                    }
                };

                firebaseUtils.getRequestNodeData(auth.getUid(), "hospitalId", listener2);
            }

            loadingDialog.dismiss();
        };

        getData(listener);


    }

    private void getData(FirebaseDataReceived listener) {
        db.collection(DbNode.HOSPITAL.toString()).document(auth.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.dataReceived(task.getResult().toObject(Hospital.class), true);
                    } else {
                        listener.dataReceived(null, false);
                    }

                }).addOnFailureListener(e -> {

        });
    }

    private void populateUi(View root, Hospital data) {
        TextView textViewHospitalName = root.findViewById(R.id.textViewHospitalNameMain);
        TextView textViewDate = root.findViewById(R.id.textViewCurrentDateMain);
        //TextView textViewRequestCounter = root.findViewById(R.id.textviewRequestCounterHospitalMain);
        TextView textViewVaccinatedCounter = root.findViewById(R.id.textviewVaccinatedCounterHospitalMain);

        textViewDate.setText(getDayAndDate());
        textViewHospitalName.setText(data.getName());

        int vaccinatedCounter = data.getVaccinatedCounter() >= 0 ? (int) data.getVaccinatedCounter() : 0;
        //int requestCounter = data.getRequestCounter() >= 0 ? (int) data.getRequestCounter() : 0;


        animateTextView(0, vaccinatedCounter, textViewVaccinatedCounter);
        //animateTextView(0, requestCounter, textViewRequestCounter);
    }
    private void  setRequestCounter(View root,int requestCounter){
        TextView textViewRequestCounter = root.findViewById(R.id.textviewRequestCounterHospitalMain);
        animateTextView(0, requestCounter, textViewRequestCounter);
    }

    private void navigate(NavDirections action) {
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
    }

}