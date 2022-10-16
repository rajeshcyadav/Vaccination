package com.example.vaccination.ui.mainscreen.hospital.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vaccination.R;
import com.example.vaccination.data.Request;
import com.example.vaccination.data.VaccineStatus;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private final List<Request> requestList;
    private final Context context;
    private final int[] colors;
    private final int colorsSize;

    public RecordAdapter(Context context, List<Request> requestList) {
        this.requestList = requestList;
        this.context = context;
        colors = context.getResources().getIntArray(R.array.colorPallet1);
        colorsSize = colors.length;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_record_card, parent, false);
        return new ViewHolder(view);
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
        holder.textviewCancellationReason.setText(request.getReason());
        holder.textViewStatus.setText(request.getStatus());
        holder.sideColor.setBackgroundColor(colors[position % colorsSize]);

        setStatusBackground(holder, request.getStatus(), context);
    }

    private void setStatusBackground(ViewHolder holder, String status, Context context) {
        Drawable bg;
        if (status.equalsIgnoreCase(VaccineStatus.WAITING.toString())) {

            bg = ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_unknown, context.getTheme());
        } else if (status.equalsIgnoreCase(VaccineStatus.APPROVED.toString()) ||
                status.equalsIgnoreCase(VaccineStatus.COMPLETED.toString())) {
            bg = ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_approved, context.getTheme());
        } else {
            bg = ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_denied, context.getTheme());
        }
        holder.textViewStatus.setBackground(bg);
    }

    @Override
    public int getItemCount() {
        if (requestList == null)
            return 0;
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private  TextView textviewHospitalName, textviewVaccineDate, textviewVaccineName,
                textviewUserName, textviewUserGender, textviewUserContact,
                textviewCancellationReason, textViewStatus;
        private ShapeableImageView sideColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textviewHospitalName = itemView.findViewById(R.id.textviewHospitalNameCustomRecord);
            textviewVaccineDate = itemView.findViewById(R.id.textviewVaccineDateCustomRecord);
            textviewVaccineName = itemView.findViewById(R.id.textviewVaccineNameCustomRecord);
            textviewUserName = itemView.findViewById(R.id.textviewUserNameCustomRecord);
            textviewUserGender = itemView.findViewById(R.id.textviewUserGenderCustomRecord);
            textviewUserContact = itemView.findViewById(R.id.textviewUserContactCustomRecord);
            textviewCancellationReason = itemView.findViewById(R.id.textviewReasonCustomRecord);
            textViewStatus = itemView.findViewById(R.id.textViewStatusCustomRecord);
            sideColor = itemView.findViewById(R.id.shapeableImageViewRecord);
        }
    }
}
