package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserBillingFragment extends Fragment {

    private RecyclerView rvActivity;
    private ActivityAdapter adapter;
    private List<UserActivityItem> billingList;

    private LinearLayout bannerOverdue;
    private TextView tvPlanTitle, tvPriceMonth;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_billing, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

        initViews(view);
        fetchBillingData();

        return view;
    }

    private void initViews(View v) {
        rvActivity = v.findViewById(R.id.rvRecentActivity);
        bannerOverdue = v.findViewById(R.id.bannerOverdue);
        tvPlanTitle = v.findViewById(R.id.tvPlanTitle);
        tvPriceMonth = v.findViewById(R.id.tvPriceMonth);

        rvActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        billingList = new ArrayList<>();
        adapter = new ActivityAdapter(billingList);
        rvActivity.setAdapter(adapter);

        // Hide banner by default
        bannerOverdue.setVisibility(View.GONE);
    }

    private void fetchBillingData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // 1. Fetch Subscription Data (Plan and Installation Date)
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String plan = snapshot.child("plan").getValue(String.class);
                    String installDateStr = snapshot.child("date").getValue(String.class); // Format: MMM dd, yyyy

                    updatePlanUI(plan);
                    checkOverdueStatus(installDateStr, uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Fetch Recent Transactions for rvRecentActivity
        mDatabase.child("Payments").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                billingList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String title = data.child("title").getValue(String.class);
                        String invoice = data.child("invoiceId").getValue(String.class);
                        String amount = data.child("amount").getValue(String.class);
                        String status = data.child("status").getValue(String.class);
                        billingList.add(0, new UserActivityItem(title, invoice, amount, status));
                    }
                } else {
                    // Default placeholder if no history exists
                    billingList.add(new UserActivityItem("Monthly Bill - Pending", "N/A", "₱ 0.00", "UNPAID"));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePlanUI(String plan) {
        if (plan == null) return;
        tvPlanTitle.setText(plan.toUpperCase() + " FIBER");
        if (plan.equalsIgnoreCase("Basic")) tvPriceMonth.setText("₱ 499.00/month");
        else if (plan.equalsIgnoreCase("Standard")) tvPriceMonth.setText("₱ 699.00/month");
        else if (plan.equalsIgnoreCase("Pro")) tvPriceMonth.setText("₱ 999.00/month");
    }

    private void checkOverdueStatus(String installDateStr, String uid) {
        if (installDateStr == null) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date installDate = sdf.parse(installDateStr);
            Calendar calInstall = Calendar.getInstance();
            calInstall.setTime(installDate);

            int dueDay = calInstall.get(Calendar.DAY_OF_MONTH);
            Calendar today = Calendar.getInstance();
            int currentDay = today.get(Calendar.DAY_OF_MONTH);

            // Logic: Overdue if today is PAST the installation day of the month
            if (currentDay > dueDay) {
                String currentMonthYear = new SimpleDateFormat("MM_yyyy", Locale.getDefault()).format(today.getTime());

                // Check Firebase 'Payments' to see if current month is paid
                mDatabase.child("Payments").child(uid).child(currentMonthYear).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.child("status").getValue(String.class);
                        if (status == null || !"PAID".equalsIgnoreCase(status)) {
                            bannerOverdue.setVisibility(View.VISIBLE);
                        } else {
                            bannerOverdue.setVisibility(View.GONE);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            } else {
                bannerOverdue.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}