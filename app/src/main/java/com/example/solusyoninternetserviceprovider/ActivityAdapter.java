package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private List<UserActivityItem> activityList;

    public ActivityAdapter(List<UserActivityItem> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_billing_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserActivityItem item = activityList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvId.setText(item.getInvoiceId());
        holder.tvAmount.setText(item.getAmount());
        holder.tvStatus.setText(item.getStatus());

        // Bind the Billing Date to the transaction
        if (item.getBillingDate() != null) {
            holder.tvActBillingDate.setText("Billing Date: " + item.getBillingDate());
            holder.tvActBillingDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvActBillingDate.setVisibility(View.GONE);
        }

        // Color Logic: Red for Unpaid, Blue for Paid
        if (item.getStatus().equalsIgnoreCase("UNPAID")) {
            holder.tvStatus.setTextColor(Color.parseColor("#B9392F"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#2D62B5"));
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvId, tvAmount, tvStatus, tvActBillingDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvActTitle);
            tvId = itemView.findViewById(R.id.tvActId);
            tvAmount = itemView.findViewById(R.id.tvActAmount);
            tvStatus = itemView.findViewById(R.id.tvActStatus);
            tvActBillingDate = itemView.findViewById(R.id.tvActBillingDate);
        }
    }
}