package com.example.vaccination.ui.mainscreen.user;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.vaccination.R;
import com.example.vaccination.data.BaseModel;
import com.example.vaccination.data.Counter;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.Request;
import com.example.vaccination.data.User;
import com.example.vaccination.data.Vaccine;
import com.example.vaccination.data.VaccineStatus;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseDataUpdated;
import com.example.vaccination.myInterface.FirebaseDataUploaded;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.Haversine;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyCurrentUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class VaccineRegistration extends Fragment {
    private static final String TAG = "Vaccine Registration";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUtils firebaseUtils;
    private TextInputLayout vaccineName, hospitalName, vaccineDate, vaccineTime, personName, personGender, personContact;

    public VaccineRegistration() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vaccine_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Vaccine vaccine = (Vaccine) getArguments().getSerializable("Vaccine");
        firebaseUtils = new FirebaseUtils(db);
        init(view, vaccine);
    }

    private void init(View root, Vaccine vaccine) {
        Request request = new Request();
        request.setVaccineName(vaccine.getName());
        request.setVaccineId(vaccine.getUid());
        vaccineName = root.findViewById(R.id.outlinedTextFieldVaccine);
        hospitalName = root.findViewById(R.id.outlinedTextFieldVaccineLocation);
        vaccineDate = root.findViewById(R.id.outlinedTextFieldDate);
        vaccineTime = root.findViewById(R.id.outlinedTextFieldTime);
        personName = root.findViewById(R.id.outlinedTextFieldContactName);
        personGender = root.findViewById(R.id.outlinedTextFieldGender);
        personContact = root.findViewById(R.id.outlinedTextFieldContactNumber);
        Button buttonRegister = root.findViewById(R.id.buttonVaccineRegister);
        CheckBox checkBox = root.findViewById(R.id.checkboxVaccineRegistration);
        vaccineName.getEditText().setText(vaccine.getName());


        /*List<TextInputLayout> inputLayouts = new ArrayList<>();
        //inputLayouts.add(vaccineName);
        inputLayouts.add(hospitalName);
        inputLayouts.add(vaccineDate);
        inputLayouts.add(vaccineTime);
        // inputLayouts.add(personName);
        inputLayouts.add(personGender);
        // inputLayouts.add(personContact);*/

        FirebaseDataUpdated listener = (success, e) -> {
            if (success) {
                //data uploaded to firebase
                Toast.makeText(getContext(), "Registration for Vaccine Successful.", Toast.LENGTH_SHORT).show();
                //send back to main screen
                NavDirections action = VaccineRegistrationDirections.actionVaccineRegistrationToUserMain();
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(action);
            } else {
                showError("Registration for Vaccine Failed");
            }
        };

        FirebaseDataUploaded requestAdded = success -> {
            firebaseUtils.updateAdminCounter(Counter.REQUESTCOUNTER.toString(), 1, listener);
        };

        buttonRegister.setOnClickListener(v -> {
            if (checkBox.isChecked()) {
                if (validateAndAddData(request, vaccine)) {
                    if (personName.getEditText().getText().toString().isEmpty()) {
                        personName.setError(MyConstants.EMPTY_FIELD);
                        return;
                    } else {
                        personName.setError(null);
                        request.setUserName(personName.getEditText().getText().toString());
                    }
                    if (personContact.getEditText().getText().toString().isEmpty()) {
                        personContact.setError(MyConstants.EMPTY_FIELD);
                        return;
                    } else {
                        Log.d(TAG,
                                "init: Phone number length "+(personContact.getEditText().getText().toString()).trim().length());
                        if (personContact.getEditText().getText().toString().trim().length()!=10) {
                            personContact.setError(MyConstants.PHONE_ERROR);
                            return;
                        } else {
                            personContact.setError(null);
                            request.setContact(personContact.getEditText().getText().toString());
                        }
                    }

                    addToNode(request, requestAdded);

                } else
                    showError("Please fill all the field");
            } else {
                showError("Please Click CheckBox");
            }
        });

        List<String> dates = covertLongToStringDates(getConsecutiveDate(0, 7));
        List<String> times = Arrays.asList(getResources().getStringArray(R.array.times));
        List<String> gender = Arrays.asList(getResources().getStringArray(R.array.Genders));

        setupArrayAdapter(vaccineDate, dates,1);
        setupArrayAdapter(vaccineTime, times,0);
        setupArrayAdapter(personGender, gender,0);


        //new method which just gets the hospital which is mentioned in vaccine
        FirebaseDataReceived hospitalDataListener = (object, success) -> {
            if (success) {
                Hospital hospital = (Hospital) object;
                request.setHospitalId(hospital.getUid());
                request.setHospitalName(hospital.getName());
                hospitalName.getEditText().setText(hospital.getName());

            } else {
                Toast.makeText(getContext(), MyConstants.DB_FAILED, Toast.LENGTH_SHORT).show();
            }
        };

        getSingleHospital(vaccine.getHospitalUid(), hospitalDataListener);
    }

    private boolean validateAndAddData(Request request, Vaccine vaccine) {
        request.setUserId(MyCurrentUser.getUser().getUid());
        //request.setUserName(personName.getEditText().getText().toString());
        request.setDate(getSelectedText(vaccineDate));
        request.setTime(getSelectedText(vaccineTime));
        request.setStatus(VaccineStatus.WAITING.toString());
        request.setGender(getSelectedText(personGender));
        request.setHospitalId(vaccine.getHospitalUid());

        return true;
    }

    private String getSelectedText(TextInputLayout textInputLayout) {
        return ((AutoCompleteTextView) textInputLayout.getEditText()).getText().toString();
    }


    private void setupArrayAdapter(TextInputLayout textInputLayout, List<String> items,int index) {
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.item_item, items);
        ((AutoCompleteTextView) textInputLayout.getEditText()).setAdapter(arrayAdapter);
        ((AutoCompleteTextView) textInputLayout.getEditText()).setText(items.get(index),false);

    }


    private void getSingleHospital(String hospitalUid, FirebaseDataReceived listener) {
        db.collection(DbNode.HOSPITAL.toString()).document(hospitalUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Hospital hospital = task.getResult().toObject(Hospital.class);
                        listener.dataReceived(hospital, true);
                    } else {
                        listener.dataReceived(null, false);
                    }
                }).addOnFailureListener(e -> {
            listener.dataReceived(null, false);
        });
    }



    //adding to db
    private void addToNode(Request request, FirebaseDataUploaded listener) {
        DocumentReference ref =
                db.collection(DbNode.REQUEST.toString()).document();
        request.setRequestId(ref.getId());
        request.setUserId(auth.getUid());
        request.setTimestamp(getCurrentTime());

        Log.d(TAG, "addToNode: ref key " + ref.getId());
        Log.d(TAG, "addToNode: \n" + request.toString());

        ref.set(request).addOnCompleteListener(task -> {
            listener.dataUploaded(task.isSuccessful());
        }).addOnFailureListener(e -> {
            listener.dataUploaded(false);
        });

    }


    private void addToUserNode(Request request, FirebaseDataUploaded listener) {
        DocumentReference reference = db.collection(DbNode.USER.toString()).document(auth.getUid()).collection(DbNode.REQUEST.toString()).document();
        String id = reference.getId();
        request.setRequestId(id);
        reference.set(request).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addToHospitalNode(request, listener);
            }
        }).addOnFailureListener(e -> {
            listener.dataUploaded(false);
        });
    }

    private void addToHospitalNode(Request request, FirebaseDataUploaded listener) {
        DocumentReference reference = db.collection(DbNode.HOSPITAL.toString()).document(request.getHospitalId()).collection(DbNode.REQUEST.toString()).document();
        String id = reference.getId();
        request.setRequestId(id);
        reference.set(request).addOnCompleteListener(task -> listener.dataUploaded(task.isSuccessful()))
                .addOnFailureListener(e -> {
                    listener.dataUploaded(false);
                });
    }

    private List<Hospital> getSearchResult(List<Hospital> hospitals, String searchParameter) {

        List<Hospital> filtered = new ArrayList<>();
        for (Hospital hospital : hospitals) {
            long count = hospitals.stream().filter(vaccine ->
                    searchParameter.equalsIgnoreCase(vaccine.getUid())
            ).count();
            if (count > 0)
                filtered.add(hospital);

        }
        return filtered;
    }

    private void sortHospitalByDistance(List<Hospital> hospitals) {
        hospitals.sort(Comparator.comparingDouble(Hospital::getDistanceFromUser));
    }

    private void filterHospitalWithVaccine(List<Hospital> hospitals, String searchParameter) {
        sortHospitalByDistance(getSearchResult(hospitals, searchParameter));
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private List<Long> getConsecutiveDate(int start, int end) {
        //start - default 0 -> 0 = current date
        //end - default 3 -> get 3 dates from starting date
        long currentTime = getCurrentTime();
        long _1Day = 86400000;
        List<Long> dates = new LinkedList<>();
        dates.add(currentTime);
        for (int i = start; i <= end; i++) {
            currentTime += _1Day;
            dates.add(currentTime);
        }
        return dates;
    }

    private List<String> covertLongToStringDates(List<Long> dates) {
        List<String> stringDates = new LinkedList<>();
        for (Long date : dates) {
            stringDates.add(new DateTime(date).toLocalDate().toString());
        }
        return stringDates;
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