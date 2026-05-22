package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;

public class BillingFragment extends Fragment {

    private RecyclerView recyclerView;
    private BillingAdapter adapter;
    private List<BillingModel> fullList = new ArrayList<>();
    private TextView tabAll, tabPaid, tabPending, tvTotalCollected, tvSelectedMonth;
    private ProgressBar progressBarCollected;
    private String selectedMonth;
    private DatabaseReference mDatabase;
    private DataSnapshot serviceSnapshot, paymentSnapshot, plansSnapshot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_billing, container, false);
        initViews(view);
        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        fetchSubscriberBillingData();
        return view;
    }

    private void initViews(View v) {
        recyclerView = v.findViewById(R.id.rvBillingHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tabAll = v.findViewById(R.id.tabAll);
        tabPaid = v.findViewById(R.id.tabPaid);
        tabPending = v.findViewById(R.id.tabPending);
        tvTotalCollected = v.findViewById(R.id.tvTotalCollected);
        tvSelectedMonth = v.findViewById(R.id.tvSelectedMonth);
        progressBarCollected = v.findViewById(R.id.progressBarCollected);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        selectedMonth = sdf.format(new Date());
        tvSelectedMonth.setText(selectedMonth);
        tvSelectedMonth.setOnClickListener(v1 -> showMonthPicker());

        tabAll.setOnClickListener(v1 -> handleTabClick(tabAll, "All"));
        tabPaid.setOnClickListener(v1 -> handleTabClick(tabPaid, "Paid"));
        tabPending.setOnClickListener(v1 -> handleTabClick(tabPending, "Pending"));
        handleTabClick(tabAll, "All");
    }

    private void fetchSubscriberBillingData() {
        mDatabase.child("ServiceApplications").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { serviceSnapshot = s; triggerDataProcess(); }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        mDatabase.child("Payments").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { paymentSnapshot = s; triggerDataProcess(); }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        mDatabase.child("Plans").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { plansSnapshot = s; triggerDataProcess(); }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void triggerDataProcess() {
        // FIX: Ensure both Applications and Plans are loaded before starting processing
        if (serviceSnapshot != null && plansSnapshot != null) {
            processBillingData(serviceSnapshot, paymentSnapshot, plansSnapshot);
        }
    }

    private void processBillingData(DataSnapshot serviceSnapshot, DataSnapshot paymentSnapshot, DataSnapshot plansSnapshot) {
        fullList.clear();
        double totalRevenue = 0;
        int completedCount = 0;
        long totalRequests = serviceSnapshot.getChildrenCount();

        for (DataSnapshot ds : serviceSnapshot.getChildren()) {
            String userId = ds.getKey();
            String name = ds.child("fullName").getValue(String.class);
            if (name == null) name = "Unknown User";
            String appId = ds.child("applicationId").getValue(String.class);
            String plan = ds.child("plan").getValue(String.class);
            String installDateStr = ds.child("date").getValue(String.class);

            String billingStatus = calculateStatus(userId, installDateStr, paymentSnapshot);

            String speed = "0Mbps";
            double price = 0;
            String formattedPlan = (plan != null) ? plan : "Unknown Plan";

            // Dynamic Matching Logic
            if (plan != null) {
                for (DataSnapshot pDs : plansSnapshot.getChildren()) {
                    String dbPlanName = pDs.child("name").getValue(String.class);
                    if (dbPlanName != null && dbPlanName.equalsIgnoreCase(plan)) {
                        String dbSpeed = pDs.child("speed").getValue(String.class);
                        String dbPrice = pDs.child("price").getValue(String.class);
                        if (dbSpeed != null) speed = dbSpeed + "Mbps";
                        if (dbPrice != null) {
                            try { price = Double.parseDouble(dbPrice.replace(",", "")); } catch (Exception e) { price = 0; }
                        }
                        if (!formattedPlan.toLowerCase().contains("fiber")) formattedPlan += " Fiber";
                        break;
                    }
                }
            }

            String billingDateStr = "N/A";
            if (installDateStr != null && !installDateStr.equals("N/A")) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    Date installDate = null;
                    try { installDate = sdf.parse(installDateStr); } catch (Exception e) {
                        installDate = new SimpleDateFormat("M/d/yyyy", Locale.getDefault()).parse(installDateStr);
                    }
                    Date targetMonthDate = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).parse(selectedMonth);
                    if (installDate != null && targetMonthDate != null) {
                        Calendar calInstall = Calendar.getInstance(); calInstall.setTime(installDate);
                        Calendar calBilling = Calendar.getInstance(); calBilling.setTime(targetMonthDate);
                        calBilling.set(Calendar.DAY_OF_MONTH, calInstall.get(Calendar.DAY_OF_MONTH));
                        billingDateStr = sdf.format(calBilling.getTime());
                    }
                } catch (Exception e) { billingDateStr = "N/A"; }
            }

            if ("Paid".equalsIgnoreCase(billingStatus)) { completedCount++; totalRevenue += price; }
            String formattedPrice = "₱ " + String.format(Locale.getDefault(), "%.2f", price);
            if (installDateStr == null) installDateStr = "N/A";

            fullList.add(new BillingModel(userId, name, appId, formattedPlan, speed, "Residential", billingStatus, formattedPrice, installDateStr, billingDateStr));
        }

        updateTopStats(totalRevenue, completedCount, totalRequests);
        filterCurrentTab();
    }

    private void filterCurrentTab() {
        String currentTab = "All";
        if (tabPaid.getBackground() != null) currentTab = "Paid";
        else if (tabPending.getBackground() != null) currentTab = "Pending";
        filter(currentTab);
    }

    private String calculateStatus(String userId, String installDateStr, DataSnapshot paymentSnapshot) {
        if (paymentSnapshot != null && paymentSnapshot.hasChild(userId)) {
            for (DataSnapshot p : paymentSnapshot.child(userId).getChildren()) {
                String paidMonth = p.child("month").getValue(String.class);
                if (selectedMonth != null && selectedMonth.equalsIgnoreCase(paidMonth)) return "Paid";
            }
        }
        if (installDateStr == null || installDateStr.isEmpty() || installDateStr.equals("N/A")) return "Pending";
        try {
            Date installDate = null;
            try { installDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(installDateStr); } catch (Exception e) {
                installDate = new SimpleDateFormat("M/d/yyyy", Locale.getDefault()).parse(installDateStr);
            }
            Date targetMonthDate = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).parse(selectedMonth);
            if (installDate == null || targetMonthDate == null) return "Pending";
            Calendar calTarget = Calendar.getInstance(); calTarget.setTime(targetMonthDate);
            Calendar calInstall = Calendar.getInstance(); calInstall.setTime(installDate);
            calTarget.set(Calendar.DAY_OF_MONTH, calInstall.get(Calendar.DAY_OF_MONTH));
            return Calendar.getInstance().after(calTarget) ? "Overdue" : "Pending";
        } catch (Exception e) { return "Pending"; }
    }

    private void updateTopStats(double revenueTotal, int completedNum, long totalRequests) {
        if (!isAdded()) return;
        if (tvTotalCollected != null) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            tvTotalCollected.setText(formatter.format(revenueTotal));
        }
        if (progressBarCollected != null && totalRequests > 0) {
            progressBarCollected.setProgress((int) (((float) completedNum / totalRequests) * 100));
        }
    }

    private void handleTabClick(TextView selectedTab, String status) {
        resetTabStyles();
        selectedTab.setBackgroundResource(R.drawable.bg_tab_active);
        selectedTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        filter(status);
    }

    private void resetTabStyles() {
        tabAll.setBackground(null); tabPaid.setBackground(null); tabPending.setBackground(null);
        if (isAdded()) {
            int inactive = ContextCompat.getColor(requireContext(), R.color.tab_text_inactive);
            tabAll.setTextColor(inactive); tabPaid.setTextColor(inactive); tabPending.setTextColor(inactive);
        }
    }

    private void showMonthPicker() {
        String[] months = new String[12];
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        for (int i = 0; i < 12; i++) { cal.set(Calendar.MONTH, i); months[i] = sdf.format(cal.getTime()); }
        new AlertDialog.Builder(requireContext()).setTitle("Select Month").setItems(months, (dialog, which) -> {
            selectedMonth = months[which]; tvSelectedMonth.setText(selectedMonth); triggerDataProcess();
        }).show();
    }

    private void filter(String status) {
        List<BillingModel> filteredList = new ArrayList<>();
        for (BillingModel item : fullList) {
            if (status.equalsIgnoreCase("All")) filteredList.add(item);
            else if (status.equalsIgnoreCase("Paid") && item.getStatus().equalsIgnoreCase("Paid")) filteredList.add(item);
            else if (status.equalsIgnoreCase("Pending") && (item.getStatus().equalsIgnoreCase("Pending") || item.getStatus().equalsIgnoreCase("Overdue"))) filteredList.add(item);
        }
        adapter = new BillingAdapter(filteredList, selectedMonth);
        recyclerView.setAdapter(adapter);
    }
}