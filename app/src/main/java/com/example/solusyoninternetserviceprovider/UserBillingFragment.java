package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UserBillingFragment extends Fragment {

    private RecyclerView rvActivity;
    private ActivityAdapter adapter;
    private List<UserActivityItem> billingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the layout
        View view = inflater.inflate(R.layout.user_billing, container, false);

        // 2. Initialize the RecyclerView with the ID from [user_billing.xml](file:///Users/chrleskndrick/AndroidStudioProjects/SolusyonISP/app/src/main/res/layout/user_billing.xml)
        rvActivity = view.findViewById(R.id.rvRecentActivity);

        if (rvActivity != null) {
            rvActivity.setLayoutManager(new LinearLayoutManager(getContext()));

            // 3. Initialize data list
            billingList = new ArrayList<>();
            billingList.add(new UserActivityItem("Monthly Service - Sept", "Invoice #INV-2023-09", "$89.90", "UNPAID"));
            billingList.add(new UserActivityItem("Late Fee Penalty", "Adjustment", "$34.60", "UNPAID"));
            billingList.add(new UserActivityItem("Monthly Service - Aug", "Invoice #INV-2023-08", "$89.90", "PAID"));

            // 4. Set up the adapter
            adapter = new ActivityAdapter(billingList);
            rvActivity.setAdapter(adapter);
        }

        return view;
    }
}