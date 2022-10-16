package com.example.vaccination.ui.mainscreen.user.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vaccination.R;
import com.example.vaccination.data.Vaccine;
import com.example.vaccination.myInterface.MyRecyclerClickListener;
import com.example.vaccination.myInterface.RecyclerLongClickListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.lang.ref.WeakReference;
import java.util.List;

public class VaccineAdapter extends RecyclerView.Adapter<VaccineAdapter.ViewHolder> {

    private int[] colors;
    private int[] colorsBg;
    private int colorsLength;
    private List<Vaccine> list;
    private final Context context;
    private MyRecyclerClickListener onClickListener;
    private RecyclerLongClickListener longClickListener = null;

    public VaccineAdapter(Context context, List<Vaccine> list,
                          MyRecyclerClickListener onClickListener, RecyclerLongClickListener longClickListener) {
        this.list = list;
        this.context = context;
        this.onClickListener = onClickListener;
        this.longClickListener = longClickListener;
        colors = context.getResources().getIntArray(R.array.colorPallet2);
        colorsBg = context.getResources().getIntArray(R.array.colorPallet2Bg);
        colorsLength = colors.length;
    }

    public VaccineAdapter(Context context, List<Vaccine> list,
                          MyRecyclerClickListener onClickListener) {
        this.list = list;
        this.context = context;
        this.onClickListener = onClickListener;
        colors = context.getResources().getIntArray(R.array.colorPallet2);
        colorsBg = context.getResources().getIntArray(R.array.colorPallet2Bg);
        colorsLength = colors.length;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_vaccine_card, parent, false);
        return new ViewHolder(view, onClickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vaccine vaccine = list.get(position);
        holder.imageViewVaccine.setBackgroundColor(colors[position % colorsLength]);
        holder.vaccineCard.setCardBackgroundColor(colorsBg[position % colorsLength]);
        holder.vaccineCard.setStrokeColor(colors[position % colorsLength]);
        holder.textViewVaccineName.setText(vaccine.getName());
        holder.textViewVaccineDisease.setText(vaccine.getDisease());
        holder.textViewVaccineMinimumAge.setText(vaccine.getMinimumAgeFormattedString());


    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }


    public void setList(List<Vaccine> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ShapeableImageView imageViewVaccine;
        private TextView textViewVaccineName, textViewVaccineDisease, textViewVaccineMinimumAge;
        private Button buttonVaccineDetails;
        private MaterialCardView vaccineCard;
        private WeakReference<MyRecyclerClickListener> clickListener;
        private WeakReference<RecyclerLongClickListener> longClickListener = new WeakReference<>(null);

        public ViewHolder(@NonNull View itemView, MyRecyclerClickListener onClickListener, RecyclerLongClickListener longClickListener) {
            super(itemView);
            clickListener = new WeakReference<>(onClickListener);
            this.longClickListener = new WeakReference<>(longClickListener);
            imageViewVaccine = itemView.findViewById(R.id.shapeableImageView);
            textViewVaccineName = itemView.findViewById(R.id.textviewVaccineName);
            textViewVaccineDisease = itemView.findViewById(R.id.textViewVaccineDisease);
            textViewVaccineMinimumAge = itemView.findViewById(R.id.textViewVaccineAge);
            buttonVaccineDetails = itemView.findViewById(R.id.buttonVaccineDetails);
            vaccineCard = itemView.findViewById(R.id.customVaccineCard);
            itemView.setTag(this);
            if (longClickListener != null || this.longClickListener.get() != null) {
                itemView.setOnLongClickListener(this);
            }
            buttonVaccineDetails.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.get().onClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            longClickListener.get().onclick(getAdapterPosition(), v);
            return false;
        }
    }
}
