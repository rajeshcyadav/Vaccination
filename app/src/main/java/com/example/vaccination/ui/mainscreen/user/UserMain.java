package com.example.vaccination.ui.mainscreen.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Disease;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.User;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myutils.Haversine;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyCurrentUser;
import com.example.vaccination.ui.mainscreen.user.adapter.HospitalAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class UserMain extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UserMain() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        init(view);
    }


    private void init(View root) {
        LoadingDialog loadingDialog = new LoadingDialog(getContext(), "Loading...");
        loadingDialog.create();

        TextView textviewGreeting = root.findViewById(R.id.textViewGreetings);
        TextView textViewUsername = root.findViewById(R.id.textViewUserName);

        ProgressBar progressBarHospitalList = root.findViewById(R.id.progressBarHospitalList);

        root.findViewById(R.id.button).setOnClickListener(v -> {
            navigateToCowin();
        });
        root.findViewById(R.id.cardViewFlu).setOnClickListener(v -> {
            navigateToVaccine(Disease.FLU);
        });
        root.findViewById(R.id.cardViewMeasles).setOnClickListener(v -> {
            navigateToVaccine(Disease.MEASLES);
        });
        root.findViewById(R.id.cardViewPolio).setOnClickListener(v -> {
            navigateToVaccine(Disease.POLIO);
        });
        root.findViewById(R.id.cardViewMore).setOnClickListener(v -> {
            navigateToVaccine(Disease.ALL);
        });

        root.findViewById(R.id.materialCardViewRequestUser).setOnClickListener(view -> {
            NavDirections action=UserMainDirections.actionUserMainToUserRequest();
            Navigation.findNavController(getActivity(),R.id.nav_host_fragment).navigate(action);
        });

        root.findViewById(R.id.materialCardViewHospitalUser).setOnClickListener(view -> {
            NavDirections action=UserMainDirections.actionUserMainToAllHospitals();
            Navigation.findNavController(getActivity(),R.id.nav_host_fragment).navigate(action);
        });

        root.findViewById(R.id.materialCardViewRecordUser).setOnClickListener(view -> {
            NavDirections action=UserMainDirections.actionUserMainToUserRecord();
            Navigation.findNavController(getActivity(),R.id.nav_host_fragment).navigate(action);
        });

        root.findViewById(R.id.materialCardViewProfileUser).setOnClickListener(view -> {
            NavDirections action=UserMainDirections.actionUserMainToUserProfile();
            Navigation.findNavController(getActivity(),R.id.nav_host_fragment).navigate(action);
        });

        FirebaseListData hospitalListReceived = (list, success) -> {
            if (success) {
                //noinspection unchecked
                setUpRecyclerView(root, (List<Hospital>) list);
            } else showError();
            progressBarHospitalList.setVisibility(View.INVISIBLE);
        };

        FirebaseDataReceived firebaseDataReceived = (object, success) -> {
            if (success) {
                User user = (User) object;
                MyCurrentUser.setUser(user);
                textViewUsername.setText(user.getName());
                loadingDialog.dismiss();
                getListData(hospitalListReceived,user);
            } else {
                //show error message
                showError();
            }
        };





        textviewGreeting.setText(getGreetings());
        getProfileData(firebaseDataReceived);

    }

    private String getGreetings() {
        LocalTime localTime = new LocalTime();
        int hour = localTime.getHourOfDay();
        if (hour >= 0 && hour < 12) {
            return getString(R.string.good_morning);
        } else if (hour >= 12 && hour < 16) {
            return getString(R.string.good_afternoon);
        } else if (hour >= 16 && hour <= 23) {
            return getString(R.string.good_evening);
        } else
            return "Some Error Occurred";

    }

    private void setUpRecyclerView(View root, List<Hospital> list) {
        sortHospitalByDistance(list);
        HospitalAdapter hospitalAdapter = new HospitalAdapter(getContext(), list, null);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewHospital);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(hospitalAdapter);
    }

    private void getProfileData(FirebaseDataReceived firebaseDataReceived) {
        db.collection(DbNode.USER.toString()).document(auth.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null)
                            firebaseDataReceived.dataReceived(user, true);
                        else
                            firebaseDataReceived.dataReceived(null, false);
                    } else {
                        //showError
                        firebaseDataReceived.dataReceived(null, false);
                        showError();
                    }
                }).addOnFailureListener(e -> {
            //show Error
            firebaseDataReceived.dataReceived(null, false);
            showError(e.getLocalizedMessage());
        });
    }


    private void getListData(FirebaseListData firebaseListData, User user) {
        //User user=MyCurrentUser.getUser();
        ArrayList<Hospital> arrayList=new ArrayList<>();
        db.collection(DbNode.HOSPITAL.toString()).limit(3).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (QueryDocumentSnapshot document:task.getResult()){
                    Hospital hospital=document.toObject(Hospital.class);
                    double distance=Haversine.getHaversine(
                            hospital.getLatitude(), hospital.getLongitude(),
                            user.getLatitude(), user.getLongitude()
                    );
                    hospital.setDistanceFromUser(distance);
                    arrayList.add(hospital);
                }

                firebaseListData.dataReceived(arrayList,true);
            }
            else {
                firebaseListData.dataReceived(null,false);
                showError();
            }
        }).addOnFailureListener(e -> {
            showError(e.getLocalizedMessage());
        });
    }

    private void navigateToCowin() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.Cowin)));
        startActivity(browserIntent);
    }

    private void navigateToVaccine(Disease search) {
        //navigate to vaccine screen parameter as search param
        UserMainDirections.ActionUserMainToVaccineList action =
                UserMainDirections.actionUserMainToVaccineList();
        action.setDiseaseName(search);
        Navigation.findNavController(getActivity(),R.id.nav_host_fragment).navigate(action);


        //Navigation.findNavController(view).navigate(action)
       /* NavDirections action =
                UserMain.actionSignUpToSignIn();
        Navigation.findNavController(view).navigate(action);*/
    }

    private void sortHospitalByDistance(List<Hospital> hospitals) {
        hospitals.sort(Comparator.comparingDouble(Hospital::getDistanceFromUser));
    }

    private void showError() {
        showError(getContext(), getString(R.string.DbError));
    }

    private void showError(String message) {
        showError(getContext(), message);
    }

    private void showError(Context context, String message) {
        //change from toast if possible
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}