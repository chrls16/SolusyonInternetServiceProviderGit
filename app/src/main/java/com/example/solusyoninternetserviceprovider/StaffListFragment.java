package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class StaffListFragment extends Fragment {

    private RecyclerView rvStaffList;
    private StaffAdapter adapter;
    private List<StaffModel> staffList;
    private List<StaffModel> filteredList;
    private DatabaseReference mDatabase;
    private ChipGroup chipGroupFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_list, container, false);

        rvStaffList = view.findViewById(R.id.rvStaffList);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);

        staffList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new StaffAdapter(filteredList);

        rvStaffList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStaffList.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Staff");

        fetchStaffData();
        setupFilter();

        return view;
    }

    private void fetchStaffData() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                staffList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    StaffModel staff = ds.getValue(StaffModel.class);
                    if (staff != null) {
                        staffList.add(staff);
                    }
                }
                filterList("All Staff");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupFilter() {
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) filterList("All Staff");
            else if (checkedId == R.id.chipOwner) filterList("Owner");
            else if (checkedId == R.id.chipAdmin) filterList("Admin");
            else if (checkedId == R.id.chipTechnician) filterList("Technician");
            else if (checkedId == R.id.chipFinance) filterList("Finance");
        });
    }

    private void filterList(String role) {
        filteredList.clear();
        if (role.equals("All Staff")) {
            filteredList.addAll(staffList);
        } else {
            for (StaffModel staff : staffList) {
                if (staff.getRole() != null && staff.getRole().equalsIgnoreCase(role)) {
                    filteredList.add(staff);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
