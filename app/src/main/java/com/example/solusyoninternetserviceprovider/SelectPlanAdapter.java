package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class SelectPlanAdapter extends RecyclerView.Adapter<SelectPlanAdapter.ViewHolder> {

    private List<PlanModel> planList;
    private OnPlanSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnPlanSelectedListener {
        void onPlanSelected(PlanModel plan);
    }

    public SelectPlanAdapter(List<PlanModel> planList, OnPlanSelectedListener listener) {
        this.planList = planList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse the item_select_plan or a similar layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanModel plan = planList.get(position);
        holder.tvName.setText(plan.getName() + " ₱" + plan.getPrice());
        holder.tvSpeed.setText("Up to " + plan.getSpeed() + " Mbps");

        // Highlight selection
        if (selectedPosition == position) {
            holder.cardPlan.setStrokeWidth(4);
            holder.cardPlan.setStrokeColor(Color.parseColor("#2D62B5"));
        } else {
            holder.cardPlan.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onPlanSelected(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardPlan;
        TextView tvName, tvSpeed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPlan = (MaterialCardView) itemView;
            tvName = itemView.findViewById(R.id.tvPlanName);
            tvSpeed = itemView.findViewById(R.id.tvPlanSpeed);
        }
    }
}