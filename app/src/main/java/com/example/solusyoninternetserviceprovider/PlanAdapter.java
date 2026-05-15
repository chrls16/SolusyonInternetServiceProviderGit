package com.example.solusyoninternetserviceprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {

    private List<PlanModel> plans;
    private OnPlanClickListener listener;

    // Updated interface to include Delete
    public interface OnPlanClickListener {
        void onEditClick(PlanModel plan);
        void onDeleteClick(PlanModel plan, int position);
    }

    public PlanAdapter(List<PlanModel> plans, OnPlanClickListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanModel plan = plans.get(position);

        holder.tvName.setText(plan.getName());// Inside onBindViewHolder in PlanAdapter.java
        holder.tvDetails.setText(plan.getSpeed() + " Mbps • ₱" + plan.getPrice() + "/mo");
        holder.iconContainer.setCardBackgroundColor(plan.getColorRes());

        // Handle Edit Icon Click
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(plan);
            }
        });

        // Handle Delete Icon Click
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(plan, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        MaterialCardView iconContainer;
        ImageButton btnEdit, btnDelete; // Changed to ImageButton

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPlanName);
            tvDetails = itemView.findViewById(R.id.tvPlanDetails);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            btnEdit = itemView.findViewById(R.id.btnEditPlan);
            btnDelete = itemView.findViewById(R.id.btnDeletePlan);
        }
    }
}