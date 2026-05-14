package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SetNewPasswordFragment extends Fragment {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnUpdatePassword, btnCancel;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_new_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Binding
        btnBack = view.findViewById(R.id.btnBack);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Retrieve oobCode passed from deep link / arguments
        String oobCode = getArguments() != null ? getArguments().getString("oobCode") : null;

        btnUpdatePassword.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            if (oobCode == null) {
                Toast.makeText(getContext(), "Invalid link", Toast.LENGTH_SHORT).show();
                return;
            }

            com.google.firebase.auth.FirebaseAuth.getInstance().confirmPasswordReset(oobCode, newPass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Success! Login with new password.", Toast.LENGTH_LONG).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}