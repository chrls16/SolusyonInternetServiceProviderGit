package com.example.solusyoninternetserviceprovider;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.*;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.HashMap;
import java.util.Map;

public class RegionalMapFragment extends Fragment {

    private MapView map;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        View view = inflater.inflate(R.layout.fragment_regional_map, container, false);

        map = view.findViewById(R.id.mapView);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14.5);
        map.getController().setCenter(new GeoPoint(14.2861, 122.7844)); // Paracale Center

        dbRef = FirebaseDatabase.getInstance().getReference("ServiceApplications");
        loadBarangayData();

        return view;
    }

    private void loadBarangayData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> counts = new HashMap<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if ("completed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(status)) {
                        String b = ds.child("barangay").getValue(String.class);
                        if (b != null) counts.put(b.toLowerCase().trim(), counts.getOrDefault(b.toLowerCase().trim(), 0) + 1);
                    }
                }
                addMarkersToMap(counts);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addMarkersToMap(Map<String, Integer> counts) {
        map.getOverlays().clear();
        // Define coordinates for Paracale Barangays
        addBarangayMarker("Bagumbayan", 14.2882, 122.7845, counts);
        addBarangayMarker("Palanas", 14.2825, 122.7920, counts);
        addBarangayMarker("Poblacion Norte", 14.2861, 122.7844, counts);
        addBarangayMarker("Poblacion Sur", 14.2835, 122.7850, counts);
        addBarangayMarker("Tugos", 14.2950, 122.7750, counts);
        map.invalidate();
    }

    private void addBarangayMarker(String name, double lat, double lon, Map<String, Integer> counts) {
        int count = counts.getOrDefault(name.toLowerCase(), 0);
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setTitle(name);
        marker.setSnippet("Subscribers: " + count);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
    }
}