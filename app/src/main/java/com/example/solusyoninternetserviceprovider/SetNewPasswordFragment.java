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

        // Navigation
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnUpdatePassword.setOnClickListener(v -> {
            String newP = etNewPassword.getText().toString().trim();
            String confP = etConfirmPassword.getText().toString().trim();

            if (newP.isEmpty() || confP.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!newP.equals(confP)) {
                etConfirmPassword.setError("Passwords do not match");
            } else {
                // Procedural success
                Toast.makeText(getContext(), "Password successfully updated!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}