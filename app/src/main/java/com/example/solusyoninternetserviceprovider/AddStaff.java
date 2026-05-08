package com.example.solusyoninternetserviceprovider;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AddStaff extends AppCompatActivity {

    private EditText etStaffName, etStaffEmail, etStaffPhone, etBirthdate, etAddress;
    private Spinner spinnerSex, spinnerRole;
    private LinearLayout layoutUploadPhoto;
    private Button btnRegisterStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addstaff);

        // Initialize Views
        etStaffName = findViewById(R.id.etStaffName);
        etStaffEmail = findViewById(R.id.etStaffEmail);
        etStaffPhone = findViewById(R.id.etStaffPhone);
        etBirthdate = findViewById(R.id.etBirthdate);
        etAddress = findViewById(R.id.etAddress);
        spinnerSex = findViewById(R.id.spinnerSex);
        spinnerRole = findViewById(R.id.spinnerRole);
        layoutUploadPhoto = findViewById(R.id.layoutUploadPhoto);
        btnRegisterStaff = findViewById(R.id.btnRegisterStaff);

        setupSpinners();
        setupDatePicker();

        layoutUploadPhoto.setOnClickListener(v -> {
            // Placeholder for Image Picker Logic
            Toast.makeText(this, "Opening Gallery...", Toast.LENGTH_SHORT).show();
        });

        btnRegisterStaff.setOnClickListener(v -> {
            if (validateForm()) {
                // Logic for database saving would go here
                Toast.makeText(this, "Staff Registered Successfully", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupSpinners() {
        // Sex Options
        String[] sexOptions = {"Select", "Male", "Female"};
        ArrayAdapter<String> sexAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, sexOptions);
        spinnerSex.setAdapter(sexAdapter);

        // Specific Roles: Owner, Admin, Technician, Finance
        String[] roleOptions = {"Assign role", "Owner", "Admin", "Technician", "Finance"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roleOptions);
        spinnerRole.setAdapter(roleAdapter);
    }

    private void setupDatePicker() {
        etBirthdate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) ->
                            etBirthdate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year1),
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private boolean validateForm() {
        String name = etStaffName.getText().toString().trim();
        String email = etStaffEmail.getText().toString().trim();
        String phone = etStaffPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etStaffName.setError("Full name required");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etStaffEmail.setError("Valid Gmail/Email required");
            return false;
        }
        if (phone.isEmpty()) {
            etStaffPhone.setError("Contact number required");
            return false;
        }
        if (spinnerSex.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select sex", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerRole.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please assign a role", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}