package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserTermsFragment extends Fragment {

    private CheckBox cbAgree;
    private Button btnBack, btnAgreeSubmit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_terms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // FIX: Keep Header and Nav Bar hidden on Terms screen
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSystemUI(false);
        }

        cbAgree = view.findViewById(R.id.cbAgree);
        btnBack = view.findViewById(R.id.btnBack);
        btnAgreeSubmit = view.findViewById(R.id.btnAgreeSubmit);

        btnAgreeSubmit.setEnabled(false);
        btnAgreeSubmit.setAlpha(0.5f);

        cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAgreeSubmit.setEnabled(isChecked);
            btnAgreeSubmit.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnAgreeSubmit.setOnClickListener(v -> performFinalSubmission());
    }

    private void performFinalSubmission() {
        Bundle bundle = getArguments();
        if (bundle == null) return;

        btnAgreeSubmit.setEnabled(false);
        btnAgreeSubmit.setText("Submitting...");

        String appId = "SOL-2026-" + (int)(Math.random() * 900000 + 100000);
        String dateIssued = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

        Map<String, Object> appData = new HashMap<>();
        appData.put("applicationId", appId);
        appData.put("date", dateIssued);
        appData.put("fullName", bundle.getString("fullName"));
        appData.put("phone", bundle.getString("phone"));
        appData.put("barangay", bundle.getString("barangay"));
        appData.put("plan", bundle.getString("plan"));
        appData.put("payment", bundle.getString("payment"));
        appData.put("status", "pending");

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("ServiceApplications")
                    .child(uid)
                    .setValue(appData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getContext(), UserApplicationReceiptActivity.class);
                            intent.putExtras(bundle);
                            intent.putExtra("appId", appId);
                            intent.putExtra("date", dateIssued);
                            startActivity(intent);
                            if (getActivity() != null) getActivity().finish();
                        } else {
                            btnAgreeSubmit.setEnabled(true);
                            btnAgreeSubmit.setText("AGREE AND SUBMIT");
                        }
                    });
        }
    }
}