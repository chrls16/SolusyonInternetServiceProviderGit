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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.ViewHolder> {
    private List<BillingModel> list;
    private String selectedMonth;

    public BillingAdapter(List<BillingModel> list, String selectedMonth) {
        this.list = list;
        this.selectedMonth = selectedMonth;
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
        holder.tvName.setText(model.getName());
        holder.tvAccount.setText(model.getAccountNo());
        holder.tvInitials.setText(model.getInitials());
        holder.tvPlan.setText(model.getPlanName());
        holder.tvPlanType.setText(model.getPlanType());
        holder.tvPrice.setText(model.getPrice());
        holder.tvDate.setText("Installed: " + model.getDate());

        // STATUS Badge logic (Beside Name)
        if ("Paid".equalsIgnoreCase(model.getStatus())) {
            holder.tvPaidBadge.setVisibility(View.VISIBLE);
            holder.tvPaidBadge.setText("PAID");
            holder.tvPaidBadge.setBackgroundResource(R.drawable.bg_badge_paid);
            holder.tvPaidBadge.setTextColor(Color.parseColor("#059669"));
            holder.layoutActions.setVisibility(View.GONE);
            holder.tvPrice.setTextColor(Color.parseColor("#2D62B5"));
        } else if ("Overdue".equalsIgnoreCase(model.getStatus())) {
            holder.tvPaidBadge.setVisibility(View.VISIBLE);
            holder.tvPaidBadge.setText("OVERDUE");
            holder.tvPaidBadge.setBackgroundResource(R.drawable.bg_overdue_banner);
            holder.tvPaidBadge.setTextColor(Color.parseColor("#B91C1C"));
            holder.tvPrice.setTextColor(Color.parseColor("#B91C1C"));
            holder.layoutActions.setVisibility(model.isExpanded() ? View.VISIBLE : View.GONE);
        } else {
            holder.tvPaidBadge.setVisibility(View.GONE);
            holder.tvPrice.setTextColor(Color.parseColor("#B9392F"));
            holder.layoutActions.setVisibility(model.isExpanded() ? View.VISIBLE : View.GONE);
        }

        holder.mainContent.setOnClickListener(v -> {
            if (!"Paid".equalsIgnoreCase(model.getStatus())) {
                boolean currentState = model.isExpanded();
                model.setExpanded(!currentState);
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.btnPaid.setOnClickListener(v -> {
            String uid = model.getUserId();
            if (uid == null) return;
            DatabaseReference db = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
            String invoiceId = "INV-" + (int)(Math.random() * 9000 + 1000);

            // Record the payment with the specific billing date for this month
            UserActivityItem historyRecord = new UserActivityItem(
                    "Monthly Bill - Paid",
                    invoiceId,
                    model.getPrice(),
                    "PAID",
                    selectedMonth,
                    model.getBillingDate()
            );

            holder.btnPaid.setEnabled(false); holder.btnPaid.setText("Processing...");
            db.child("Payments").child(uid).push().setValue(historyRecord).addOnSuccessListener(aVoid -> {
                db.child("ServiceApplications").child(uid).child("status").setValue("completed").addOnSuccessListener(aVoid2 -> {
                    Toast.makeText(v.getContext(), "Payment confirmed for " + selectedMonth, Toast.LENGTH_SHORT).show();
                    model.setExpanded(false); holder.btnPaid.setEnabled(true); holder.btnPaid.setText("Mark as Paid");
                });
            });
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAccount, tvPlan, tvPlanType, tvInitials, tvPrice, tvDate, tvPaidBadge;
        LinearLayout layoutActions, mainContent;
        MaterialButton btnPaid;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            tvPlan = itemView.findViewById(R.id.tvPlan);
            tvPlanType = itemView.findViewById(R.id.tvPlanType);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPaidBadge = itemView.findViewById(R.id.tvPaidBadge);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            mainContent = itemView.findViewById(R.id.mainContent);
            btnPaid = itemView.findViewById(R.id.btnPaid);
        }
    }
}