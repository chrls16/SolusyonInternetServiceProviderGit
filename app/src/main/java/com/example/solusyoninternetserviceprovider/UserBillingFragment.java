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

        // 1. Listen to ServiceApplications in REAL-TIME
        mDatabase.child("ServiceApplications").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    String plan = snapshot.child("plan").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String installDateStr = snapshot.child("date").getValue(String.class);

                    updatePlanUI(plan);
                    calculateNextBillingDate(installDateStr);

                    // LOGIC: If status is "approved", admin marked them as Unpaid (due to overdue).
                    // If status is "completed", they are current/fully paid.
                    if ("approved".equalsIgnoreCase(status)) {
                        bannerOverdue.setVisibility(View.VISIBLE);
                    } else {
                        bannerOverdue.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Fetch Recent Transactions
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
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePlanUI(String planName) {
        if (planName == null) return;
        tvPlanTitle.setText(planName.toUpperCase() + " FIBER");

        // Fetch the Price from the 'Plans' master list based on name
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

            Calendar cal = Calendar.getInstance();
            cal.setTime(installDate);

            Calendar today = Calendar.getInstance();

            // Rolling Logic: Add 1 month repeatedly until the date is in the future
            while (cal.before(today)) {
                cal.add(Calendar.MONTH, 1);
            }

            // Set formatted date to UI
            if (tvNextBillingDate != null) {
                tvNextBillingDate.setText(sdf.format(cal.getTime()));
            }

        } catch (ParseException e) {
            e.printStackTrace();
            if (tvNextBillingDate != null) tvNextBillingDate.setText("N/A");
        }
    }
}