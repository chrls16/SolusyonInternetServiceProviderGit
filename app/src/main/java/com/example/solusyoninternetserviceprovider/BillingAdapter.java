package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.ViewHolder> {
    private List<BillingModel> list;

    public BillingAdapter(List<BillingModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_billing_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillingModel model = list.get(position);

        // Basic Info
        holder.tvName.setText(model.getName());
        holder.tvAccount.setText(model.getAccountNo());
        holder.tvInitials.setText(model.getInitials());

        // Plan Info
        holder.tvPlan.setText(model.getPlanName() + " " + model.getPlanSpeed());
        holder.tvPlanType.setText(model.getPlanType());

        // Price and Date Info
        holder.tvPrice.setText(model.getPrice());
        holder.tvDate.setText("Installed: " + model.getDate());

        // Status Color Logic: Blue for Paid, Red for Pending
        if (model.getStatus().equalsIgnoreCase("Paid")) {
            holder.tvPrice.setTextColor(Color.parseColor("#2D62B5"));
        } else {
            holder.tvPrice.setTextColor(Color.parseColor("#B9392F"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAccount, tvPlan, tvPlanType, tvInitials, tvPrice, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvPlan = itemView.findViewById(R.id.tvPlan);
            tvPlanType = itemView.findViewById(R.id.tvPlanType);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}