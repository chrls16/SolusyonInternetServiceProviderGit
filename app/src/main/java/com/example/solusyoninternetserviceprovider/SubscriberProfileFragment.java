package com.example.solusyoninternetserviceprovider;
import android.app.Activity; import android.content.Intent; import android.graphics.Bitmap; import android.graphics.BitmapFactory; import android.location.Address; import android.location.Geocoder; import android.net.Uri; import android.os.Bundle; import android.preference.PreferenceManager; import android.util.Base64; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup; import android.widget.Button; import android.widget.EditText; import android.widget.ImageView; import android.widget.TextView; import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher; import androidx.activity.result.contract.ActivityResultContracts; import androidx.annotation.NonNull; import androidx.annotation.Nullable; import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth; import com.google.firebase.auth.FirebaseUser; import com.google.firebase.database.DataSnapshot; import com.google.firebase.database.DatabaseError; import com.google.firebase.database.DatabaseReference; import com.google.firebase.database.FirebaseDatabase; import com.google.firebase.database.ValueEventListener;
import org.osmdroid.config.Configuration; import org.osmdroid.util.GeoPoint; import org.osmdroid.views.MapView; import org.osmdroid.views.overlay.Marker;
import java.io.ByteArrayOutputStream; import java.io.IOException; import java.io.InputStream; import java.util.List; import java.util.Locale;
public class SubscriberProfileFragment extends Fragment {
    private ImageView profileImage;
    private TextView tvUserName, tvAccountId, tvPlanBadge, tvStatusBadge;
    private EditText etFullName, etContactNumber, etEmailAddress, etServiceAddress;
    private Button btnSaveChanges;
    private TextView tvModemModel, tvMacAddress, tvIpAllocation, tvInstallationDate;
    private TextView tvDataUsage;

    private MapView map = null;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button btnLogoutProfile;
    private final String DB_URL = "https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveImageAsBase64(imageUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Initialize OSMDroid
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        View view = inflater.inflate(R.layout.user_profile_dashboard, container, false);

        initViews(view);

        // 2. Map Setup
        map = view.findViewById(R.id.mapView);
        map.setMultiTouchControls(true);

        // FIX: Prevents ScrollView from intercepting map touches (zoom/pan)
        map.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        // Default view (Paracale center)
        GeoPoint startPoint = new GeoPoint(14.2861, 122.7844);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

        fetchUserData();

        profileImage.setOnClickListener(v -> openGallery());
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        btnLogoutProfile.setOnClickListener(v -> {
            // Show the same logout sheet used in the main activity
            LogoutBottomSheet bottomSheet = new LogoutBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "LogoutBottomSheet");
        });

        return view;
    }

    private void initViews(View v) {
        profileImage = v.findViewById(R.id.profileImage);
        tvUserName = v.findViewById(R.id.tvUserName);
        tvAccountId = v.findViewById(R.id.tvAccountId);
        tvPlanBadge = v.findViewById(R.id.tvPlanBadge);
        tvStatusBadge = v.findViewById(R.id.tvStatusBadge);
        etFullName = v.findViewById(R.id.etFullName);
        etContactNumber = v.findViewById(R.id.etContactNumber);
        etEmailAddress = v.findViewById(R.id.etEmailAddress);
        etServiceAddress = v.findViewById(R.id.etServiceAddress);
        btnSaveChanges = v.findViewById(R.id.btnSaveChanges);
        tvModemModel = v.findViewById(R.id.tvModemModel);
        tvMacAddress = v.findViewById(R.id.tvMacAddress);
        tvIpAllocation = v.findViewById(R.id.tvIpAllocation);
        tvInstallationDate = v.findViewById(R.id.tvInstallationDate);
        tvDataUsage = v.findViewById(R.id.tvDataUsage);
        btnLogoutProfile = v.findViewById(R.id.btnLogoutProfile);
        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveImageAsBase64(Uri imageUri) {
        if (mAuth.getCurrentUser() == null) return;
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // FIX: CROP TO SQUARE TO PREVENT COMPRESSION/STRETCHING
            Bitmap squareBitmap;
            if (bitmap.getWidth() >= bitmap.getHeight()) {
                squareBitmap = Bitmap.createBitmap(bitmap,
                        bitmap.getWidth() / 2 - bitmap.getHeight() / 2, 0,
                        bitmap.getHeight(), bitmap.getHeight());
            } else {
                squareBitmap = Bitmap.createBitmap(bitmap, 0,
                        bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                        bitmap.getWidth(), bitmap.getWidth());
            }

            // Resize the square version
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, 400, 400, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            String uid = mAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(uid).child("profilePictureUrl").setValue(base64String)
                    .addOnSuccessListener(aVoid -> {
                        profileImage.setImageBitmap(resizedBitmap);
                        if (isAdded()) Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        String uid = currentUser.getUid();

        // A. Basic User Info
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String profilePic = snapshot.child("profilePictureUrl").getValue(String.class);

                    if (name != null) { tvUserName.setText(name); etFullName.setText(name); }
                    if (email != null) etEmailAddress.setText(email);
                    if (phone != null) etContactNumber.setText(phone);

                    if (profilePic != null && !profilePic.isEmpty()) {
                        try {
                            byte[] decoded = Base64.decode(profilePic, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            profileImage.setImageBitmap(bitmap);
                        } catch (Exception e) { profileImage.setImageResource(R.drawable.profile_placeholder); }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // B. Technical Details & Address (Syncs Map)
        mDatabase.child("ServiceApplications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String appId = snapshot.child("applicationId").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String plan = snapshot.child("plan").getValue(String.class);
                    String barangay = snapshot.child("barangay").getValue(String.class);
                    String purok = snapshot.child("purok").getValue(String.class);

                    if (appId != null) tvAccountId.setText("Account ID: " + appId);
                    if (status != null) tvStatusBadge.setText("STATUS: " + status.toUpperCase());
                    if (plan != null) tvPlanBadge.setText(plan.toUpperCase() + " FIBER");

                    String address = (purok != null ? "Purok " + purok + ", " : "") + (barangay != null ? barangay : "") + ", Paracale";
                    etServiceAddress.setText(address);

                    // Sync map to the installation address
                    syncMapWithAddress(address);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void syncMapWithAddress(String address) {
        if (!isAdded() || getContext() == null) return;
        String fullAddress = address + ", Camarines Norte, Philippines";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(fullAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address loc = addresses.get(0);
                GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());

                map.getController().animateTo(point);
                map.getController().setZoom(18.0);

                map.getOverlays().clear();
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle("Installation Site");
                map.getOverlays().add(marker);
                map.invalidate();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveChanges() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        String uid = currentUser.getUid();

        String name = etFullName.getText().toString().trim();
        String phone = etContactNumber.getText().toString().trim();
        String email = etEmailAddress.getText().toString().trim();

        mDatabase.child("users").child(uid).child("fullName").setValue(name);
        mDatabase.child("users").child(uid).child("phone").setValue(phone);
        mDatabase.child("users").child(uid).child("email").setValue(email)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                        tvUserName.setText(name);
                    }
                });
    }

    @Override
    public void onResume() { super.onResume(); if (map != null) map.onResume(); }

    @Override
    public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}