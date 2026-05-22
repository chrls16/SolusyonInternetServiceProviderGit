package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.graphics.Typeface;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private TextView tvTotalSubscribers, tvMonthlyRevenue, tvAnnualRevenue;
    private TextView btnFilter6Months, btnFilter1Year;
    private RecyclerView rvBarangayDistribution;
    private DatabaseReference dbRef;
    private LineChart lineChart;
    private LinearLayout monthContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        // 1. Initialize Views
        tvTotalSubscribers = view.findViewById(R.id.tvTotalSubscribers);
        tvMonthlyRevenue = view.findViewById(R.id.tvMonthlyRevenue);
        tvAnnualRevenue = view.findViewById(R.id.tvAnnualRevenue);
        lineChart = view.findViewById(R.id.reportingLineChart);
        monthContainer = view.findViewById(R.id.monthContainer);
        rvBarangayDistribution = view.findViewById(R.id.rvBarangayDistribution);

        btnFilter6Months = view.findViewById(R.id.btnFilter6Months);
        btnFilter1Year = view.findViewById(R.id.btnFilter1Year);

        rvBarangayDistribution.setLayoutManager(new LinearLayoutManager(getContext()));

        dbRef = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        setupMonthClickListeners(view);
        setupFilterClickListeners();
        setupLineChart();

        // 2. Load Real-time Data
        fetchRealtimeStats();

        return view;
    }

    private void fetchRealtimeStats() {
        // A. SUBSCRIBER COUNT & DYNAMIC BARANGAY LISTING
        dbRef.child("ServiceApplications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int activeSubscribers = 0;
                Map<String, Integer> barangayCounts = new HashMap<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if ("completed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
                        activeSubscribers++;

                        String barangay = ds.child("barangay").getValue(String.class);
                        if (barangay != null) {
                            String normalized = barangay.toLowerCase().trim();
                            barangayCounts.put(normalized, barangayCounts.getOrDefault(normalized, 0) + 1);
                        }
                    }
                }

                if (isAdded()) {
                    tvTotalSubscribers.setText(String.valueOf(activeSubscribers));
                    updateBarangayDistributionList(barangayCounts, activeSubscribers);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // B. REVENUE CALCULATION
        dbRef.child("Payments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalAmount = 0;
                for (DataSnapshot userNode : snapshot.getChildren()) {
                    for (DataSnapshot payment : userNode.getChildren()) {
                        String status = payment.child("status").getValue(String.class);
                        String amountStr = payment.child("amount").getValue(String.class);
                        if ("PAID".equalsIgnoreCase(status) && amountStr != null) {
                            totalAmount += parseCurrency(amountStr);
                        }
                    }
                }

                if (isAdded()) {
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
                    tvMonthlyRevenue.setText(formatter.format(totalAmount));
                    tvAnnualRevenue.setText(formatter.format(totalAmount));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateBarangayDistributionList(Map<String, Integer> counts, int total) {
        if (total == 0 || !isAdded()) return;

        // Convert Map to sorted list (Highest count first)
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(counts.entrySet());
        Collections.sort(sortedList, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Set the adapter to show ALL barangays in the list
        BarangayReportAdapter adapter = new BarangayReportAdapter(sortedList, total);
        rvBarangayDistribution.setAdapter(adapter);
    }

    private double parseCurrency(String amountStr) {
        try {
            return Double.parseDouble(amountStr.replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void setupFilterClickListeners() {
        btnFilter6Months.setOnClickListener(v -> {
            updateFilterUI(btnFilter6Months, btnFilter1Year);
            updateChartData(6);
        });
        btnFilter1Year.setOnClickListener(v -> {
            updateFilterUI(btnFilter1Year, btnFilter6Months);
            updateChartData(12);
        });
    }

    private void updateFilterUI(TextView active, TextView inactive) {
        active.setBackgroundResource(R.drawable.bg_filter_selected);
        active.setTextColor(Color.parseColor("#0E3C7E"));
        active.setTypeface(null, Typeface.BOLD);
        inactive.setBackground(null);
        inactive.setTextColor(Color.parseColor("#64748B"));
        inactive.setTypeface(null, Typeface.NORMAL);
    }

    private void setupMonthClickListeners(View view) {
        int[] monthIds = {R.id.monthJan, R.id.monthFeb, R.id.monthMar, R.id.monthApr, R.id.monthMay, R.id.monthJun, R.id.monthJul, R.id.monthAug, R.id.monthSep, R.id.monthOct, R.id.monthNov, R.id.monthDec};
        for (int id : monthIds) {
            TextView monthTv = view.findViewById(id);
            if (monthTv != null) {
                if (id == R.id.monthJun) highlightSelectedMonth(monthTv);
                monthTv.setOnClickListener(v -> highlightSelectedMonth(monthTv));
            }
        }
    }

    private void highlightSelectedMonth(TextView selectedMonth) {
        for (int i = 0; i < monthContainer.getChildCount(); i++) {
            View v = monthContainer.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(Color.parseColor("#64748B"));
                v.setBackground(null);
            }
        }
        selectedMonth.setTextColor(Color.parseColor("#0E3C7E"));
        selectedMonth.setBackgroundResource(R.drawable.bg_month_selected);
    }

    private void setupLineChart() { updateChartData(6); }

    private void updateChartData(int range) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < range; i++) entries.add(new Entry(i, (float) (Math.random() * 40) + 40));

        LineDataSet dataSet = new LineDataSet(entries, "Subscription Growth");
        dataSet.setColor(Color.parseColor("#0E3C7E"));
        dataSet.setCircleColor(Color.parseColor("#0E3C7E"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E0F2FE"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.setData(new LineData(dataSet));
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.animateX(800);
        lineChart.invalidate();
    }
}