package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDashboardFragment extends Fragment {

    private EditText etLastName, etFirstName, etPhone;
    private Spinner spBarangay;
    private Button btnProceed;
    private MaterialCardView planBasic, planStandard, planPro;
    private MaterialCardView payGcash, payMaya, payBank, payCash;

    private String selectedPlan = "Standard";
    private String selectedPayment = "Cash on Install";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // FIX: Ensure Header and Nav Bar are hidden during onboarding
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSystemUI(false);
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        initializeViews(view);
        setupListeners();
        fetchUserData();
    }

    private void initializeViews(View view) {
        etLastName = view.findViewById(R.id.etLastName);
        etFirstName = view.findViewById(R.id.etFirstName);
        etPhone = view.findViewById(R.id.etPhone);
        spBarangay = view.findViewById(R.id.spBarangay);
        btnProceed = view.findViewById(R.id.btnProceed);
        planBasic = view.findViewById(R.id.planBasic);
        planStandard = view.findViewById(R.id.planStandard);
        planPro = view.findViewById(R.id.planPro);
        payGcash = view.findViewById(R.id.payGcash);
        payMaya = view.findViewById(R.id.payMaya);
        payBank = view.findViewById(R.id.payBank);
        payCash = view.findViewById(R.id.payCash);
    }

    private void setupListeners() {
        setupSpinner();
        planBasic.setOnClickListener(v -> selectPlan("Basic", planBasic));
        planStandard.setOnClickListener(v -> selectPlan("Standard", planStandard));
        planPro.setOnClickListener(v -> selectPlan("Pro", planPro));

        payGcash.setOnClickListener(v -> selectPayment("Gcash", payGcash));
        payMaya.setOnClickListener(v -> selectPayment("Maya", payMaya));
        payBank.setOnClickListener(v -> selectPayment("Bank Transfer", payBank));
        payCash.setOnClickListener(v -> selectPayment("Cash on Install", payCash));

        btnProceed.setOnClickListener(v -> validateAndProceed());
    }

    private void selectPlan(String planName, MaterialCardView selectedCard) {
        selectedPlan = planName;
        planBasic.setStrokeWidth(0);
        planStandard.setStrokeWidth(0);
        planPro.setStrokeWidth(0);
        selectedCard.setStrokeWidth(dpToPx(2));
        selectedCard.setStrokeColor(Color.parseColor("#2D62B5"));
    }

    private void selectPayment(String paymentName, MaterialCardView selectedCard) {
        selectedPayment = paymentName;
        payGcash.setStrokeWidth(0);
        payMaya.setStrokeWidth(0);
        payBank.setStrokeWidth(0);
        payCash.setStrokeWidth(0);
        selectedCard.setStrokeWidth(dpToPx(2));
        selectedCard.setStrokeColor(Color.parseColor("#2D62B5"));
    }

    private void fetchUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        String rawPhone = snapshot.child("phone").getValue(String.class);
                        if (rawPhone != null) etPhone.setText(rawPhone.startsWith("63") ? "0" + rawPhone.substring(2) : rawPhone);
                        if (fullName != null) {
                            String[] parts = fullName.split(" ");
                            etFirstName.setText(parts[0]);
                            if (parts.length > 1) etLastName.setText(parts[parts.length - 1]);
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupSpinner() {
        String[] barangays = {"Select Barangay", "Bagumbayan", "Palanas", "Poblacion Norte", "Poblacion Sur", "Tugos"};
        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, barangays);
            spBarangay.setAdapter(adapter);
        }
    }

    private void validateAndProceed() {
        String phone = etPhone.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String barangay = spBarangay.getSelectedItem().toString();

        if (phone.length() != 11 || !phone.startsWith("09")) {
            etPhone.setError("Must be exactly 11 digits starting with 09");
            return;
        }
        if (spBarangay.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select a Barangay", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("fullName", firstName + " " + lastName);
        bundle.putString("phone", phone);
        bundle.putString("barangay", barangay);
        bundle.putString("plan", selectedPlan);
        bundle.putString("payment", selectedPayment);

        UserTermsFragment termsFragment = new UserTermsFragment();
        termsFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, termsFragment)
                .addToBackStack(null)
                .commit();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
