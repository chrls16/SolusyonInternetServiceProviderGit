package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvCurrentMonthYear, tvLogisticsCount;
    private ImageView btnPrevMonth, btnNextMonth;
    private RecyclerView rvCalendarGrid, rvEvents;
    private MaterialButton btnViewList, btnCreateAnnouncement, btnAddStaff, btnAddPlan;
    private View cardStaffList;

    private EventAdapter eventAdapter;
    private List<EventSchedule> eventList;
    private List<EventSchedule> displayedEvents;

    private Calendar calendar;
    private DatabaseReference eventRef, applicationRef;

    private ValueEventListener eventListener, applicationListener;

    public DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Safety check — show Header and BottomNav when entering Dashboard
        if (getActivity() != null) {
            View headerLayout = getActivity().findViewById(R.id.headerLayout);
            if (headerLayout != null) headerLayout.setVisibility(View.VISIBLE);

            View bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }

        // 1. Initialize DB
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://solusyon-isp-default-rtdb.asia-southeast1.firebasedatabase.app");
        eventRef = database.getReference("EventSchedule/Upcoming");
        applicationRef = database.getReference("ServiceApplications");

        // 2. UI Setup
        tvCurrentMonthYear = view.findViewById(R.id.tvCurrentMonthYear);
        tvLogisticsCount = view.findViewById(R.id.tvLogisticsCount);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        btnViewList = view.findViewById(R.id.btnViewList);
        rvCalendarGrid = view.findViewById(R.id.rvCalendarGrid);
        rvEvents = view.findViewById(R.id.rvEvents);
        btnCreateAnnouncement = view.findViewById(R.id.btnCreateAnnouncement);
        btnAddStaff = view.findViewById(R.id.btnAddStaff);
        btnAddPlan = view.findViewById(R.id.btnAddPlan); // Initialize btnAddPlan
        cardStaffList = view.findViewById(R.id.cardStaffList);

        // 3. Initialize Lists
        eventList = new ArrayList<>();
        displayedEvents = new ArrayList<>();

        // 4. Calendar Setup
        calendar = Calendar.getInstance();
        setupCalendarGrid();

        btnPrevMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            setupCalendarGrid();
        });

        btnNextMonth.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            setupCalendarGrid();
        });

        // 5. Button Navigation
        btnViewList.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), PendingStatusActivity.class);
                startActivity(intent);
            }
        });

        if (btnCreateAnnouncement != null) {
            btnCreateAnnouncement.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new CreateAnnouncementFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (btnAddStaff != null) {
            btnAddStaff.setOnClickListener(v -> {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), AddStaff.class);
                    startActivity(intent);
                }
            });
        }

        // NEW: Add Plan Navigation logic
        if (btnAddPlan != null) {
            btnAddPlan.setOnClickListener(v -> {
                // Switch to Plan Management Fragment
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new PlanManagementFragment())
                        .addToBackStack(null)
                        .commit();

                // Synchronize the selection on the Bottom Navigation Bar
                if (getActivity() != null) {
                    BottomNavigationView nav = getActivity().findViewById(R.id.bottomNavigation);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_plans);
                    }
                }
            });
        }

        // 6. Staff List Navigation
        if (cardStaffList != null) {
            cardStaffList.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new StaffListFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // 7. Adapters Setup
        if (getContext() != null) {
            rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
            eventAdapter = new EventAdapter(displayedEvents);
            rvEvents.setAdapter(eventAdapter);
        }

        // 8. Fetch Data
        fetchEventsData();
        fetchApplicationCount();
    }

    private void fetchApplicationCount() {
        applicationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if ("pending".equalsIgnoreCase(status)) {
                        count++;
                    }
                }
                if (isAdded() && tvLogisticsCount != null) {
                    tvLogisticsCount.setText(String.valueOf(count));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        applicationRef.addValueEventListener(applicationListener);
    }

    public void filterEvents(String selectedDate) {
        if (!isAdded() || eventList == null) return;

        displayedEvents.clear();
        for (EventSchedule event : eventList) {
            if (event.getDate() != null && event.getDate().equals(selectedDate)) {
                displayedEvents.add(event);
            }
        }

        if (eventAdapter != null) {
            eventAdapter.notifyDataSetChanged();
        }

        if (displayedEvents.isEmpty() && getContext() != null) {
            Toast.makeText(getContext(), "No appointments for " + selectedDate, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCalendarGrid() {
        if (!isAdded() || getContext() == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvCurrentMonthYear.setText(sdf.format(calendar.getTime()));

        List<String> daysInMonth = new ArrayList<>();
        Calendar calcCal = (Calendar) calendar.clone();
        calcCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calcCal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInTotal = calcCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) daysInMonth.add("");
        for (int i = 1; i <= daysInTotal; i++) daysInMonth.add(String.valueOf(i));

        rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, eventList,
                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), this);
        rvCalendarGrid.setAdapter(calendarAdapter);
    }

    private void fetchEventsData() {
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                eventList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    EventSchedule event = ds.getValue(EventSchedule.class);
                    if (event != null) {
                        event.setKey(ds.getKey());
                        eventList.add(event);
                    }
                }
                setupCalendarGrid();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        eventRef.addValueEventListener(eventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventRef != null && eventListener != null) eventRef.removeEventListener(eventListener);
        if (applicationRef != null && applicationListener != null) applicationRef.removeEventListener(applicationListener);
    }
}