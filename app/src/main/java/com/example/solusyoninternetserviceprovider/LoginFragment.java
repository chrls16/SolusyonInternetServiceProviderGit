package com.example.solusyoninternetserviceprovider;

import android.content.Intent; // <--- ADD THIS LINE
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

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private CheckBox cbTrustDevice;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private View viewSuccess;
    private View viewError;

    // Firebase Auth & Database
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide the Header and Bottom Navigation Bar on the Login Screen
        toggleSystemUI(false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Initialize views
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        cbTrustDevice = view.findViewById(R.id.cbTrustDevice);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        viewSuccess = view.findViewById(R.id.viewSuccess);
        viewError = view.findViewById(R.id.viewError);

        // REMEMBER ME: Load saved credentials
        SharedPreferences prefs = requireActivity().getSharedPreferences("LoginPrefs", 0);
        if (prefs.getBoolean("rememberMe", false)) {
            etUsername.setText(prefs.getString("email", ""));
            etPassword.setText(prefs.getString("password", ""));
            cbTrustDevice.setChecked(true);
        }

        // Set click listeners
        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Password reset feature coming soon", Toast.LENGTH_SHORT).show();
        });
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

        // Check if input is an Email or a Username
        if (input.contains("@")) {
            // It's an Email - Login directly
            performFirebaseLogin(input, password, isTrusted);
        } else {
            // It's a Username - Look up the email in the database first
            lookupEmailByUsername(input, password, isTrusted);
        }
    }

    private void lookupEmailByUsername(String username, String password, boolean isTrusted) {
        // Search the "users" node for a child where "username" matches the input
        mDatabase.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            // Username found! Get the email associated with it
                            String email = "";
                            for (DataSnapshot child : snapshot.getChildren()) {
                                email = child.child("email").getValue(String.class);
                            }

                            if (email != null && !email.isEmpty()) {
                                performFirebaseLogin(email, password, isTrusted);
                            }
                        } else {
                            // Username doesn't exist
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Sign in to Dashboard");
                            viewError.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnLogin.setEnabled(true);
                        Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();
                    }
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
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign in to Dashboard");
                        viewError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void checkUserRole(String uid) {
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    if (name == null || name.isEmpty()) {
                        name = snapshot.child("username").getValue(String.class);
                    }

                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null) role = role.toLowerCase().trim();

                    SharedPreferences userSession = requireActivity().getSharedPreferences("UserSession", 0);
                    SharedPreferences.Editor editor = userSession.edit();
                    editor.putString("userName", name);
                    editor.putString("userRole", role);
                    editor.apply();

                    if ("subscriber".equals(role)) {
                        // Switch MainActivity to Subscriber layout before loading subscriber fragments
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setupLayout(true);
                        }
                        checkApplicationStatus(uid);
                    } else if ("admin".equals(role)) {
                        navigateToFragment(new DashboardFragment(), true);
                    } else {
                        navigateToFragment(new DashboardFragment(), true);
                    }
                } else {
                    Toast.makeText(getContext(), "User profile not found.", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnLogin.setEnabled(true);
            }
        });
    }

    private void checkApplicationStatus(String uid) {
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);

                    // 1. IF STATUS IS COMPLETED OR APPROVED: Show the Billing/Dashboard
                    if ("completed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
                        // This takes the user to their active account view
                        navigateToFragment(new UserBillingFragment(), true);
                    }
                    // 2. IF STATUS IS STILL PENDING: Show the Digital Receipt
                    else {
                        viewSuccess.setVisibility(View.VISIBLE);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isAdded()) {
                                Intent intent = new Intent(getActivity(), UserApplicationReceiptActivity.class);
                                // Pass database values to the intent
                                intent.putExtra("appId", snapshot.child("applicationId").getValue(String.class));
                                intent.putExtra("date", snapshot.child("date").getValue(String.class));
                                intent.putExtra("fullName", snapshot.child("fullName").getValue(String.class));
                                intent.putExtra("phone", snapshot.child("phone").getValue(String.class));
                                intent.putExtra("plan", snapshot.child("plan").getValue(String.class));
                                intent.putExtra("payment", snapshot.child("payment").getValue(String.class));

                                startActivity(intent);
                                if (getActivity() != null) getActivity().finish();
                            }
                        }, 800);
                    }
                } else {
                    // NO APPLICATION FOUND: Take them to the application form
                    navigateToFragment(new UserDashboardFragment(), true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnLogin.setEnabled(true);
            }
        });
    }

    private void navigateToFragment(Fragment fragment, boolean showAdminUI) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                toggleSystemUI(showAdminUI);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).syncWelcomeHeader();
                }
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment_container, fragment);
                transaction.commit();
            }
        }, 800);
    }

    private void toggleSystemUI(boolean show) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSystemUI(show);
        }
    }

    private void saveLoginPrefs(String email, String password, boolean remember) {
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("LoginPrefs", 0);
        SharedPreferences.Editor editor = loginPrefs.edit();
        if (remember) {
            editor.putString("email", email);
            editor.putString("password", password);
            editor.putBoolean("rememberMe", true);
        } else {
            editor.clear();
        }
        editor.apply();
    }
}