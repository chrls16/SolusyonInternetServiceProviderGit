package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

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

        // 1. Basic Data Binding
        holder.tvName.setText(model.getName());
        holder.tvAccount.setText(model.getAccountNo());
        holder.tvInitials.setText(model.getInitials());
        holder.tvPlan.setText(model.getPlanName() + " (" + model.getPlanSpeed() + ")");
        holder.tvPlanType.setText(model.getPlanType());
        holder.tvPrice.setText(model.getPrice());
        holder.tvDate.setText("Installed: " + model.getDate());

        // 2. Status Color Styling
        if (model.getStatus().equalsIgnoreCase("Paid")) {
            holder.tvPrice.setTextColor(Color.parseColor("#2D62B5")); // Blue for Paid
        } else {
            holder.tvPrice.setTextColor(Color.parseColor("#B9392F")); // Red for Pending/Unpaid
        }

        // 3. EXPANSION LOGIC: Show/Hide action buttons based on model state
        holder.layoutActions.setVisibility(model.isExpanded() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            boolean currentState = model.isExpanded();
            model.setExpanded(!currentState);
            notifyItemChanged(holder.getAdapterPosition());
        });

        // 4. ACTION BUTTONS: Firebase Update Logic
        holder.btnPaid.setOnClickListener(v -> {
            String uid = model.getUserId();
            if (uid == null) return;

            // A. Update the main status in ServiceApplications
            updateFirebaseStatus(uid, "completed");

            // B. Create a permanent Payment History record in the Payments node
            String invoiceId = "INV-" + (int)(Math.random() * 9000 + 1000);
            UserActivityItem historyRecord = new UserActivityItem(
                    "Monthly Bill - Paid",
                    invoiceId,
                    model.getPrice(),
                    "PAID"
            );

            FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Payments")
                    .child(uid)
                    .push()
                    .setValue(historyRecord)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Payment Recorded for " + model.getName(), Toast.LENGTH_SHORT).show();
                    });

            model.setExpanded(false); // Close after action
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.btnUnpaid.setOnClickListener(v -> {
            String uid = model.getUserId();
            if (uid == null) return;

            // Mark as approved (which triggers the Overdue banner in the subscriber app)
            updateFirebaseStatus(uid, "approved");

            Toast.makeText(v.getContext(), model.getName() + " marked as Unpaid", Toast.LENGTH_SHORT).show();
            model.setExpanded(false); // Close
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    private void updateFirebaseStatus(String uid, String status) {
        if (uid == null) return;
        FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("ServiceApplications")
                .child(uid)
                .child("status")
                .setValue(status);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAccount, tvPlan, tvPlanType, tvInitials, tvPrice, tvDate;
        LinearLayout layoutActions;
        MaterialButton btnPaid, btnUnpaid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvPlan = itemView.findViewById(R.id.tvPlan);
            tvPlanType = itemView.findViewById(R.id.tvPlanType);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvDate);

            // Action UI Elements from item_billing_history.xml
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnPaid = itemView.findViewById(R.id.btnPaid);
            btnUnpaid = itemView.findViewById(R.id.btnUnpaid);
        }
    }
}
