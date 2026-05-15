package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlanManagementFragment extends Fragment {

    private RecyclerView rvPlans;
    private PlanAdapter adapter;
    private List<PlanModel> planList = new ArrayList<>();
    private DatabaseReference planRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_management, container, false);

        // 1. Initialize Firebase Reference
        planRef = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Plans");

        // 2. Setup RecyclerView
        rvPlans = view.findViewById(R.id.rvPlans);
        rvPlans.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PlanAdapter(planList, new PlanAdapter.OnPlanClickListener() {
            @Override
            public void onEditClick(PlanModel plan) {
                showPlanPanel(plan);
            }

            @Override
            public void onDeleteClick(PlanModel plan, int position) {
                if (plan.getPlanId() != null) {
                    planRef.child(plan.getPlanId()).removeValue();
                    Toast.makeText(getContext(), "Plan deleted from cloud", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rvPlans.setAdapter(adapter);

        // 3. FAB for adding new plans
        FloatingActionButton fabAddPlan = view.findViewById(R.id.fabAddPlan);
        if (fabAddPlan != null) {
            fabAddPlan.setOnClickListener(v -> showPlanPanel(null));
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchPlansFromFirebase();
    }

    private void fetchPlansFromFirebase() {
        planRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                planList.clear();
                if (!snapshot.exists()) {
                    // SEED DATABASE: If no plans exist in Firebase, add the original 3
                    seedDefaultPlans();
                } else {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        PlanModel plan = ds.getValue(PlanModel.class);
                        if (plan != null) {
                            plan.setPlanId(ds.getKey());
                            planList.add(plan);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void seedDefaultPlans() {
        // Save the 3 original plans to Firebase so they are never lost
        planRef.push().setValue(new PlanModel("Basic", "499.00", "25", "25", R.drawable.ic_speed, Color.parseColor("#CCFBF1")));
        planRef.push().setValue(new PlanModel("Standard", "699.00", "50", "50", R.drawable.ic_wifi_tethering, Color.parseColor("#E0E7FF")));
        planRef.push().setValue(new PlanModel("Pro", "999.00", "100", "100", R.drawable.ic_rocket, Color.parseColor("#FFEDD5")));
    }

    private void showPlanPanel(PlanModel plan) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.layout_edit_plan_bottom_sheet, null);
        dialog.setContentView(view);

        ImageView ivIcon = view.findViewById(R.id.ivPanelIcon);
        TextView tvTitle = view.findViewById(R.id.tvPanelTitle);
        EditText etName = view.findViewById(R.id.etEditPlanName);
        EditText etPrice = view.findViewById(R.id.etEditPrice);
        EditText etDownload = view.findViewById(R.id.etEditDownload);
        EditText etUpload = view.findViewById(R.id.etEditUpload);
        MaterialButton btnSave = view.findViewById(R.id.btnEditSave);

        if (plan != null) {
            tvTitle.setText("Edit Plan Details");
            ivIcon.setImageResource(R.drawable.ic_edit);
            etName.setText(plan.getName());
            etPrice.setText(plan.getPrice());
            etDownload.setText(plan.getSpeed());
            etUpload.setText(plan.getUpload());
        } else {
            tvTitle.setText("Add Plan Details");
            ivIcon.setImageResource(R.drawable.ic_add);
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String price = etPrice.getText().toString().trim();
            String download = etDownload.getText().toString().trim();
            String upload = etUpload.getText().toString().trim();

            if (name.isEmpty() || price.isEmpty()) return;

            if (plan != null) {
                // Update existing record
                plan.setName(name);
                plan.setPrice(price);
                plan.setSpeed(download);
                plan.setUpload(upload);
                planRef.child(plan.getPlanId()).setValue(plan);
            } else {
                // Add new record to the "Plans" node
                PlanModel newPlan = new PlanModel(name, price, download, upload, R.drawable.ic_router, Color.parseColor("#E0E7FF"));
                planRef.push().setValue(newPlan);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}