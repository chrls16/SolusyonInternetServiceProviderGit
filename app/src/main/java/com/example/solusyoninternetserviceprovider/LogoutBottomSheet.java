package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutBottomSheet extends BottomSheetDialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        // Remove the default pinkish Material 3 background from the container
        if (getDialog() != null) {
            View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // Setting this to transparent allows your white XML background to show through perfectly
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the XML layout we created for the logout UI
        View v = inflater.inflate(R.layout.layout_logout_bottom_sheet, container, false);

        MaterialButton btnLogout = v.findViewById(R.id.btnLogout);
        TextView tvCancel = v.findViewById(R.id.tvCancel);

        tvCancel.setOnClickListener(view -> dismiss());

        btnLogout.setOnClickListener(view -> {
            // 1. Clear session data
            requireActivity().getSharedPreferences("UserSession", 0).edit().clear().apply();

            // 2. Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // 3. Go back to Welcome screen
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dismiss();
            requireActivity().finish();
        });

        return v;
    }
}