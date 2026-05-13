package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog; // Added
import com.google.android.material.button.MaterialButton; // Added
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ShapeableImageView ivProfile;
    private TextView tvSolusyonLogo;
    private DatabaseReference mDatabase;
    private boolean isSubscriber;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isSubscriber = getIntent().getBooleanExtra("IS_SUBSCRIBER", false);

        if (!isSubscriber) {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String role = prefs.getString("userRole", "");
            isSubscriber = "subscriber".equalsIgnoreCase(role);
        }

        setupLayout(isSubscriber);
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();
        syncWelcomeHeader();

        boolean shouldShowLogin = getIntent().getBooleanExtra("SHOW_LOGIN", false);
        boolean isAutoLogin = getIntent().getBooleanExtra("IS_AUTO_LOGIN", false);

        if (savedInstanceState == null) {
            if (shouldShowLogin) {
                loadFragment(new LoginFragment(), false);
                toggleSystemUI(false);
            } else if (isSubscriber) {
                performSessionRoleCheck(); // Trigger the database check instead of loading a fragment
            } else if (isAutoLogin || FirebaseAuth.getInstance().getCurrentUser() != null) {
                performSessionRoleCheck();
            } else {
                loadFragment(new DashboardFragment(), false);
                toggleSystemUI(true);
            }
        }
    }

    public void syncWelcomeHeader() {
        tvSolusyonLogo = findViewById(R.id.tvSolusyonLogo);
        if (tvSolusyonLogo != null) {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String fullName = prefs.getString("userName", "");
            if (!fullName.isEmpty()) {
                String firstName = fullName.split(" ")[0];
                tvSolusyonLogo.setText("Welcome, " + firstName + "!");
            } else {
                tvSolusyonLogo.setText("SOLUSYON");
            }
        }
    }

    private void performSessionRoleCheck() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    String name = snapshot.child("fullName").getValue(String.class);
                    if (name != null) {
                        getSharedPreferences("UserSession", MODE_PRIVATE)
                                .edit()
                                .putString("userName", name)
                                .putString("userRole", role)
                                .apply();
                        syncWelcomeHeader();
                    }

                    if ("subscriber".equalsIgnoreCase(role)) {
                        if (!isSubscriber) {
                            restartForSubscriber();
                        } else {
                            checkSubscriberStatus(uid);
                        }
                    } else {
                        loadFragment(new DashboardFragment(), false);
                        toggleSystemUI(true);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void restartForSubscriber() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("IS_SUBSCRIBER", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void checkSubscriberStatus(String uid) {
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("status").getValue(String.class);
                if ("completed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
                    loadFragment(new UserBillingFragment(), false);
                } else if (snapshot.exists()) {
                    navigateToReceipt(snapshot);
                } else {
                    // Load the FORM (user_dashboard.xml) for new users
                    loadFragment(new UserDashboardFragment(), false);
                }
                toggleSystemUI(true);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void navigateToReceipt(DataSnapshot snapshot) {
        Intent intent = new Intent(MainActivity.this, UserApplicationReceiptActivity.class);
        intent.putExtra("appId", snapshot.child("applicationId").getValue(String.class));
        intent.putExtra("fullName", snapshot.child("fullName").getValue(String.class));
        intent.putExtra("phone", snapshot.child("phone").getValue(String.class));
        intent.putExtra("plan", snapshot.child("plan").getValue(String.class));
        intent.putExtra("payment", snapshot.child("payment").getValue(String.class));
        intent.putExtra("date", snapshot.child("date").getValue(String.class));
        startActivity(intent);
        finish();
    }

    public void setupLayout(boolean subscriberMode) {
        this.isSubscriber = subscriberMode;
        if (isSubscriber) {
            setContentView(R.layout.activity_main_user);
        } else {
            setContentView(R.layout.activity_main);
        }

        bottomNavigationView = findViewById(R.id.bottomNavigation);
// Inside setupLayout(boolean subscriberMode)
        ivProfile = findViewById(R.id.ivProfile);

        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                if (isSubscriber) {
                    // Load the Profile Dashboard for subscribers
                    loadFragment(new SubscriberProfileFragment(), true);
                    // Optionally, highlight the profile tab in bottom nav
                    bottomNavigationView.setSelectedItemId(R.id.nav_sub_profile);
                } else {
                    // Keep logout for admins
                    showLogoutDialog();
                }
            });
        }

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard || itemId == R.id.nav_sub_dashboard) {
                    selectedFragment = isSubscriber ? new ClientDashboardFragment() : new DashboardFragment();
                } else if (itemId == R.id.nav_subscribers) {
                    selectedFragment = new SubscriberManagement();
                } else if (itemId == R.id.nav_billing || itemId == R.id.nav_sub_billing) {
                    selectedFragment = isSubscriber ? new UserBillingFragment() : new BillingFragment();
                } else if (itemId == R.id.nav_reports) {
                    selectedFragment = new ReportsFragment();
                } else if (itemId == R.id.nav_sub_profile) {
                    selectedFragment = new SubscriberProfileFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, true);
                }
                return true;
            });
        }
    }

    /**
     * UPDATED: Now displays the modern BottomSheet logout
     */
    private void showLogoutDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_logout_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);

        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        TextView tvCancel = view.findViewById(R.id.tvCancel);

        tvCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            bottomSheetDialog.dismiss();
            finish();
        });

        bottomSheetDialog.show();
    }

    public void toggleSystemUI(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(visibility);
        View headerLayout = findViewById(R.id.headerLayout);
        if (headerLayout != null) {
            headerLayout.setVisibility(visibility);
        }
        if (ivProfile != null) ivProfile.setVisibility(visibility);
    }

    private void loadFragment(Fragment fragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate) {
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}