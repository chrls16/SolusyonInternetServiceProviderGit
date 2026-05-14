package com.example.solusyoninternetserviceprovider;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class AddStaff extends AppCompatActivity {

    private EditText etStaffName, etStaffEmail, etStaffPhone, etBirthdate, etAddress;
    private Spinner spinnerSex, spinnerRole;
    private LinearLayout layoutUploadPhoto;
    private ImageView ivStaffPhoto; // Reference to the photo view
    private Button btnRegisterStaff;
    private TextView tvAddStaffTitle;

    private String base64Image = "";
    private String staffIdToEdit = null;
    private boolean isEditMode = false;

    private DatabaseReference mDatabase;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    processAndSetImage(result.getData().getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addstaff);

        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Staff");

        initializeViews();
        setupSpinners();
        setupDatePicker();

        StaffModel staffToEdit = (StaffModel) getIntent().getSerializableExtra("staff_data");
        if (staffToEdit != null) {
            isEditMode = true;
            staffIdToEdit = staffToEdit.getStaffId();
            prefillData(staffToEdit);
        }

        layoutUploadPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnRegisterStaff.setOnClickListener(v -> {
            if (validateForm()) {
                saveStaffToFirebase();
            }
        });
    }

    private void initializeViews() {
        etStaffName = findViewById(R.id.etStaffName);
        etStaffEmail = findViewById(R.id.etStaffEmail);
        etStaffPhone = findViewById(R.id.etStaffPhone);
        etBirthdate = findViewById(R.id.etBirthdate);
        etAddress = findViewById(R.id.etAddress);
        spinnerSex = findViewById(R.id.spinnerSex);
        spinnerRole = findViewById(R.id.spinnerRole);
        layoutUploadPhoto = findViewById(R.id.layoutUploadPhoto);
        ivStaffPhoto = findViewById(R.id.ivStaffPhoto); // Initialize
        btnRegisterStaff = findViewById(R.id.btnRegisterStaff);
        tvAddStaffTitle = findViewById(R.id.tvAddStaffTitle);
    }

    private void prefillData(StaffModel staff) {
        if (tvAddStaffTitle != null) tvAddStaffTitle.setText("Edit Staff Information");
        btnRegisterStaff.setText("Save Changes");

        etStaffName.setText(staff.getName());
        etStaffEmail.setText(staff.getEmail());
        etStaffPhone.setText(staff.getPhone());
        etBirthdate.setText(staff.getBirthdate());
        etAddress.setText(staff.getAddress());
        base64Image = staff.getImageUrl();

        // Decode and set image if exists
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                ivStaffPhoto.setImageBitmap(bitmap);
                ivStaffPhoto.setPadding(0, 0, 0, 0); // Remove placeholder padding
                ivStaffPhoto.setColorFilter(null); // Remove tint
            } catch (Exception ignored) {}
        }

        setSpinnerValue(spinnerSex, staff.getSex());
        setSpinnerValue(spinnerRole, staff.getRole());
    }

    private void processAndSetImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // 1. CROP TO SQUARE (Center Crop logic)
            Bitmap squareBitmap;
            if (bitmap.getWidth() >= bitmap.getHeight()) {
                squareBitmap = Bitmap.createBitmap(
                        bitmap,
                        bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                        0,
                        bitmap.getHeight(),
                        bitmap.getHeight()
                );
            } else {
                squareBitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                        bitmap.getWidth(),
                        bitmap.getWidth()
                );
            }

            // 2. RESIZE for database (now it stays perfectly proportional)
            Bitmap resized = Bitmap.createScaledBitmap(squareBitmap, 400, 400, true);

            // 3. Update UI
            ivStaffPhoto.setImageBitmap(resized);
            ivStaffPhoto.setPadding(0, 0, 0, 0);
            ivStaffPhoto.setColorFilter(null);

            // 4. Convert to Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

            Toast.makeText(this, "Photo Applied", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveStaffToFirebase() {
        btnRegisterStaff.setEnabled(false);
        String name = etStaffName.getText().toString().trim();
        String email = etStaffEmail.getText().toString().trim();
        String phone = etStaffPhone.getText().toString().trim();
        String bday = etBirthdate.getText().toString().trim();
        String sex = spinnerSex.getSelectedItem().toString();
        String addr = etAddress.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        StaffModel staff = new StaffModel(name, email, phone, bday, sex, addr, role, base64Image, addr);

        if (isEditMode) {
            staff.setStaffId(staffIdToEdit);
            mDatabase.child(staffIdToEdit).setValue(staff).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Staff Updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            String newId = mDatabase.push().getKey();
            staff.setStaffId(newId);
            mDatabase.child(newId).setValue(staff).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Staff Registered", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void setupSpinners() {
        String[] sexOptions = {"Select", "Male", "Female"};
        spinnerSex.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sexOptions));
        String[] roleOptions = {"Assign role", "Owner", "Admin", "Technician", "Finance"};
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roleOptions));
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupDatePicker() {
        etBirthdate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> etBirthdate.setText((m + 1) + "/" + d + "/" + y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private boolean validateForm() {
        if (etStaffName.getText().toString().isEmpty()) { etStaffName.setError("Required"); return false; }
        if (etStaffEmail.getText().toString().isEmpty()) { etStaffEmail.setError("Required"); return false; }
        if (spinnerRole.getSelectedItemPosition() == 0) { Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }
}