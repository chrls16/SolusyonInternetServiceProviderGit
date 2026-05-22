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

    private ActivityAdapter activityAdapter;
    private List<UserActivityItem> paymentList = new ArrayList<>();

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

    private void setupTransactions() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        // Link with real ActivityAdapter used in Billing tab
        activityAdapter = new ActivityAdapter(paymentList);
        rvTransactions.setAdapter(activityAdapter);
        fetchPaymentHistory();
    }

    private void fetchActivePlanDetails() {
        String uid = mAuth.getUid();
        if (uid == null) return;

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
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUIByPlan(String planName, String date) {
        if (date != null) { tvDate.setText("Availed on " + date); }
        tvPlanName.setText(planName);

        mDatabase.child("Plans").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String dbPlanName = ds.child("name").getValue(String.class);
                    if (dbPlanName != null && dbPlanName.equalsIgnoreCase(planName)) {
                        String price = ds.child("price").getValue(String.class);
                        String speed = ds.child("speed").getValue(String.class);
                        String upload = ds.child("upload").getValue(String.class);

                        tvPlanPrice.setText("₱" + price);
                        tvDownloadSpeed.setText(speed + " Mbps");
                        tvUploadSpeed.setText(upload + " Mbps");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tvPlanPrice.setText("₱0.00");
                    tvDownloadSpeed.setText("0 Mbps");
                    tvUploadSpeed.setText("0 Mbps");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchPaymentHistory() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        mDatabase.child("Payments").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        UserActivityItem item = data.getValue(UserActivityItem.class);
                        if (item != null) paymentList.add(0, item); // latest first
                    }
                }
                if (isAdded()) {
                    activityAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}