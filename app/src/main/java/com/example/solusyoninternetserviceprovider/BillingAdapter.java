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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

            holder.btnPaid.setEnabled(false);
            holder.btnPaid.setText("Checking...");

            DatabaseReference db = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();

            try {
                SimpleDateFormat monthYearSdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                Date currentSelectedDate = monthYearSdf.parse(selectedMonth);

                Calendar cal = Calendar.getInstance();
                if (currentSelectedDate != null) cal.setTime(currentSelectedDate);
                cal.add(Calendar.MONTH, -1);
                String previousMonthStr = monthYearSdf.format(cal.getTime());

                // Check if there WAS a previous month for this user based on installation
                Date installDate = null;
                try {
                    installDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(model.getDate());
                } catch (Exception e) {
                    installDate = new SimpleDateFormat("M/d/yyyy", Locale.getDefault()).parse(model.getDate());
                }

                if (installDate != null) {
                    Calendar calInstall = Calendar.getInstance();
                    calInstall.setTime(installDate);
                    calInstall.set(Calendar.DAY_OF_MONTH, 1);
                    calInstall.set(Calendar.HOUR_OF_DAY, 0); calInstall.set(Calendar.MINUTE, 0); calInstall.set(Calendar.SECOND, 0); calInstall.set(Calendar.MILLISECOND, 0);

                    Calendar calPrev = Calendar.getInstance();
                    if (currentSelectedDate != null) calPrev.setTime(currentSelectedDate);
                    calPrev.add(Calendar.MONTH, -1);
                    calPrev.set(Calendar.DAY_OF_MONTH, 1);
                    calPrev.set(Calendar.HOUR_OF_DAY, 0); calPrev.set(Calendar.MINUTE, 0); calPrev.set(Calendar.SECOND, 0); calPrev.set(Calendar.MILLISECOND, 0);

                    if (calPrev.before(calInstall)) {
                        // This is their first billing month, no previous check needed
                        recordPayment(db, uid, model, holder);
                    } else {
                        // Must check database for previous month payment
                        db.child("Payments").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean hasPaidPrevious = false;
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String m = ds.child("month").getValue(String.class);
                                    String s = ds.child("status").getValue(String.class);
                                    if (previousMonthStr.equalsIgnoreCase(m) && "PAID".equalsIgnoreCase(s)) {
                                        hasPaidPrevious = true;
                                        break;
                                    }
                                }

                                if (hasPaidPrevious) {
                                    recordPayment(db, uid, model, holder);
                                } else {
                                    holder.btnPaid.setEnabled(true);
                                    holder.btnPaid.setText("Mark as Paid");
                                    Toast.makeText(v.getContext(), "The subscriber hasn't paid last month's subscription yet.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override public void onCancelled(@NonNull DatabaseError error) {
                                holder.btnPaid.setEnabled(true);
                                holder.btnPaid.setText("Mark as Paid");
                            }
                        });
                    }
                } else {
                    recordPayment(db, uid, model, holder);
                }
            } catch (Exception e) {
                recordPayment(db, uid, model, holder);
            }
        });
    }

    private void recordPayment(DatabaseReference db, String uid, BillingModel model, ViewHolder holder) {
        String invoiceId = "INV-" + (int)(Math.random() * 9000 + 1000);
        holder.btnPaid.setText("Processing...");

        UserActivityItem historyRecord = new UserActivityItem(
                "Monthly Bill - Paid",
                invoiceId,
                model.getPrice(),
                "PAID",
                selectedMonth,
                model.getBillingDate()
        );

        db.child("Payments").child(uid).push().setValue(historyRecord).addOnSuccessListener(aVoid -> {
            db.child("ServiceApplications").child(uid).child("status").setValue("completed").addOnSuccessListener(aVoid2 -> {
                Toast.makeText(holder.itemView.getContext(), "Payment confirmed for " + selectedMonth, Toast.LENGTH_SHORT).show();
                model.setExpanded(false);
                holder.btnPaid.setEnabled(true);
                holder.btnPaid.setText("Mark as Paid");
                notifyItemChanged(holder.getAdapterPosition());
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