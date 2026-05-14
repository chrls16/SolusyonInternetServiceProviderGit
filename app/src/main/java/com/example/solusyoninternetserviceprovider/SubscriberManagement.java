package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubscriberManagement extends Fragment {

    private View barBagumbayan, barPalanas, barPobNorte, barPobSur, barTugos;
    private TextView tvPendingValue, tvTotalConValue, tvActiveConValue;
    private DatabaseReference mDatabase;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.subscriber_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

        // Start real-time listeners
        fetchDashboardStats();
        fetchSubscriberDistribution();
    }

    private void initViews(View view) {
        tvPendingValue = view.findViewById(R.id.tvPendingValue);
        tvTotalConValue = view.findViewById(R.id.tvTotalConValue);
        tvActiveConValue = view.findViewById(R.id.tvActiveConValue);

        barBagumbayan = view.findViewById(R.id.barBagumbayan);
        barPalanas = view.findViewById(R.id.barPalanas);
        barPobNorte = view.findViewById(R.id.barPobNorte);
        barPobSur = view.findViewById(R.id.barPobSur);
        barTugos = view.findViewById(R.id.barTugos);

        View cardPendingSetups = view.findViewById(R.id.cardPendingSetups);
        if (cardPendingSetups != null) {
            cardPendingSetups.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new PendingStatusFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    /**
     * Fetches counts for Total, Active, and Pending connections
     */
    private void fetchDashboardStats() {
        mDatabase.child("ServiceApplications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                int active = 0;
                int pending = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    total++; // Every application in this node is a connection record

                    if ("completed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
                        active++;
                    } else if ("pending".equalsIgnoreCase(status)) {
                        pending++;
                    }
                }

                if (isAdded()) {
                    tvTotalConValue.setText(String.valueOf(total));
                    tvActiveConValue.setText(String.valueOf(active));
                    tvPendingValue.setText(String.valueOf(pending));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Counts subscribers per barangay and updates the bar heights
     */
    private void fetchSubscriberDistribution() {
        mDatabase.child("ServiceApplications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int bBagumbayan = 0, bPalanas = 0, bPobNorte = 0, bPobSur = 0, bTugos = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Only count active/installed subscribers for the distribution graph
                    String status = ds.child("status").getValue(String.class);
                    if (!"completed".equalsIgnoreCase(status)) continue;

                    String barangay = ds.child("barangay").getValue(String.class);
                    if (barangay == null) continue;

                    barangay = barangay.toLowerCase().trim();

                    if (barangay.contains("bagumbayan")) bBagumbayan++;
                    else if (barangay.contains("palanas")) bPalanas++;
                    else if (barangay.contains("norte")) bPobNorte++;
                    else if (barangay.contains("sur")) bPobSur++;
                    else if (barangay.contains("tugos")) bTugos++;
                }

                // Determine the highest count to scale the bars proportionally
                int max = Math.max(bBagumbayan, Math.max(bPalanas,
                        Math.max(bPobNorte, Math.max(bPobSur, bTugos))));

                // Avoid division by zero, set a minimum scale floor
                if (max == 0) max = 1;

                if (isAdded()) {
                    // Max height of bars is 100dp as defined in XML
                    updateBar(barBagumbayan, bBagumbayan, max);
                    updateBar(barPalanas, bPalanas, max);
                    updateBar(barPobNorte, bPobNorte, max);
                    updateBar(barPobSur, bPobSur, max);
                    updateBar(barTugos, bTugos, max);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateBar(View bar, int count, int max) {
        // Calculate proportional height (min 5dp if count > 0 so it's visible)
        int maxHeight = 100; // DP
        int targetHeight = (int) (((float) count / max) * maxHeight);
        if (count > 0 && targetHeight < 10) targetHeight = 10;

        int pxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                targetHeight, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.height = pxHeight;
        bar.setLayoutParams(params);
    }
}