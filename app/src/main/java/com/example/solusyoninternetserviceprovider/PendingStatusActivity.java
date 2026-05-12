package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.widget.ImageView; // Add this import
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class PendingStatusActivity extends AppCompatActivity {

    private RecyclerView rvPendingRequests;
    private PendingRequestAdapter adapter;
    private List<PendingRequestModel> requestList;
    private DatabaseReference mDatabase;
    private ImageView btnBack; // Add this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_status);

        // 1. Initialize Views
        rvPendingRequests = findViewById(R.id.rvPendingRequests);
        btnBack = findViewById(R.id.id_btnBack); // Initialize the back button

        rvPendingRequests.setLayoutManager(new LinearLayoutManager(this));

        // 2. Setup Back Button Listener
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Closes this activity and returns to the Dashboard
                finish();
            });
        }

        requestList = new ArrayList<>();
        adapter = new PendingRequestAdapter(requestList, this);
        rvPendingRequests.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("ServiceApplications");

        fetchPendingApplications();
    }

    private void fetchPendingApplications() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        PendingRequestModel model = ds.getValue(PendingRequestModel.class);
                        if (model != null) {
                            model.setUid(ds.getKey());
                            if ("pending".equalsIgnoreCase(model.getStatus())) {
                                requestList.add(model);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {
                        Toast.makeText(PendingStatusActivity.this, "No pending applications found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PendingStatusActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}