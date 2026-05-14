package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PlanManagementFragment extends Fragment {

    private EditText etName, etPrice, etDownload, etUpload;
    private MaterialButton btnSaveChanges, btnCancel;
    private RecyclerView rvPlans;
    private PlanAdapter adapter;
    private List<PlanModel> planList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_management, container, false);

        // 1. Initialize Edit Form Views
        etName = view.findViewById(R.id.etPlanName);
        etPrice = view.findViewById(R.id.etPrice);
        etDownload = view.findViewById(R.id.etDownload);
        etUpload = view.findViewById(R.id.etUpload);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnCancel = view.findViewById(R.id.btnCancel);

        // 2. Setup RecyclerView
        rvPlans = view.findViewById(R.id.rvPlans);
        rvPlans.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Prepare Data (Matching image_aec3d7.png)
        planList = new ArrayList<>();
        planList.add(new PlanModel("Basic", "29.99", "25", "10", R.drawable.ic_speed, Color.parseColor("#CCFBF1")));
        planList.add(new PlanModel("Standard", "59.99", "100", "50", R.drawable.ic_wifi_tethering, Color.parseColor("#E0E7FF")));
        planList.add(new PlanModel("Pro", "99.99", "500", "100", R.drawable.ic_rocket, Color.parseColor("#FFEDD5")));

        // 4. Initialize Adapter with Integrated Edit and Delete Logic
        adapter = new PlanAdapter(planList, new PlanAdapter.OnPlanClickListener() {
            @Override
            public void onEditClick(PlanModel plan) {
                // Fill the form with the selected plan's data
                etName.setText(plan.getName());
                etPrice.setText(plan.getPrice());
                etDownload.setText(plan.getSpeed());
                etUpload.setText(plan.getUpload());

                // Focus and allow user to start editing immediately
                etName.requestFocus();
            }

            @Override
            public void onDeleteClick(PlanModel plan, int position) {
                // Remove the plan from the data list
                planList.remove(position);

                // Animate removal from the RecyclerView
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, planList.size());

                Toast.makeText(getContext(), "Deleted " + plan.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Bind Adapter to RecyclerView
        rvPlans.setAdapter(adapter);

        // 6. Form Button Actions
        btnSaveChanges.setOnClickListener(v -> {
            // Logic for saving changes can be added here
            Toast.makeText(getContext(), "Plan updated successfully!", Toast.LENGTH_SHORT).show();
            clearFields();
        });

        btnCancel.setOnClickListener(v -> clearFields());

        return view;
    }

    private void clearFields() {
        etName.setText("");
        etPrice.setText("");
        etDownload.setText("");
        etUpload.setText("");
        etName.clearFocus();
    }
}