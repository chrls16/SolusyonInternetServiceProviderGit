package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CreateAnnouncementFragment extends Fragment {

    private EditText etTitle, etDescription;
    private RadioGroup rgUrgency;
    private TextView tvSaveDraft;
    private Button btnBroadcast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_announcement, container, false);

        // Initialize Views
        etTitle = view.findViewById(R.id.etAnnouncementTitle);
        etDescription = view.findViewById(R.id.etMessageDescription);
        rgUrgency = view.findViewById(R.id.rgUrgency);
        tvSaveDraft = view.findViewById(R.id.tvSaveDraft);
        btnBroadcast = view.findViewById(R.id.btnBroadcastNow);

        // Save as Draft Click
        tvSaveDraft.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Announcement saved to Drafts", Toast.LENGTH_SHORT).show();
        });

        // Broadcast Click
        btnBroadcast.setOnClickListener(v -> {
            if (validateForm()) {
                // Get selected Urgency Level
                int selectedId = rgUrgency.getCheckedRadioButtonId();
                String urgency = "";
                if (selectedId == R.id.rbInfo) urgency = "Info";
                else if (selectedId == R.id.rbWarning) urgency = "Warning";
                else if (selectedId == R.id.rbCritical) urgency = "Critical";

                String title = etTitle.getText().toString().trim();

                // Final submission feedback
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
        return true;
    }
}