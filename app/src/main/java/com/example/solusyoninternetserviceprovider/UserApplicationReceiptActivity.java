package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button; // Ensure this is imported
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class UserApplicationReceiptActivity extends AppCompatActivity {

    private RelativeLayout statusContainer;
    private TextView tvStatusTitle, tvStatusDesc;
    private ImageView statusIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_application_receipt);

        // 1. Initialize Views
        statusContainer = findViewById(R.id.statusContainer);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusDesc = findViewById(R.id.tvStatusDesc);
        statusIcon = findViewById(R.id.statusIcon);

        TextView tvReceiptID = findViewById(R.id.tvReceiptID);
        TextView tvDateIssued = findViewById(R.id.tvDateIssued);
        TextView tvReceiptName = findViewById(R.id.tvReceiptName);
        TextView tvReceiptPhone = findViewById(R.id.tvReceiptPhone);
        TextView tvPlanName = findViewById(R.id.tvPlanName);
        TextView tvPaymentName = findViewById(R.id.tvPaymentName);
        ImageView imgPaymentLogo = findViewById(R.id.imgPaymentLogo);

        // Find the "Proceed to Create Account" button
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // 2. Fill the receipt data from Intent
        Bundle data = getIntent().getExtras();
        if (data != null) {
            tvReceiptID.setText(data.getString("appId"));
            tvDateIssued.setText(data.getString("date"));
            tvReceiptName.setText(data.getString("fullName"));
            tvReceiptPhone.setText(data.getString("phone"));
            tvPlanName.setText(data.getString("plan") + " Fiber Plan");

            String payment = data.getString("payment");
            tvPaymentName.setText(payment);

            if (payment != null && payment.contains("Gcash")) {
                imgPaymentLogo.setImageResource(R.drawable.gcash);
            } else if (payment != null && payment.contains("Maya")) {
                imgPaymentLogo.setImageResource(R.drawable.ic_maya);
            }
        }

        // 3. Set Status to Pending
        updateStatus("pending");

        // 4. Button Logic: Go to Login
        btnCreateAccount.setOnClickListener(v -> {
            // Point to MainActivity
            Intent intent = new Intent(UserApplicationReceiptActivity.this, MainActivity.class);

            // This flag tells MainActivity to load the LoginFragment
            intent.putExtra("SHOW_LOGIN", true);

            // Clear the activity stack so the user can't "Go Back" to the receipt
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }


private void updateStatus(String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "approved":
                statusContainer.setBackgroundResource(R.drawable.bg_status_approved);
                tvStatusTitle.setText("Approved");
                tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.green_dark));
                tvStatusDesc.setText("Your internet service request has\nbeen processed and verified.");
                statusIcon.setImageResource(R.drawable.ic_check_circle);
                break;

            case "pending":
                // Using setBackgroundResource or setBackgroundColor depending on your drawable
                statusContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow_light));
                tvStatusTitle.setText("Pending");
                tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.yellow_dark));
                tvStatusDesc.setText("Your application is currently under\nreview by our technical team.");
                statusIcon.setImageResource(R.drawable.ic_clock);
                break;

            case "denied":
                statusContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.red_light));
                tvStatusTitle.setText("Denied");
                tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.red_dark));
                tvStatusDesc.setText("We couldn't verify your details.\nPlease contact support for help.");
                statusIcon.setImageResource(R.drawable.ic_error);
                break;
        }
    }
}