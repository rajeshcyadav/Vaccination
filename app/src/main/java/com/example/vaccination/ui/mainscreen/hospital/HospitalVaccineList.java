package com.example.vaccination.ui.mainscreen.hospital;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.HospitalCollection;
import com.example.vaccination.data.Vaccine;
import com.example.vaccination.myInterface.FirebaseDataReceived;
import com.example.vaccination.myInterface.FirebaseQueryCallback;
import com.example.vaccination.myInterface.MyRecyclerClickListener;
import com.example.vaccination.myInterface.RecyclerLongClickListener;
import com.example.vaccination.myutils.FirebaseUtils;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyAlertDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.ui.mainscreen.user.adapter.VaccineAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class HospitalVaccineList extends Fragment implements MyAlertDialog.MyAlertDialogListener {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Vaccine> vaccines;
    private VaccineAdapter vaccineAdapter;
    private int longClickPosition;

    public HospitalVaccineList() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hospital_vaccine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        init(view);
    }

    private void init(View root) {
        LoadingDialog loadingDialog = new LoadingDialog(getContext(), "Getting Vaccines...");
        loadingDialog.create();

        SearchView searchView = root.findViewById(R.id.searchViewVaccineListHospital);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateAdapter(getSearchResult(vaccines, query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        FirebaseQueryCallback firebaseQueryCallback = (success, list, query) -> {
            if (success) {
                vaccines = (List<Vaccine>) list;
                setupRecyclerView(root, vaccines);

            } else {
                showMessage();
            }
            loadingDialog.dismiss();

        };
        FirebaseUtils firebaseUtils = new FirebaseUtils(db);
        firebaseUtils.getAllVaccineBySearch(auth.getUid(), firebaseQueryCallback);
        //getData(firebaseDataReceived);
    }

    private void updateAdapter(List<Vaccine> updatedList) {
        if (updatedList.size() == 0) {
            Toast.makeText(getContext(), "No Vaccine Available", Toast.LENGTH_LONG).show();
        }
        vaccineAdapter.setList(updatedList);
        vaccineAdapter.notifyDataSetChanged();

    }

    private void setupRecyclerView(View root, List<Vaccine> list) {
        MyRecyclerClickListener myRecyclerClickListener = position -> {
            HospitalVaccineListDirections.ActionHospitalVaccineListToEditVaccine action = HospitalVaccineListDirections.actionHospitalVaccineListToEditVaccine();
            action.setVaccine(list.get(position));
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
        };

        RecyclerLongClickListener longClickListener = (position, view) -> {
            longClickPosition = position;
            popUpMenu(view);
        };


        vaccineAdapter = new VaccineAdapter(getContext(), list, myRecyclerClickListener, longClickListener);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewHospitalVaccine);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(vaccineAdapter);
    }

    @Deprecated
    private void getData(FirebaseDataReceived listener) {
        ArrayList<Vaccine> arrayList = new ArrayList<>();
        db.collection(DbNode.HOSPITAL.toString()).document(auth.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Hospital hospital = task.getResult().toObject(Hospital.class);
                listener.dataReceived(hospital, true);
            } else {
                listener.dataReceived(null, false);
                showMessage();
            }
        }).addOnFailureListener(e -> {
            listener.dataReceived(null, false);
            showMessage(e.getLocalizedMessage());
        });
    }

    private List<Vaccine> getSearchResult(List<Vaccine> vaccines, String searchParameter) {
        return vaccines.stream().filter(vaccine ->
                searchParameter.toLowerCase().contains(vaccine.getName().toLowerCase()) ||
                        searchParameter.toLowerCase().contains(vaccine.getDisease().toLowerCase())
        ).collect(Collectors.toList());
    }

    private void popUpMenu(View button) {
        PopupMenu popup = new PopupMenu(getContext(), button);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.vaccine_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.vaccine_menu_delete) {
                showAlertBox();
            }

            return true;
        });

        popup.show();//showing popup menu
    }

    private void showMessage() {
        showMessage(getContext(), getString(R.string.DbError));
    }

    private void showMessage(String message) {
        showMessage(getContext(), message);
    }

    private void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void showAlertBox() {
        DialogFragment dialog = new MyAlertDialog();
        dialog.show(getParentFragmentManager(), "NoticeDialogFragment");
    }

    private void removeVaccine() {
        String uid = vaccines.get(longClickPosition).getUid();
        db.collection(DbNode.HOSPITAL.toString()).document(auth.getUid())
                .collection(HospitalCollection.vaccineList.toString())
                .document(uid).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showMessage(MyConstants.VACCINE_DELETED);
                        vaccineAdapter.notifyItemRemoved(longClickPosition);
                    } else {
                        showMessage(MyConstants.VACCINE_DELETE_FAILED);
                    }
                })
                .addOnFailureListener(e -> {
                    showMessage(MyConstants.VACCINE_DELETE_FAILED);

                });
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        removeVaccine();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}