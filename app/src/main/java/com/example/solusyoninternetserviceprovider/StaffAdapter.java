package com.example.solusyoninternetserviceprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private List<StaffModel> staffList;

    public StaffAdapter(List<StaffModel> staffList) {
        this.staffList = staffList;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        StaffModel staff = staffList.get(position);

        holder.tvName.setText(staff.getName());
        holder.tvRoleNode.setText(staff.getRole() + " • " + staff.getNode());

        holder.btnMore.setOnClickListener(v -> {
            // Edit/Delete logic
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProfile;
        TextView tvName, tvRoleNode;
        ImageButton btnMore;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivStaffProfile);
            tvName = itemView.findViewById(R.id.tvStaffName);
            tvRoleNode = itemView.findViewById(R.id.tvStaffRoleNode);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}