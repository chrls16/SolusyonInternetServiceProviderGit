package com.example.solusyoninternetserviceprovider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubscriberProfileFragment extends Fragment {

    private TextView tvName, tvSubscriberId, tvAccountStatus, tvLocationCity;
    private TextView tvPlanTag, tvPackageTier, tvSpeed, tvBilling;
    private TextView tvEmail, tvPhone, tvAddress;
    private View vStatusDot;
    private ImageView ivProfilePicture; // Added reference

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    // 1. Create the Image Picker Launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        ivProfilePicture.setImageURI(imageUri);
                        Toast.makeText(getContext(), "Profile picture updated locally!", Toast.LENGTH_SHORT).show();
                        // Note: To save this permanently, you'll need Firebase Storage
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_subscriber_profile, container, false);

        initViews(view);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

        fetchSubscriberData();

        // 2. Set Click Listener to change photo
        ivProfilePicture.setOnClickListener(v -> openGallery());

        return view;
    }

    private void initViews(View v) {
        tvName = v.findViewById(R.id.tvName);
        tvSubscriberId = v.findViewById(R.id.tvSubscriberId);
        tvAccountStatus = v.findViewById(R.id.tvAccountStatus);
        tvLocationCity = v.findViewById(R.id.tvLocationCity);
        tvPlanTag = v.findViewById(R.id.tvPlanTag);
        tvPackageTier = v.findViewById(R.id.tvPackageTier);
        tvSpeed = v.findViewById(R.id.tvSpeed);
        tvBilling = v.findViewById(R.id.tvBilling);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvPhone = v.findViewById(R.id.tvPhone);
        tvAddress = v.findViewById(R.id.tvAddress);
        vStatusDot = v.findViewById(R.id.vStatusDot);
        ivProfilePicture = v.findViewById(R.id.ivProfilePicture); // Make sure this ID exists in XML
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void fetchSubscriberData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        // A. Fetch from 'users' node for basic account info
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    if (name != null) tvName.setText(name);
                    if (email != null) tvEmail.setText(email);
                    if (phone != null) tvPhone.setText(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // B. Fetch from 'ServiceApplications' node for technical/plan details
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String appId = snapshot.child("applicationId").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String plan = snapshot.child("plan").getValue(String.class);
                    String barangay = snapshot.child("barangay").getValue(String.class);
                    String purok = snapshot.child("purok").getValue(String.class);

                    if (appId != null) tvSubscriberId.setText("SUBSCRIBER ID: " + appId);
                    if (status != null) {
                        tvAccountStatus.setText(status.toUpperCase() + " ACCOUNT");
                        updateStatusUI(status);
                    }

                    if (barangay != null) {
                        tvLocationCity.setText(barangay.toUpperCase() + ", PARACALE");
                        String fullAddr = (purok != null ? "Purok " + purok + ", " : "") + barangay + ", Paracale";
                        tvAddress.setText(fullAddr);
                    }

                    if (plan != null) {
                        updatePlanDetails(plan);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStatusUI(String status) {
        // Change dot color and badge style based on account status
        if ("approved".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
            vStatusDot.setBackgroundResource(R.drawable.dot_green);
            tvAccountStatus.setBackgroundResource(R.drawable.bg_status_approved);
        } else {
            vStatusDot.setBackgroundResource(R.drawable.dot_inactive);
            tvAccountStatus.setBackgroundResource(R.drawable.bg_tab_active);
        }
    }

    private void updatePlanDetails(String planName) {
        // Map the plan names to specific speeds and prices
        tvPlanTag.setText(planName.toUpperCase() + " FIBER");

        if (planName.equalsIgnoreCase("Basic")) {
            tvPackageTier.setText("Basic Home 25");
            tvSpeed.setText("25");
            tvBilling.setText("₱ 499.00");
        } else if (planName.equalsIgnoreCase("Standard")) {
            tvPackageTier.setText("Standard Plus 50");
            tvSpeed.setText("50");
            tvBilling.setText("₱ 699.00");
        } else if (planName.equalsIgnoreCase("Pro")) {
            tvPackageTier.setText("Enterprise Pro 100");
            tvSpeed.setText("100");
            tvBilling.setText("₱ 999.00");
        }
    }
}