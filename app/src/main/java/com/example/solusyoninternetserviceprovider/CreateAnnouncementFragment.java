package com.example.solusyoninternetserviceprovider;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
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

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAnnouncementFragment extends Fragment {

    private EditText etTitle, etDescription;
    private RadioButton rbInfo, rbWarning, rbCritical;
    private MaterialCardView cardInfo, cardWarning, cardCritical;
    private Button btnBroadcast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_announcement, container, false);

        initializeViews(view);
        setupSelectionLogic();

        btnBroadcast.setOnClickListener(v -> {
            if (validateForm()) {
                publishToFirebase();
            }
        });

        return view;
    }

    private void initializeViews(View view) {
        etTitle = view.findViewById(R.id.etAnnouncementTitle);
        etDescription = view.findViewById(R.id.etMessageDescription);
        rbInfo = view.findViewById(R.id.rbInfo);
        rbWarning = view.findViewById(R.id.rbWarning);
        rbCritical = view.findViewById(R.id.rbCritical);
        btnBroadcast = view.findViewById(R.id.btnBroadcastNow);

        cardInfo = (MaterialCardView) rbInfo.getParent();
        cardWarning = (MaterialCardView) rbWarning.getParent();
        cardCritical = (MaterialCardView) rbCritical.getParent();
    }

    private void setupSelectionLogic() {
        View.OnClickListener listener = v -> updateSelection(v.getId());
        rbInfo.setOnClickListener(listener);
        rbWarning.setOnClickListener(listener);
        rbCritical.setOnClickListener(listener);
        cardInfo.setOnClickListener(v -> updateSelection(R.id.rbInfo));
        cardWarning.setOnClickListener(v -> updateSelection(R.id.rbWarning));
        cardCritical.setOnClickListener(v -> updateSelection(R.id.rbCritical));
        updateSelection(R.id.rbInfo);
    }

    private void updateSelection(int selectedId) {
        rbInfo.setChecked(selectedId == R.id.rbInfo);
        rbWarning.setChecked(selectedId == R.id.rbWarning);
        rbCritical.setChecked(selectedId == R.id.rbCritical);
        resetCardStyle(cardInfo);
        resetCardStyle(cardWarning);
        resetCardStyle(cardCritical);
        if (selectedId == R.id.rbInfo) applyActiveStyle(cardInfo, "#2D62B5");
        else if (selectedId == R.id.rbWarning) applyActiveStyle(cardWarning, "#854D0E");
        else if (selectedId == R.id.rbCritical) applyActiveStyle(cardCritical, "#991B1B");
    }

    private void applyActiveStyle(MaterialCardView card, String colorHex) {
        card.setStrokeWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        card.setStrokeColor(Color.parseColor(colorHex));
    }

    private void resetCardStyle(MaterialCardView card) { card.setStrokeWidth(0); }

    private void publishToFirebase() {
        btnBroadcast.setEnabled(false);
        btnBroadcast.setText("Broadcasting...");

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String urgency = rbInfo.isChecked() ? "INFO" : rbWarning.isChecked() ? "WARNING" : "CRITICAL";
        String colorHex = rbInfo.isChecked() ? "#2D62B5" : rbWarning.isChecked() ? "#854D0E" : "#991B1B";
        String timestamp = new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());

        AnnouncementModel model = new AnnouncementModel(urgency, title, description, timestamp, colorHex);

        // This simple write will now trigger your server-side notification!
        FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Announcements")
                .push()
                .setValue(model)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Broadcast Successful!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    btnBroadcast.setEnabled(true);
                    btnBroadcast.setText("Broadcast Now");
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean validateForm() {
        if (etTitle.getText().toString().isEmpty()) { etTitle.setError("Required"); return false; }
        if (etDescription.getText().toString().isEmpty()) { etDescription.setError("Required"); return false; }
        return true;
    }
}