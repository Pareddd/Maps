package com.example.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment {
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    enableMyLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(getContext(), "Izin ditolak. Peta tidak bisa melacak lokasimu.", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(getContext(), getContext().getSharedPreferences("osmdroid_prefs", android.content.Context.MODE_PRIVATE));

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint defaultPoint = new GeoPoint(-5.147665, 119.432731);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(defaultPoint);

        checkLocationPermission();

        if (getArguments() != null) {
            double lat = getArguments().getDouble("lat", 0.0);
            double lng = getArguments().getDouble("lng", 0.0);
            String name = getArguments().getString("name", "Lokasi Kurir");

            if (lat != 0.0 && lng != 0.0) {
                GeoPoint targetLocation = new GeoPoint(lat, lng);

                mapView.getController().setCenter(targetLocation);
                mapView.getController().setZoom(14.5);

                Marker startMarker = new Marker(mapView);
                startMarker.setPosition(targetLocation);
                startMarker.setTitle(name);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(startMarker);
                mapView.invalidate();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    GeoPoint myCurrentPoint = null;
                    if (myLocationOverlay != null) {
                        myCurrentPoint = myLocationOverlay.getMyLocation();
                    }

                    if (myCurrentPoint == null) {
                        Toast.makeText(getContext(), "Sinyal GPS lemah. Menggunakan titik simulasi untuk rute.", Toast.LENGTH_SHORT).show();
                        myCurrentPoint = new GeoPoint(-5.147665, 119.432731);
                    }

                    drawRoute(myCurrentPoint, targetLocation);
                }, 1500);
            }
        }

        return view;
    }

    private void drawRoute(GeoPoint startPoint, GeoPoint destinationPoint) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            RoadManager roadManager = new OSRMRoadManager(requireContext(), "RadarEkspedisi/1.0");

            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(startPoint);
            waypoints.add(destinationPoint);

            Road road = roadManager.getRoad(waypoints);

            handler.post(() -> {
                if (road.mStatus != Road.STATUS_OK) {
                    Toast.makeText(getContext(), "Gagal mencari rute. Cek koneksi internet.", Toast.LENGTH_SHORT).show();
                } else {
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    roadOverlay.setWidth(15.0f);
                    roadOverlay.setColor(Color.parseColor("#3498DB"));

                    mapView.getOverlays().add(roadOverlay);
                    mapView.invalidate();
                }
            });
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocation() {
        if (mapView == null) return;

        GpsMyLocationProvider provider = new GpsMyLocationProvider(requireContext());
        myLocationOverlay = new MyLocationNewOverlay(provider, mapView);
        myLocationOverlay.enableMyLocation();

        mapView.getOverlays().add(myLocationOverlay);
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }
}