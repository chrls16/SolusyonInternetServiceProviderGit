package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ClientDashboardFragment extends Fragment {

    private RecyclerView rvTransactions;
    private TextView tvPlanName, tvDate, tvPlanPrice;
    private TextView tvDownloadSpeed, tvUploadSpeed;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_dashboard, container, false);

        // Initialize Views
        tvPlanName = view.findViewById(R.id.tvPlanName);
        tvDate = view.findViewById(R.id.tvDate);
        tvPlanPrice = view.findViewById(R.id.tvPlanPrice);
        tvDownloadSpeed = view.findViewById(R.id.tvDownloadSpeed);
        tvUploadSpeed = view.findViewById(R.id.tvUploadSpeed);
        rvTransactions = view.findViewById(R.id.rvTransactions);

        // Announcements Button
        MaterialButton btnAnnouncements = view.findViewById(R.id.btnAnnouncements);
        if (btnAnnouncements != null) {
            btnAnnouncements.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new AnnouncementsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        setupTransactions();
        fetchActivePlanDetails();

        return view;
    }

    private void fetchActivePlanDetails() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // Listen to ServiceApplications for the current user's plan details
        mDatabase.child("ServiceApplications").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String planName = snapshot.child("plan").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);

                    if (planName != null) {
                        updateUIByPlan(planName, date);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUIByPlan(String planName, String date) {
        if (date != null) {
            tvDate.setText("Availed on " + date);
        }

        // Logic to set price and speed based on the plan type
        if (planName.equalsIgnoreCase("Basic")) {
            tvPlanName.setText("Basic Home Fiber");
            tvPlanPrice.setText("₱ 499.00");
            tvDownloadSpeed.setText("25 Mbps");
            tvUploadSpeed.setText("25 Mbps");
        } else if (planName.equalsIgnoreCase("Standard")) {
            tvPlanName.setText("Standard Plus Fiber");
            tvPlanPrice.setText("₱ 699.00");
            tvDownloadSpeed.setText("50 Mbps");
            tvUploadSpeed.setText("50 Mbps");
        } else if (planName.equalsIgnoreCase("Pro")) {
            tvPlanName.setText("Enterprise Pro Fiber");
            tvPlanPrice.setText("₱ 999.00");
            tvDownloadSpeed.setText("100 Mbps");
            tvUploadSpeed.setText("100 Mbps");
        } else {
            // Fallback for custom plans or unexpected values
            tvPlanName.setText(planName);
        }
    }

    private void setupTransactions() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        List<TransactionModel> list = new ArrayList<>();
        list.add(new TransactionModel("Invoice #SOL-9921", "Nov 01, 2023 • Paid via Visa", "$89.00"));
        list.add(new TransactionModel("Invoice #SOL-8845", "Oct 01, 2023 • Paid via Visa", "$89.00"));

        TransactionAdapter adapter = new TransactionAdapter(list);
        rvTransactions.setAdapter(adapter);
    }
}