package com.example.solusyoninternetserviceprovider;
import android.graphics.Color; import android.os.Bundle; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup; import android.widget.Button; import android.widget.EditText; import android.widget.RadioButton; import android.widget.TextView; import android.widget.Toast; import androidx.annotation.NonNull; import androidx.annotation.Nullable; import androidx.fragment.app.Fragment; import com.google.android.material.card.MaterialCardView;
public class CreateAnnouncementFragment extends Fragment {
    private EditText etTitle, etDescription;
    private RadioButton rbInfo, rbWarning, rbCritical;
    private MaterialCardView cardInfo, cardWarning, cardCritical; // Added cards
    private TextView tvSaveDraft;
    private Button btnBroadcast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_announcement, container, false);

        // 1. Initialize Buttons and Text Fields
        etTitle = view.findViewById(R.id.etAnnouncementTitle);
        etDescription = view.findViewById(R.id.etMessageDescription);
        rbInfo = view.findViewById(R.id.rbInfo);
        rbWarning = view.findViewById(R.id.rbWarning);
        rbCritical = view.findViewById(R.id.rbCritical);
        tvSaveDraft = view.findViewById(R.id.tvSaveDraft);
        btnBroadcast = view.findViewById(R.id.btnBroadcastNow);

        // 2. Initialize Parent CardViews (To show active state)
        cardInfo = (MaterialCardView) rbInfo.getParent();
        cardWarning = (MaterialCardView) rbWarning.getParent();
        cardCritical = (MaterialCardView) rbCritical.getParent();

        // 3. Setup Custom Selection Logic
        View.OnClickListener listener = v -> {
            // Determine which ID was clicked (v can be the RadioButton or the Card)
            int id = v.getId();
            updateSelection(id);
        };

        rbInfo.setOnClickListener(listener);
        rbWarning.setOnClickListener(listener);
        rbCritical.setOnClickListener(listener);

        // Also make cards clickable
        cardInfo.setOnClickListener(v -> updateSelection(R.id.rbInfo));
        cardWarning.setOnClickListener(v -> updateSelection(R.id.rbWarning));
        cardCritical.setOnClickListener(v -> updateSelection(R.id.rbCritical));

        // Initial default state
        updateSelection(R.id.rbInfo);

        // 4. Save/Broadcast listeners
        tvSaveDraft.setOnClickListener(v -> Toast.makeText(getContext(), "Draft saved", Toast.LENGTH_SHORT).show());
        // Inside CreateAnnouncementFragment.java -> btnBroadcast.setOnClickListener

        btnBroadcast.setOnClickListener(v -> {
            if (validateForm()) {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                String category = "INFO";
                String colorHex = "#2D62B5";
                if (rbWarning.isChecked()) { category = "WARNING"; colorHex = "#854D0E"; }
                else if (rbCritical.isChecked()) { category = "CRITICAL"; colorHex = "#991B1B"; }

                String timestamp = new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());

                // Use the model for type safety
                AnnouncementModel announcement = new AnnouncementModel(category, title, description, timestamp, colorHex);

                btnBroadcast.setEnabled(false);
                btnBroadcast.setText("Publishing...");

                com.google.firebase.database.FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("Announcements")
                        .push()
                        .setValue(announcement)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Broadcast Published!", Toast.LENGTH_LONG).show();
                            getParentFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> {
                            btnBroadcast.setEnabled(true);
                            btnBroadcast.setText("Broadcast Now");
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        return view;
    }

    private void updateSelection(int selectedId) {
        // Reset all buttons
        rbInfo.setChecked(selectedId == R.id.rbInfo);
        rbWarning.setChecked(selectedId == R.id.rbWarning);
        rbCritical.setChecked(selectedId == R.id.rbCritical);

        // Update card visual states
        resetCardStyle(cardInfo);
        resetCardStyle(cardWarning);
        resetCardStyle(cardCritical);

        if (selectedId == R.id.rbInfo) applyActiveStyle(cardInfo, "#2D62B5");
        else if (selectedId == R.id.rbWarning) applyActiveStyle(cardWarning, "#854D0E");
        else if (selectedId == R.id.rbCritical) applyActiveStyle(cardCritical, "#991B1B");
    }

    private void resetCardStyle(MaterialCardView card) {
        card.setStrokeWidth(0);
    }

    private void applyActiveStyle(MaterialCardView card, String colorHex) {
        card.setStrokeWidth(dpToPx(2));
        card.setStrokeColor(Color.parseColor(colorHex));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private boolean validateForm() {
        if (etTitle.getText().toString().isEmpty()) { etTitle.setError("Required"); return false; }
        if (etDescription.getText().toString().isEmpty()) { etDescription.setError("Required"); return false; }
        return true;
    }
}