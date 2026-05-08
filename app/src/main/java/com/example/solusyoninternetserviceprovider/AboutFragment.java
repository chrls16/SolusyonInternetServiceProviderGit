package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        LinearLayout layoutLocation = view.findViewById(R.id.layoutLocation);
        LinearLayout layoutEmail = view.findViewById(R.id.layoutEmail);
        LinearLayout layoutPhone = view.findViewById(R.id.layoutPhone);
        MaterialButton btnSendMessage = view.findViewById(R.id.btnSendMessage);

        // Location -> Open Maps
        layoutLocation.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=Purok+Kamagong+Poblacion+Norte+Paracale+Camarines+Norte");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        // Phone -> Open Dialer
        layoutPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+63028449021"));
            startActivity(intent);
        });

        // Email Section & Send Message Button -> Open Email Client
        View.OnClickListener emailClick = v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@solusyon.net"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Customer Inquiry - Solusyon ISP");
            startActivity(Intent.createChooser(intent, "Contact via:"));
        };

        layoutEmail.setOnClickListener(emailClick);
        btnSendMessage.setOnClickListener(emailClick);

        return view;
    }
}