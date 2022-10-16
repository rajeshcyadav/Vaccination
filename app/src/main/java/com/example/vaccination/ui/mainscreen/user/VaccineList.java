package com.example.vaccination.ui.mainscreen.user;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Disease;
import com.example.vaccination.data.Hospital;
import com.example.vaccination.data.Vaccine;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myInterface.MyRecyclerClickListener;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.ui.mainscreen.user.adapter.VaccineAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class VaccineList extends Fragment {
    private static final String TAG = "VaccineList";
    private FirebaseFirestore db;
    private List<Vaccine> vaccines;
    private VaccineAdapter vaccineAdapter;

    public VaccineList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vaccine_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        Disease search = (Disease) getArguments().getSerializable("DiseaseName");
        init(view, search);
    }

    private void init(View root, Disease search) {
        LoadingDialog loadingDialog = new LoadingDialog(getContext(), "Getting Vaccines...");
        loadingDialog.create();

        SearchView searchView = root.findViewById(R.id.searchViewVaccineListUser);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.equals("")) {
                    updateAdapter(vaccines);
                } else
                    updateAdapter(getSearchResult(vaccines, query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        FirebaseListData firebaseListDataListener = (list, success) -> {
            if (success) {
                vaccines = (List<Vaccine>) list;
                setupRecyclerView(root, vaccines);
                if (search != Disease.ALL)
                    updateAdapter(getSearchResult(vaccines, search.toString()));
            } else {
                showError();
                Log.d(TAG, "init: Listener success failed");
            }
            loadingDialog.dismiss();
        };
        getData(firebaseListDataListener);
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
            NavDirections action = VaccineListDirections.actionVaccineListToVaccineDetails(list.get(position));
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(action);
        };


        vaccineAdapter = new VaccineAdapter(getContext(), list, myRecyclerClickListener);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewVaccine);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(vaccineAdapter);
    }

    private void getData(FirebaseListData listener) {
        ArrayList<Vaccine> arrayList = new ArrayList<>();
        //db.collection(DbNode.HOSPITAL.toString()).get()
        db.collection(DbNode.VACCINE.toString()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "getData: task length" + task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "getData: " + document.getData().toString());
                            if (document.exists()) {
                       /* Log.d(TAG, "getData: "+document.getData().toString());
                        Hospital hospital = document.toObject(Hospital.class);
                        arrayList.addAll(hospital.getVaccineList());*/

                                Vaccine vaccine = document.toObject(Vaccine.class);
                                arrayList.add(vaccine);

                            } else {
                                Log.d(TAG, "getData: " + document.exists());
                            }
                        }

                        listener.dataReceived(arrayList, true);
                    } else {
                        listener.dataReceived(null, false);
                        Log.d(TAG, "getData: task Unsuccessful");
                        showError();
                    }
                }).addOnFailureListener(e -> {
            listener.dataReceived(null, false);
            Log.d(TAG, "getData: Db Failed");
            showError(e.getLocalizedMessage());
        });
    }

    private List<Vaccine> getSearchResult(List<Vaccine> vaccines, String searchParameter) {
        return vaccines.stream().filter(vaccine ->
                vaccine.getName().contains(searchParameter.toLowerCase()) ||
                        vaccine.getDisease().toLowerCase().contains(searchParameter.toLowerCase())
        ).collect(Collectors.toList());
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