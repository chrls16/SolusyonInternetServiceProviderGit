package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView; // Added import
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private TextView tvSolusyonLogo; // Added reference for the header text
    private DatabaseReference mDatabase;
    private boolean isSubscriber;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for navigation flags to determine which layout to use
        isSubscriber = getIntent().getBooleanExtra("IS_SUBSCRIBER", false);
        
        // Also check saved session if intent flag is not set
        if (!isSubscriber) {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String role = prefs.getString("userRole", "");
            isSubscriber = "subscriber".equalsIgnoreCase(role);
        }

        setupLayout(isSubscriber);

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

        // 2. Initial Header Sync
        syncWelcomeHeader();

        // 3. Check for Navigation Flags
        boolean shouldShowLogin = getIntent().getBooleanExtra("SHOW_LOGIN", false);
        boolean isAutoLogin = getIntent().getBooleanExtra("IS_AUTO_LOGIN", false);

        if (savedInstanceState == null) {
            if (shouldShowLogin) {
                loadFragment(new LoginFragment(), false);
                toggleSystemUI(false);
            } else if (isSubscriber) {
                loadFragment(new UserDashboardFragment(), false);
                toggleSystemUI(true);
            } else if (isAutoLogin || FirebaseAuth.getInstance().getCurrentUser() != null) {
                performSessionRoleCheck();
            } else {
                loadFragment(new DashboardFragment(), false);
                toggleSystemUI(true);
            }
        }
    }

    /**
     * Updates the header text based on the saved user session.
     * Splitting the name ensures we only show the first name for a cleaner UI.
     */
    public void syncWelcomeHeader() {
        // Look for the ID only when this method is called
        tvSolusyonLogo = findViewById(R.id.tvSolusyonLogo);

        // If tvSolusyonLogo is null, it means we are in the Admin layout.
        // The code inside this 'if' will simply be skipped.
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

                    // Update header name even for auto-login sessions
                    String name = snapshot.child("fullName").getValue(String.class);
                    if (name != null) {
                        getSharedPreferences("UserSession", MODE_PRIVATE)
                                .edit().putString("userName", name).apply();
                        syncWelcomeHeader();
                    }

                    if ("subscriber".equalsIgnoreCase(role)) {
                        if (!isSubscriber) {
                            // Restart with subscriber flag to use R.layout.activity_main_user
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.putExtra("IS_SUBSCRIBER", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
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

    private void checkSubscriberStatus(String uid) {
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("status").getValue(String.class);

                if ("completed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
                    loadFragment(new UserBillingFragment(), false);
                } else if (snapshot.exists()) {
                    Intent intent = new Intent(MainActivity.this, UserApplicationReceiptActivity.class);
                    intent.putExtra("appId", snapshot.child("applicationId").getValue(String.class));
                    intent.putExtra("fullName", snapshot.child("fullName").getValue(String.class));
                    intent.putExtra("phone", snapshot.child("phone").getValue(String.class));
                    intent.putExtra("plan", snapshot.child("plan").getValue(String.class));
                    intent.putExtra("payment", snapshot.child("payment").getValue(String.class));
                    intent.putExtra("date", snapshot.child("date").getValue(String.class));
                    startActivity(intent);
                    finish();
                } else {
                    loadFragment(new UserDashboardFragment(), false);
                }
                toggleSystemUI(true);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Account")
                .setMessage("Would you like to log out of your session?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear user session data on logout
                    getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();

                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void setupLayout(boolean subscriberMode) {
        this.isSubscriber = subscriberMode;
        if (isSubscriber) {
            setContentView(R.layout.activity_main_user);
        } else {
            setContentView(R.layout.activity_main);
        }

        // Initialize Views
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        ivProfile = findViewById(R.id.ivProfile);
        
        // Re-attach listeners
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> showLogoutDialog());
        }

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard || itemId == R.id.nav_sub_dashboard) {
                    if (isSubscriber) {
                        selectedFragment = new UserDashboardFragment();
                    } else {
                        selectedFragment = new DashboardFragment();
                    }
                } else if (itemId == R.id.nav_subscribers) {
                    selectedFragment = new SubscriberManagement();
                } else if (itemId == R.id.nav_billing || itemId == R.id.nav_sub_billing) {
                    if (isSubscriber) {
                        selectedFragment = new UserBillingFragment();
                    } else {
                        selectedFragment = new BillingFragment();
                    }
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

    public void toggleSystemUI(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(visibility);
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