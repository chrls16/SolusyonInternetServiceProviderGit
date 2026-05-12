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

public class ClientDashboardFragment extends Fragment {

    private RecyclerView rvTransactions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_dashboard, container, false);

        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        List<TransactionModel> list = new ArrayList<>();
        list.add(new TransactionModel("Invoice #SOL-9921", "Nov 01, 2023 • Paid via Visa", "$89.00"));
        list.add(new TransactionModel("Invoice #SOL-8845", "Oct 01, 2023 • Paid via Visa", "$89.00"));
        list.add(new TransactionModel("One-time Installation", "Oct 12, 2023 • Setup Fee", "$45.00"));

        TransactionAdapter adapter = new TransactionAdapter(list);
        rvTransactions.setAdapter(adapter);

        return view;
    }
}