package com.example.vaccination.ui.mainscreen.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vaccination.R;
import com.example.vaccination.data.Hospital;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {

    private final List<Hospital> list;
    private final Context context;
    private final View.OnClickListener onClickListener;


    public HospitalAdapter(Context context, List<Hospital> list, View.OnClickListener onClickListener) {
        this.context = context;
        this.list = list;
        this.onClickListener = onClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_hospital_card, parent, false);
        return new ViewHolder(view, onClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hospital hospital = list.get(position);

        Glide.with(context).load(hospital.getImgUrl())
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_image_not_supported_24)
                .into(holder.imageViewHospital);

        holder.hospitalName.setText(hospital.getName());
        if (hospital.isOpen24Hours()) {
            holder.hospitalTime.setText(R.string.hospitalOpen24);
        } else {
            holder.hospitalTime.setText(hospital.getSingleTime());
        }
        holder.hospitalAddress.setText(hospital.getLocation());
        holder.hospitalDistance.setText(String.valueOf(hospital.getFormattedDistance()));

        holder.hospitalStars.setText(String.valueOf(hospital.getStars()));

    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewHospital;
        TextView hospitalName, hospitalTime, hospitalAddress;
        Chip hospitalDistance, hospitalStars;

        public ViewHolder(@NonNull View itemView, View.OnClickListener onClickListener) {
            super(itemView);
            imageViewHospital = itemView.findViewById(R.id.imageViewHospitalImageCustom);
            hospitalName = itemView.findViewById(R.id.textViewHospitalNameCustom);
            hospitalTime = itemView.findViewById(R.id.textViewHospitalTimeCustom);
            hospitalAddress = itemView.findViewById(R.id.textViewHospitalAddressCustom);
            hospitalDistance = itemView.findViewById(R.id.chipHospitalDistanceCustom);
            hospitalStars = itemView.findViewById(R.id.chipHospitalStarsCustom);
            //itemView.setTag(this);
            //itemView.setOnClickListener(onClickListener);
        }
    }
}
