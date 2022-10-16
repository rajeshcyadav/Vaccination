package com.example.vaccination.ui.mainscreen.user;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Vaccine;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myutils.FirebaseUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class VaccineDetails extends Fragment {
    private FirebaseFirestore db;
private FirebaseUtils firebaseUtils;

    public VaccineDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vaccine_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Vaccine vaccine = (Vaccine) getArguments().getSerializable("Vaccine");
        String vaccineUid = vaccine.getUid();
        db = FirebaseFirestore.getInstance();
        firebaseUtils=new FirebaseUtils(db);
        init(view, vaccine);
    }

    private void init(View root, Vaccine argsVaccine) {

        TextView textViewDiseaseName = root.findViewById(R.id.textViewVaccineNameDetails);
        TextView textViewDisease = root.findViewById(R.id.textViewDiseaseNameDetails);
        TextView textViewHospitalName = root.findViewById(R.id.textViewVaccineHospitalName);
        TextView textViewDiseaseAge = root.findViewById(R.id.textViewVaccineAgeDetails);
        TextView textViewDiseaseDescription = root.findViewById(R.id.textViewVaccineDescriptionDetails);

        FirebaseDataReceived firebaseDataReceived = (object, success) -> {
            if (success) {
                Vaccine vaccine = (Vaccine) object;

                textViewDiseaseName.setText(vaccine.getName());
                textViewDisease.setText(vaccine.getDisease());
                textViewDiseaseAge.setText(vaccine.getMinimumAgeFormattedString());
                textViewDiseaseDescription.setText(vaccine.getDescription());
                textViewHospitalName.setText(vaccine.getHospitalName());
            }
        };

        root.findViewById(R.id.buttonVaccineBookNow).setOnClickListener(v -> {
            NavDirections action = VaccineDetailsDirections.actionVaccineDetailsToVaccineRegistration(argsVaccine);
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
        });

        //getData(argsVaccine.getHospitalUid(), argsVaccine.getUid(), firebaseDataReceived);
        firebaseUtils.getVaccine( argsVaccine.getUid(), firebaseDataReceived);
        //move to vaccine registration



    }

    @Deprecated
    private void getData(String hospitalUid, String vaccineUid, FirebaseDataReceived firebaseDataReceived) {
        db.collection(DbNode.HOSPITAL.toString()).document(hospitalUid).collection(vaccineUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Vaccine vaccine = document.toObject(Vaccine.class);
                            if (vaccine != null) {
                                firebaseDataReceived.dataReceived(vaccine, true);
                            } else {
                                firebaseDataReceived.dataReceived(null, false);
                            }
                            break;
                        }
                    } else {
                        showError();
                        firebaseDataReceived.dataReceived(null, false);

                    }
                })
                .addOnFailureListener(e -> {
                    showError();
                    firebaseDataReceived.dataReceived(null, false);
                });
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