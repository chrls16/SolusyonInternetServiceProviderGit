package com.example.solusyoninternetserviceprovider;
import android.os.Bundle; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup; import android.widget.ProgressBar; import android.widget.TextView;
import androidx.annotation.NonNull; import androidx.annotation.Nullable; import androidx.core.content.ContextCompat; import androidx.fragment.app.Fragment; import androidx.recyclerview.widget.LinearLayoutManager; import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot; import com.google.firebase.database.DatabaseError; import com.google.firebase.database.DatabaseReference; import com.google.firebase.database.FirebaseDatabase; import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat; import java.util.ArrayList; import java.util.List; import java.util.Locale;
public class BillingFragment extends Fragment {
    private RecyclerView recyclerView;
    private BillingAdapter adapter;
    private List<BillingModel> fullList = new ArrayList<>();
    private TextView tabAll, tabPaid, tabPending;
    private TextView tvTotalCollected;
    private ProgressBar progressBarCollected;

    private DatabaseReference mDatabase;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_billing, container, false);

        initViews(view);

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

        fetchSubscriberBillingData();

        return view;
    }

    private void initViews(View v) {
        recyclerView = v.findViewById(R.id.rvBillingHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        tabAll = v.findViewById(R.id.tabAll);
        tabPaid = v.findViewById(R.id.tabPaid);
        tabPending = v.findViewById(R.id.tabPending);

        // Stats in the top card (You may need to add these IDs to fragment_billing.xml)
        tvTotalCollected = v.findViewById(R.id.tvTotalCollected);
        progressBarCollected = v.findViewById(R.id.progressBarCollected);

        tabAll.setOnClickListener(v1 -> handleTabClick(tabAll, "All"));
        tabPaid.setOnClickListener(v1 -> handleTabClick(tabPaid, "Paid"));
        tabPending.setOnClickListener(v1 -> handleTabClick(tabPending, "Pending"));
    }

    private void fetchSubscriberBillingData() {
        mDatabase.child("ServiceApplications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                double totalRevenue = 0;
                int completedCount = 0;
                long totalRequests = snapshot.getChildrenCount();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = ds.child("fullName").getValue(String.class);
                    String appId = ds.child("applicationId").getValue(String.class);
                    String plan = ds.child("plan").getValue(String.class);
                    String status = ds.child("status").getValue(String.class);

                    // 1. Determine Speed and Formatted Plan Name
                    String speed = "0Mbps";
                    String formattedPlan = "Unknown Plan";
                    if (plan != null) {
                        if (plan.equalsIgnoreCase("Basic")) { speed = "25Mbps"; formattedPlan = "Basic Fiber"; }
                        else if (plan.equalsIgnoreCase("Standard")) { speed = "50Mbps"; formattedPlan = "Standard Fiber"; }
                        else if (plan.equalsIgnoreCase("Pro")) { speed = "100Mbps"; formattedPlan = "Pro Fiber"; }
                    }

                    // 2. Logic: Only include if admin marked as "completed" (done in calendar)
                    String billingStatus = "Pending";
                    if ("completed".equalsIgnoreCase(status)) {
                        billingStatus = "Paid";
                        completedCount++; // Count only finished installs
                        totalRevenue += getPriceFromPlan(plan); // Sum only finished installs
                    }

                    double price = getPriceFromPlan(plan);
                    String formattedPrice = "₱ " + String.format("%.2f", price);
                    String date = ds.child("date").getValue(String.class);
                    if (date == null) date = "N/A";

                    if (name != null && appId != null) {
                        fullList.add(new BillingModel(name, appId, formattedPlan, speed, "Residential", billingStatus, formattedPrice, date));
                    }
                }

                // Update the dashboard header with accurate financial data
                updateTopStats(totalRevenue, completedCount, totalRequests);
                filter("All");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private double getPriceFromPlan(String plan) {
        if (plan == null) return 0;
        if (plan.equalsIgnoreCase("Basic")) return 499.00;
        if (plan.equalsIgnoreCase("Standard")) return 699.00;
        if (plan.equalsIgnoreCase("Pro")) return 999.00;
        return 0;
    }

    private void updateTopStats(double revenueTotal, int completedNum, long totalRequests) {
        if (!isAdded()) return;

        // 1. Update the Revenue Text
        if (tvTotalCollected != null) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            tvTotalCollected.setText(formatter.format(revenueTotal));
        }

        // 2. Update the Progress Bar
        // Now it shows what percentage of your applicants have been successfully installed (completed)
        if (progressBarCollected != null && totalRequests > 0) {
            int percentage = (int) (((float) completedNum / totalRequests) * 100);
            progressBarCollected.setProgress(percentage);
        }
    }


    private void handleTabClick(TextView selectedTab, String status) {
        resetTabStyles();
        selectedTab.setBackgroundResource(R.drawable.bg_tab_active);
        selectedTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        filter(status);
    }

    private void resetTabStyles() {
        tabAll.setBackground(null);
        tabPaid.setBackground(null);
        tabPending.setBackground(null);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.tab_text_inactive);
        tabAll.setTextColor(inactiveColor);
        tabPaid.setTextColor(inactiveColor);
        tabPending.setTextColor(inactiveColor);
    }

    private void filter(String status) {
        List<BillingModel> filteredList = new ArrayList<>();
        for (BillingModel item : fullList) {
            // Logic: Show all if "All" is selected, otherwise match "Paid" or "Pending"
            if (status.equalsIgnoreCase("All") || item.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(item);
            }
        }

        // FIX: Instead of creating a new adapter, check if one exists
        if (adapter == null) {
            adapter = new BillingAdapter(filteredList);
            recyclerView.setAdapter(adapter);
        } else {
            // You'll need a method in your adapter to update the list,
            // or just re-set it like this for now:
            adapter = new BillingAdapter(filteredList);
            recyclerView.setAdapter(adapter);
        }
    }
}