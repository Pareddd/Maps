package com.example.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {
    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(getContext(), getContext().getSharedPreferences("osmdroid_prefs", android.content.Context.MODE_PRIVATE));

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint makassarPoint = new GeoPoint(-5.147665, 119.432731);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(makassarPoint);

        if (getArguments() != null) {
            double lat = getArguments().getDouble("lat", 0.0);
            double lng = getArguments().getDouble("lng", 0.0);
            String name = getArguments().getString("name", "Lokasi Kurir");

            if (lat != 0.0 && lng != 0.0) {
                GeoPoint targetLocation = new GeoPoint(lat, lng);

                mapView.getController().setCenter(targetLocation);
                mapView.getController().setZoom(18.0);

                Marker startMarker = new Marker(mapView);
                startMarker.setPosition(targetLocation);
                startMarker.setTitle(name);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                mapView.getOverlays().add(startMarker);
                mapView.invalidate();
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}