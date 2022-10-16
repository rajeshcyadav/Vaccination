package com.example.vaccination.ui.mainscreen.hospital;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Disease;
import com.example.vaccination.data.HospitalCollection;
import com.example.vaccination.data.Vaccine;
import com.example.vaccination.myInterface.TaskCallbackMessage;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyCurrentUser;
import com.example.vaccination.myutils.MyMessage;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EditVaccine extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MyMessage message;
    //private TextInputLayout vaccineUid, vaccineName, vaccineFor, vaccineStock, vaccineAddStock;

    public EditVaccine() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_vaccine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Vaccine vaccine = (Vaccine) getArguments().getSerializable("Vaccine");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        message = new MyMessage(getContext(), "Failed to perform given action");
        init(view, vaccine);
    }

    private void init(View root, Vaccine vaccine) {
        TextView title = root.findViewById(R.id.textviewEditVaccineTitle);
        Button buttonSubmit = root.findViewById(R.id.buttonUpdateVaccine);

        TextInputLayout vaccineDisease = root.findViewById(R.id.outlinedTextFieldVaccineDisease);
        TextInputLayout vaccineAge = root.findViewById(R.id.outlinedTextFieldVaccineAge);
        TextInputLayout vaccineUid = root.findViewById(R.id.outlinedTextFieldVaccineUid);
        TextInputLayout vaccineName = root.findViewById(R.id.outlinedTextFieldVaccineName);
        TextInputLayout vaccineStock = root.findViewById(R.id.outlinedTextFieldVaccineStock);
        TextInputLayout vaccineAddStock = root.findViewById(R.id.outlinedTextFieldAddVaccineStock);
        TextInputLayout vaccineDescription = root.findViewById(R.id.outlinedTextFieldVaccineDescription);

        setupDropdown(vaccineDisease, vaccine);



        if (vaccine == null) {
            title.setText("Add Vaccine");
            buttonSubmit.setText("Add");
            vaccine = new Vaccine();
        } else {
            title.setText("Edit Vaccine");
            buttonSubmit.setText("Update");

            vaccineUid.getEditText().setText(vaccine.getUid());
            vaccineName.getEditText().setText(vaccine.getName());
            vaccineAge.getEditText().setText(String.valueOf(vaccine.getMinimumAge()));
            vaccineStock.getEditText().setText(String.valueOf(vaccine.getStock()));
            vaccineDescription.getEditText().setText(vaccine.getDescription());
        }

        TaskCallbackMessage dataUploadedCallback = (taskSuccess, taskMessage) -> {
            if (taskSuccess) {
                message.showToastMessage(taskMessage);
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack();
            } else {
                message.showToastMessage(taskMessage);
            }
        };

        Vaccine finalVaccine = vaccine;
        buttonSubmit.setOnClickListener(view -> {
            if (isValid(vaccineName) && isValid(vaccineStock) && isValid(vaccineAddStock) && isValid(vaccineAge)) {
                finalVaccine.setName(getText(vaccineName));
                finalVaccine.setDisease(getText(vaccineDisease));
                long finalStock = Long.parseLong(getText(vaccineStock)) + Long.parseLong(getText(vaccineAddStock));
                finalVaccine.setStock(finalStock);
                finalVaccine.setDescription(getText(vaccineDescription));
                finalVaccine.setHospitalName(MyCurrentUser.getUser().getName());
                finalVaccine.setMinimumAge(Integer.parseInt(getText(vaccineAge)));
                uploadData(finalVaccine, dataUploadedCallback);
            }
        });
    }

    private void uploadData(Vaccine vaccine, TaskCallbackMessage listener) {
        if (vaccine.getUid() != null && vaccine.getUid().length() > 1) {
            updateVaccine(vaccine, listener);
        } else
            addVaccine(vaccine, listener);
    }

    private String getText(TextInputLayout textInputLayout) {
        return textInputLayout.getEditText().getText().toString();
    }

    private boolean isValid(TextInputLayout textInputLayout) {
        boolean isValid = !textInputLayout.getEditText().getText().toString().isEmpty();
        if (!isValid) {
            textInputLayout.setError(MyConstants.EMPTY_FIELD);
        } else {
            textInputLayout.setError(null);
        }
        return isValid;
    }

    private void setupDropdown(TextInputLayout textInputLayout, Vaccine vaccine) {
        List<String> list = Stream.of(Disease.values()).map(Enum::name).collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_item, list);
        AutoCompleteTextView dropdown = ((AutoCompleteTextView) textInputLayout.getEditText());
        dropdown.setAdapter(adapter);
        if (vaccine != null) {
            dropdown.setText(vaccine.getDisease(),false);
        }
        //dropdown.setOnItemClickListener((parent, view, position, id) -> vaccine.setDisease(list.get(position)));
    }

    private void setupDropdown(TextInputLayout textInputLayout, List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_item, values);
        AutoCompleteTextView dropdown = ((AutoCompleteTextView) textInputLayout.getEditText());
        dropdown.setAdapter(adapter);
    }


    private void addVaccine(Vaccine vaccine, TaskCallbackMessage callback) {
       /* DocumentReference ref = db.collection(DbNode.HOSPITAL.toString())
                .document(auth.getUid())
                .collection(HospitalCollection.vaccineList.toString()).document();*/

        DocumentReference ref = db.collection(DbNode.VACCINE.toString())
                .document();

        vaccine.setUid(ref.getId());
        vaccine.setHospitalUid(auth.getUid());

        ref.set(vaccine).addOnCompleteListener(task -> {
            callback.result(task.isSuccessful(), "Vaccine Added Successfully");
        }).addOnFailureListener(e -> {
            callback.result(false, null);
        });
    }

    private void updateVaccine(Vaccine vaccine, TaskCallbackMessage callback) {
        /*db.collection(DbNode.HOSPITAL.toString())
                .document(auth.getUid())
                .collection(HospitalCollection.vaccineList.toString())
                .document(vaccine.getUid()).set(vaccine).*/


        db.collection(DbNode.VACCINE.toString())
                .document(vaccine.getUid()).set(vaccine).
                addOnCompleteListener(task -> {
                    callback.result(task.isSuccessful(), "Vaccine Updated Successfully");
                }).addOnFailureListener(e -> {
            callback.result(false, null);
        });
    }
}