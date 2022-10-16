package com.example.vaccination.ui.mainscreen.admin;

import static com.example.vaccination.myutils.MyUtil.animateTextView;
import static com.example.vaccination.myutils.MyUtil.getDayAndDate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vaccination.R;
import com.example.vaccination.data.Admin;
import com.example.vaccination.data.AdminCounter;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.Request;
import com.example.vaccination.data.VaccineStatus;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseQueryCallback;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.ui.mainscreen.hospital.HospitalMainDirections;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AdminMain extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;
    private FirebaseUtils firebaseUtils;
    private List<Request> requestList;

    public AdminMain() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingDialog = new LoadingDialog(getContext(), "Loading...");
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUtils = new FirebaseUtils(db);
        init(view);
    }

    private void init(View root) {
        loadingDialog.show();
        root.findViewById(R.id.materialCardViewRequestAdmin).setOnClickListener(v -> {
            navigate(AdminMainDirections.actionAdminMainToAdminRequest());
        });
        root.findViewById(R.id.materialCardViewUsersAdmin).setOnClickListener(v -> {
            navigate(AdminMainDirections.actionAdminMainToAdminUserList());
        });
        root.findViewById(R.id.materialCardViewRecordsAdmin).setOnClickListener(v -> {
            navigate(AdminMainDirections.actionAdminMainToAdminRecords());
        });
        root.findViewById(R.id.materialCardViewProfileAdmin).setOnClickListener(v -> {
            navigate(AdminMainDirections.actionAdminMainToUserProfile().setUserType(DbNode.ADMIN));
        });
        root.findViewById(R.id.buttonAddUserAdminMain).setOnClickListener(v -> {
            navigate(AdminMainDirections.actionAdminMainToAdminAddAccount());
        });

        FirebaseDataReceived counterDataReceived = (object, success) -> {
            if (success) {
                AdminCounter adminCounter = (AdminCounter) object;
                updateCounter(root, adminCounter);

                FirebaseQueryCallback firebaseQueryCallback = (success2, list, query) -> {
                    if (success2) {

                        requestList = (List<Request>) list;
                        requestList = requestList.stream().filter(request -> request.getStatus()
                                .equalsIgnoreCase(VaccineStatus.WAITING.toString()))
                                .collect(Collectors.toList());
                        setRequestCounter(root,requestList.size());
                    }
                    loadingDialog.dismiss();
                };

                firebaseUtils.getAllRequestNodeData(firebaseQueryCallback);

            }
        };


        FirebaseDataReceived listener = (object, success) -> {

            if (success) {
                populateUi(root, (Admin) object);

            }

            loadingDialog.dismiss();
        };
        firebaseUtils.getAdminCounterData(counterDataReceived);
        getData(listener);
    }

    private void getData(FirebaseDataReceived listener) {
        db.collection(DbNode.ADMIN.toString()).document(auth.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.dataReceived(task.getResult().toObject(Admin.class), true);
                    } else {
                        listener.dataReceived(null, false);
                    }

                }).addOnFailureListener(e -> {

        });
    }

    private void updateCounter(View root, AdminCounter counter) {
        //TextView textViewRequestCounter = root.findViewById(R.id.textviewRequestCounterAdminMain);
        TextView textViewUserCounter = root.findViewById(R.id.textviewUserCounterAdminMain);

        int userCounter = counter.getUSERCOUNTER() >= 0 ? (int) counter.getUSERCOUNTER() : 0;
        //int requestCounter = counter.getREQUESTCOUNTER() >= 0 ? (int) counter.getREQUESTCOUNTER() : 0;

        animateTextView(0, userCounter, textViewUserCounter);
        //animateTextView(0, requestCounter, textViewRequestCounter);

        firebaseUtils.verify(counter);
    }

    private void populateUi(View root, Admin data) {
        TextView textViewAdminName = root.findViewById(R.id.textViewAdminNameMain);
        TextView textViewDate = root.findViewById(R.id.textViewCurrentDateAdminMain);


        textViewDate.setText(getDayAndDate());
        textViewAdminName.setText(data.getName());

    }


    private void setRequestCounter(View root, int requestCounter) {
        TextView textViewRequestCounter = root.findViewById(R.id.textviewRequestCounterAdminMain);
        animateTextView(0, requestCounter, textViewRequestCounter);
    }

    private void navigate(NavDirections action) {
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
    }
}