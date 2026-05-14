package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        TextInputEditText etEmail = view.findViewById(R.id.etResetEmail);
        MaterialButton btnSend = view.findViewById(R.id.btnSendReset);
        TextView tvBackToSignIn = view.findViewById(R.id.tvBackToSignIn);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        tvBackToSignIn.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                return;
            }

            btnSend.setEnabled(false);
            btnSend.setText("Sending...");

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        btnSend.setEnabled(true);
                        btnSend.setText("Send Reset Link");
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Reset link sent! Check your email.", Toast.LENGTH_LONG).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }
}