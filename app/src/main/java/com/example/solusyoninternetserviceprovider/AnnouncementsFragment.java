package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementsFragment extends Fragment {

    private RecyclerView rvAnnouncements;
    private TextView tvMarkRead;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_announcements, container, false);
    }

// Inside AnnouncementsFragment.java -> onViewCreated

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAnnouncements = view.findViewById(R.id.rvAnnouncements);
        tvMarkRead = view.findViewById(R.id.tvMarkRead);
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(getContext()));

        List<AnnouncementModel> list = new ArrayList<>();
        AnnouncementAdapter adapter = new AnnouncementAdapter(list);
        rvAnnouncements.setAdapter(adapter);

        // Fetch from Firebase
        com.google.firebase.database.FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Announcements")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        list.clear();
                        for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                            AnnouncementModel item = ds.getValue(AnnouncementModel.class);
                            if (item != null) {
                                list.add(0, item); // Newest at top
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });

        tvMarkRead.setOnClickListener(v -> Toast.makeText(getContext(), "All announcements marked as read", Toast.LENGTH_SHORT).show());
    }
}