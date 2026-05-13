package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CreateAnnouncementFragment extends Fragment {

    private EditText etTitle, etDescription;
    private RadioButton rbInfo, rbWarning, rbCritical; // Replaced RadioGroup with individual buttons
    private TextView tvSaveDraft;
    private Button btnBroadcast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_announcement, container, false);

        // Initialize Views
        etTitle = view.findViewById(R.id.etAnnouncementTitle);
        etDescription = view.findViewById(R.id.etMessageDescription);
        rbInfo = view.findViewById(R.id.rbInfo);
        rbWarning = view.findViewById(R.id.rbWarning);
        rbCritical = view.findViewById(R.id.rbCritical);
        tvSaveDraft = view.findViewById(R.id.tvSaveDraft);
        btnBroadcast = view.findViewById(R.id.btnBroadcastNow);

        // Manual RadioGroup Logic: Ensure only one is checked at a time
        View.OnClickListener urgencyListener = v -> {
            rbInfo.setChecked(v.getId() == R.id.rbInfo);
            rbWarning.setChecked(v.getId() == R.id.rbWarning);
            rbCritical.setChecked(v.getId() == R.id.rbCritical);
        };

        rbInfo.setOnClickListener(urgencyListener);
        rbWarning.setOnClickListener(urgencyListener);
        rbCritical.setOnClickListener(urgencyListener);

        // Save as Draft Click
        tvSaveDraft.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Announcement saved to Drafts", Toast.LENGTH_SHORT).show();
        });

        // Broadcast Click
        btnBroadcast.setOnClickListener(v -> {
            if (validateForm()) {
                // Get selected Urgency Level manually
                String urgency = "";
                if (rbInfo.isChecked()) urgency = "Info";
                else if (rbWarning.isChecked()) urgency = "Warning";
                else if (rbCritical.isChecked()) urgency = "Critical";

                String title = etTitle.getText().toString().trim();
                Toast.makeText(getContext(), "Broadcast sent: " + title + " [" + urgency + "]", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private boolean validateForm() {
        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Title required");
            return false;
        }
        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Description required");
            return false;
        }
        // Check if at least one is selected
        if (!rbInfo.isChecked() && !rbWarning.isChecked() && !rbCritical.isChecked()) {
            Toast.makeText(getContext(), "Please select an urgency level", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
