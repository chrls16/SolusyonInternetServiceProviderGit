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
    private TextView tvPlanTitle, tvPriceMonth, tvTotalDueAmount, tvNextBillingDate;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private String mInstallDateStr;

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
        tvTotalDueAmount = v.findViewById(R.id.tvTotalDueAmount);
        tvNextBillingDate = v.findViewById(R.id.tvNextBillingDate);

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

        // 1. Listen to ServiceApplications (Installation Info)
        mDatabase.child("ServiceApplications").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    String plan = snapshot.child("plan").getValue(String.class);
                    mInstallDateStr = snapshot.child("date").getValue(String.class);

                    updatePlanUI(plan);
                    calculateNextBillingDate(mInstallDateStr);
                    evaluateOverdueBanner();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Listen to Payments (Transaction History)
        mDatabase.child("Payments").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                billingList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        UserActivityItem item = data.getValue(UserActivityItem.class);
                        if (item != null) billingList.add(0, item);
                    }
                }
                adapter.notifyDataSetChanged();
                evaluateOverdueBanner();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void evaluateOverdueBanner() {
        if (mInstallDateStr == null || billingList == null || !isAdded()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat monthYearSdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

            Date installDate = sdf.parse(mInstallDateStr);
            if (installDate == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(installDate);
            // Ignore time for comparison
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);

            boolean isOverdue = false;

            // First billing deadline is 1 month after installation
            cal.add(Calendar.MONTH, 1);

            // Loop through all billing deadlines that have already passed
            while (cal.before(today)) {
                // Identify the billed month (the month prior to the deadline)
                Calendar billedMonthCal = (Calendar) cal.clone();
                billedMonthCal.add(Calendar.MONTH, -1);
                String targetMonth = monthYearSdf.format(billedMonthCal.getTime());

                boolean hasPaidForThisMonth = false;
                for (UserActivityItem payment : billingList) {
                    if (targetMonth.equalsIgnoreCase(payment.getMonth()) && "PAID".equalsIgnoreCase(payment.getStatus())) {
                        hasPaidForThisMonth = true;
                        break;
                    }
                }

                if (!hasPaidForThisMonth) {
                    isOverdue = true;
                    break; // Stop at the first missed payment
                }

                cal.add(Calendar.MONTH, 1); // Check next month
            }

            bannerOverdue.setVisibility(isOverdue ? View.VISIBLE : View.GONE);

        } catch (ParseException e) {
            e.printStackTrace();
            bannerOverdue.setVisibility(View.GONE);
        }
    }

    private void updatePlanUI(String planName) {
        if (planName == null) return;
        tvPlanTitle.setText(planName.toUpperCase() + " FIBER");

        mDatabase.child("Plans").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String dbPlanName = ds.child("name").getValue(String.class);
                    if (dbPlanName != null && dbPlanName.equalsIgnoreCase(planName)) {
                        String price = ds.child("price").getValue(String.class);
                        tvPriceMonth.setText("₱ " + price + "/month");
                        if (tvTotalDueAmount != null) tvTotalDueAmount.setText("₱ " + price);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tvPriceMonth.setText("₱ 0.00/month");
                    if (tvTotalDueAmount != null) tvTotalDueAmount.setText("₱ 0.00");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void calculateNextBillingDate(String installDateStr) {
        if (installDateStr == null || installDateStr.isEmpty()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date installDate = sdf.parse(installDateStr);
            if (installDate == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(installDate);
            Calendar today = Calendar.getInstance();

            while (cal.before(today) || cal.equals(today)) {
                cal.add(Calendar.MONTH, 1);
            }

            if (tvNextBillingDate != null) {
                tvNextBillingDate.setText(sdf.format(cal.getTime()));
            }

        } catch (ParseException e) {
            if (tvNextBillingDate != null) tvNextBillingDate.setText("N/A");
        }
    }
}