package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment {

    private TextInputEditText etUsername, etPassword;
    private CheckBox cbTrustDevice;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private View viewSuccess, viewError;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggleSystemUI(false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        cbTrustDevice = view.findViewById(R.id.cbTrustDevice);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        viewSuccess = view.findViewById(R.id.viewSuccess);
        viewError = view.findViewById(R.id.viewError);

        SharedPreferences prefs = requireActivity().getSharedPreferences("LoginPrefs", 0);
        if (prefs.getBoolean("rememberMe", false)) {
            etUsername.setText(prefs.getString("email", ""));
            etPassword.setText(prefs.getString("password", ""));
            cbTrustDevice.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> handleLogin());
        tvForgotPassword.setOnClickListener(v -> navigateToFragment(new ForgotPasswordFragment(), false));
    }

    private void handleLogin() {
        String input = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        boolean isTrusted = cbTrustDevice.isChecked();

        viewSuccess.setVisibility(View.GONE);
        viewError.setVisibility(View.GONE);

        if (input.isEmpty() || password.isEmpty()) {
            viewError.setVisibility(View.VISIBLE);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verifying...");

        if (input.contains("@")) {
            performFirebaseLogin(input, password, isTrusted);
        } else {
            lookupEmailByUsername(input, password, isTrusted);
        }
    }

    private void lookupEmailByUsername(String username, String password, boolean isTrusted) {
        mDatabase.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String email = "";
                            for (DataSnapshot child : snapshot.getChildren()) {
                                email = child.child("email").getValue(String.class);
                            }
                            if (email != null) performFirebaseLogin(email, password, isTrusted);
                        } else {
                            resetLoginButton();
                            Toast.makeText(getContext(), "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { resetLoginButton(); }
                });
    }

    private void performFirebaseLogin(String email, String password, boolean isTrusted) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveLoginPrefs(email, password, isTrusted);
                            checkUserRole(user.getUid());
                        }
                    } else {
                        resetLoginButton();
                        viewError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void checkUserRole(String uid) {
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null) role = role.toLowerCase().trim();

                    String name = snapshot.child("fullName").getValue(String.class);
                    if (name == null) name = snapshot.child("username").getValue(String.class);

                    SharedPreferences userSession = requireActivity().getSharedPreferences("UserSession", 0);
                    userSession.edit().putString("userName", name).putString("userRole", role).apply();

                    if ("subscriber".equals(role)) {
                        // FIX: Force switch to Subscriber Layout
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setupLayout(true);
                        }
                        checkApplicationStatus(uid);
                    } else {
                        // FIX: Force switch to Admin Layout
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setupLayout(false);
                        }
                        navigateToFragment(new DashboardFragment(), true);
                    }
                } else {
                    resetLoginButton();
                    Toast.makeText(getContext(), "User profile not found.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { resetLoginButton(); }
        });
    }

    // Update this method in LoginFragment.java

    private void checkApplicationStatus(String uid) {
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).applicationStatus = status;
                    }

                    if ("completed".equalsIgnoreCase(status)) {
                        navigateToFragment(new ClientDashboardFragment(), true); // true = SHOW UI
                    } else if ("approved".equalsIgnoreCase(status)) {
                        navigateToFragment(new UserBillingFragment(), true); // true = SHOW UI
                        updateBottomNav(R.id.nav_sub_billing);
                    } else {
                        // Handle pending receipt...
                        viewSuccess.setVisibility(View.VISIBLE);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isAdded()) {
                                Intent intent = new Intent(getActivity(), UserApplicationReceiptActivity.class);
                                intent.putExtra("appId", snapshot.child("applicationId").getValue(String.class));
                                intent.putExtra("fullName", snapshot.child("fullName").getValue(String.class));
                                intent.putExtra("phone", snapshot.child("phone").getValue(String.class));
                                intent.putExtra("plan", snapshot.child("plan").getValue(String.class));
                                intent.putExtra("payment", snapshot.child("payment").getValue(String.class));
                                intent.putExtra("date", snapshot.child("date").getValue(String.class));
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }, 800);
                    }
                } else {
                    // No Application -> Go to Form
                    navigateToFragment(new UserDashboardFragment(), false);
                    updateBottomNav(R.id.nav_sub_dashboard);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { resetLoginButton(); }
        });
    }

    private void updateBottomNav(int itemId) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() instanceof MainActivity) {
                BottomNavigationView bnv = getActivity().findViewById(R.id.bottomNavigation);
                if (bnv != null) bnv.setSelectedItemId(itemId);
            }
        }, 850);
    }

    private void navigateToFragment(Fragment fragment, boolean showUI) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                toggleSystemUI(showUI);
                if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).syncWelcomeHeader();
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }, 800);
    }

    private void toggleSystemUI(boolean show) {
        if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).toggleSystemUI(show);
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Sign in to Dashboard");
    }

    private void saveLoginPrefs(String email, String password, boolean remember) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("LoginPrefs", 0).edit();
        if (remember) {
            editor.putString("email", email).putString("password", password).putBoolean("rememberMe", true);
        } else {
            editor.clear();
        }
        editor.apply();
    }
}