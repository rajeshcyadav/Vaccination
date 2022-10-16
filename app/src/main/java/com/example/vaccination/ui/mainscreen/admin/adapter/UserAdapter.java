package com.example.vaccination.ui.mainscreen.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vaccination.R;
import com.example.vaccination.data.BaseModel;
import com.example.vaccination.data.Index;
import com.example.vaccination.data.User;
import com.example.vaccination.ui.mainscreen.hospital.adapter.RequestAdapter;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<Index> list;
    private final Context context;
    private final int[] colors;
    private final int colorsSize;

    public UserAdapter(List<Index> list, Context context) {
        this.list = list;
        this.context = context;
        colors = context.getResources().getIntArray(R.array.colorPallet1);
        colorsSize = colors.length;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_user_card2, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Index index = list.get(position);

        holder.email.setText(index.getEmail());
        holder.uid.setText(index.getUid());
        holder.userType.setText(index.getUserType());
        holder.profileCompleted.setText(String.valueOf(index.isProfileCompleted()));
        holder.sideColor.setBackgroundColor(colors[position % colorsSize]);
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView email, uid, userType, profileCompleted;
        private final ShapeableImageView sideColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.textViewUserEmailCustom);
            uid = itemView.findViewById(R.id.textViewUserUidCustom);
            userType = itemView.findViewById(R.id.textViewUserTypeCustom);
            profileCompleted = itemView.findViewById(R.id.textViewUserProfileCompletedCustom);
            sideColor = itemView.findViewById(R.id.shapeableImageViewUserCustomColor);
        }
    }
}
