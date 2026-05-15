package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
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
    public String applicationStatus = ""; // Public so LoginFragment can update it
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. FCM Subscription with Logcat Tracking
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    android.util.Log.d("FCM", "Current Device Token: " + token);
                });

        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("FCM", "Subscribed to announcements successfully!");
                    } else {
                        android.util.Log.e("FCM", "Subscription failed: " + task.getException());
                    }
                });

        // 2. Initialize Database Reference
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

        // 3. Request Notification Permissions (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 4. Determine User Role (Subscriber vs Admin)
        isSubscriber = getIntent().getBooleanExtra("IS_SUBSCRIBER", false);
        if (!isSubscriber) {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String role = prefs.getString("userRole", "");
            isSubscriber = "subscriber".equalsIgnoreCase(role);
        }

        // 5. UI and Sync Setup
        setupLayout(isSubscriber);
        syncWelcomeHeader();
        syncProfilePicture();

        // 6. Handle Incoming Deep Links (e.g., Password Reset Email)
        handleDeepLink(getIntent());

        // 7. Decide which Fragment to load first
        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("SHOW_LOGIN", false)) {
                loadFragment(new LoginFragment(), false);
                toggleSystemUI(false);
            } else {
                // Check session status or redirect to default dashboard
                performSessionRoleCheck();
            }
        }
    }

    private void handleDeepLink(Intent intent) {
        if (intent != null && intent.getData() != null) {
            android.net.Uri data = intent.getData();
            if (data.getQueryParameter("oobCode") != null) {
                String oobCode = data.getQueryParameter("oobCode");

                // Navigate to SetNewPasswordFragment and pass the code
                SetNewPasswordFragment fragment = new SetNewPasswordFragment();
                Bundle bundle = new Bundle();
                bundle.putString("oobCode", oobCode);
                fragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                toggleSystemUI(false); // Hide bars for reset screen
            }
        }
    }
    private void syncProfilePicture() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        mDatabase.child("users").child(uid).child("profilePictureUrl")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String base64Image = snapshot.getValue(String.class);
                        if (base64Image != null && !base64Image.isEmpty() && ivProfile != null) {
                            try {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivProfile.setImageBitmap(decodedByte);
                            } catch (Exception e) {
                                ivProfile.setImageResource(R.drawable.logo);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
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
                        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                                .putString("userName", name).putString("userRole", role).apply();
                        syncWelcomeHeader();
                    }
                    if ("subscriber".equalsIgnoreCase(role)) {
                        if (!isSubscriber) restartForSubscriber();
                        else checkSubscriberStatus(uid);
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
        if (uid == null) return;
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    applicationStatus = snapshot.child("status").getValue(String.class);
                    if (applicationStatus == null) applicationStatus = "pending";

                    if ("completed".equalsIgnoreCase(applicationStatus)) {
                        loadFragment(new ClientDashboardFragment(), false);
                        toggleSystemUI(true); // SHOW UI
                    } else if ("approved".equalsIgnoreCase(applicationStatus)) {
                        loadFragment(new UserBillingFragment(), false);
                        toggleSystemUI(true); // SHOW UI
                    } else {
                        navigateToReceipt(snapshot); // Activity handles its own UI
                    }
                } else {
                    applicationStatus = "";
                    loadFragment(new UserDashboardFragment(), false);
                    toggleSystemUI(false); // HIDE UI for Step 2 Form
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
        setContentView(isSubscriber ? R.layout.activity_main_user : R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        ivProfile = findViewById(R.id.ivProfile);

        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                if (isSubscriber) {
                    loadFragment(new SubscriberProfileFragment(), true);
                    bottomNavigationView.setSelectedItemId(R.id.nav_sub_profile);
                } else showLogoutDialog();
            });
        }

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard || itemId == R.id.nav_sub_dashboard) {
                    if (isSubscriber) {
                        if ("completed".equalsIgnoreCase(applicationStatus)) {
                            selectedFragment = new ClientDashboardFragment();
                            toggleSystemUI(true);
                        } else {
                            selectedFragment = new UserDashboardFragment();
                            toggleSystemUI(false); // HIDE UI if they click dashboard but are back at the form
                        }
                    } else {
                        selectedFragment = new DashboardFragment();
                        toggleSystemUI(true);
                    }
                    selectedFragment = isSubscriber ? new ClientDashboardFragment() : new DashboardFragment();
                } else if (itemId == R.id.nav_subscribers) {
                    selectedFragment = new SubscriberManagement();
                } else if (itemId == R.id.nav_billing || itemId == R.id.nav_sub_billing) {
                    selectedFragment = isSubscriber ? new UserBillingFragment() : new BillingFragment();
                } else if (itemId == R.id.nav_plans) {
                    selectedFragment = new PlanManagementFragment();
                } else if (itemId == R.id.nav_reports) {
                    selectedFragment = new ReportsFragment();
                } else if (itemId == R.id.nav_sub_profile) {
                    selectedFragment = new SubscriberProfileFragment();
                }

                if (selectedFragment != null) loadFragment(selectedFragment, true);
                return true;
            });
        }
        syncProfilePicture();
    }

    private void showLogoutDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_logout_bottom_sheet, null);
        dialog.setContentView(view);
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        ((MaterialButton)view.findViewById(R.id.btnLogout)).setOnClickListener(v -> {
            getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    public void toggleSystemUI(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(visibility);
        View headerLayout = findViewById(R.id.headerLayout);
        if (headerLayout != null) headerLayout.setVisibility(visibility);
        if (ivProfile != null) ivProfile.setVisibility(visibility);
    }

    private void loadFragment(Fragment fragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate) transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}