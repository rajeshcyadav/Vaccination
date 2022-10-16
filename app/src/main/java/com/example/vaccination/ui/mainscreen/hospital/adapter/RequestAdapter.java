package com.example.vaccination.ui.mainscreen.hospital.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vaccination.R;
import com.example.vaccination.data.Request;
import com.example.vaccination.myInterface.MyRecyclerClickListener;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private final List<Request> requestList;
    private final MyRecyclerClickListener positiveClick;
    private final MyRecyclerClickListener negativeClick;
    private boolean isUser = false;

    public RequestAdapter(List<Request> requestList, MyRecyclerClickListener positiveClick, MyRecyclerClickListener negativeClick) {
        this.requestList = requestList;
        this.positiveClick = positiveClick;
        this.negativeClick = negativeClick;
    }

    public RequestAdapter(List<Request> requestList, MyRecyclerClickListener positiveClick, MyRecyclerClickListener negativeClick, boolean isUser) {
        this.requestList = requestList;
        this.positiveClick = positiveClick;
        this.negativeClick = negativeClick;
        this.isUser = isUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_request_card, parent, false);
        return new ViewHolder(view, positiveClick, negativeClick, isUser);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Request request = requestList.get(position);
        holder.textviewHospitalName.setText(request.getHospitalName());
        holder.textviewVaccineDate.setText(request.getDate());
        holder.textviewVaccineName.setText(request.getVaccineName());
        holder.textviewUserName.setText(request.getUserName());
        holder.textviewUserGender.setText(request.getGender());
        holder.textviewUserContact.setText(request.getContact());
    }

    @Override
    public int getItemCount() {
        if (requestList == null)
            return 0;
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textviewHospitalName, textviewVaccineDate, textviewVaccineName, textviewUserName, textviewUserGender, textviewUserContact;
        private final Button buttonAccept, buttonDeny;

        public ViewHolder(@NonNull View itemView, MyRecyclerClickListener positiveClick,
                          MyRecyclerClickListener negativeClick, boolean isUser) {
            super(itemView);

            textviewHospitalName = itemView.findViewById(R.id.textviewHospitalNameCustomRequest);
            textviewVaccineDate = itemView.findViewById(R.id.textviewVaccineDateCustomRequest);
            textviewVaccineName = itemView.findViewById(R.id.textviewVaccineNameCustomRequest);
            textviewUserName = itemView.findViewById(R.id.textviewUserNameCustomRequest);
            textviewUserGender = itemView.findViewById(R.id.textviewUserGenderCustomRequest);
            textviewUserContact = itemView.findViewById(R.id.textviewUserContactCustomRequest);
            buttonAccept = itemView.findViewById(R.id.buttonAcceptCustomRequestHospital);
            buttonDeny = itemView.findViewById(R.id.buttonDenyCustomRequestHospital);

            if (isUser) {
                buttonAccept.setVisibility(View.GONE);
                buttonDeny.setText("Cancel");
            }

            buttonAccept.setOnClickListener(view -> {
                positiveClick.onClick(getAdapterPosition());
            });

            buttonDeny.setOnClickListener(view -> {
                negativeClick.onClick(getAdapterPosition());
            });
        }
    }
}
