package com.example.vaccination.ui.mainscreen.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vaccination.R;
import com.example.vaccination.data.DbNode;
import com.example.vaccination.data.Index;
import com.example.vaccination.myInterface.FirebaseListData;
import com.example.vaccination.myutils.LoadingDialog;
import com.example.vaccination.myutils.MyConstants;
import com.example.vaccination.myutils.MyMessage;
import com.example.vaccination.ui.mainscreen.admin.adapter.UserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class AdminUserList extends Fragment {

    private FirebaseFirestore db;

    public AdminUserList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        init(view);
    }

    private void init(View root) {
        LoadingDialog loadingDialog = new LoadingDialog(getContext(), "Loading...");
        loadingDialog.show();

        FirebaseListData allUserListener = (list, success) -> {
            if (success) {
                setupRecycler(root, (List<Index>) list);
            } else {
                Toast.makeText(getContext(), MyConstants.DB_FAILED, Toast.LENGTH_LONG).show();
            }
            loadingDialog.dismiss();
        };

        getData(allUserListener);
    }

    private void getData(FirebaseListData listener) {
        db.collection(DbNode.INDEX.toString()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Index> list = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            list.add(document.toObject(Index.class));
                        }
                        listener.dataReceived(list, true);
                    } else {
                        listener.dataReceived(null, true);
                    }
                }).addOnFailureListener(e -> {
            listener.dataReceived(null, true);
        });
    }

    private void setupRecycler(View root, List<Index> list) {
        UserAdapter userAdapter = new UserAdapter(list, getContext());
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewUsersAdmin);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);
    }

}