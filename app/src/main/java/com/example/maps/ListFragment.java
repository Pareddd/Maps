package com.example.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maps.api.ApiClient;
import com.example.maps.api.ApiService;
import com.example.maps.db.DatabaseHelper;
import com.example.maps.model.LocationModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFragment extends Fragment {
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        Button btnRefresh = view.findViewById(R.id.btnRefresh); // Hanya tersisa tombol refresh

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(getContext());

        fetchEkspedisi();

        btnRefresh.setOnClickListener(v -> fetchEkspedisi());

        return view;
    }

    private void fetchEkspedisi() {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        apiService.getLocations("jasa ekspedisi Makassar", "json").enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationModel>> call, @NonNull Response<List<LocationModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.saveLocationsAsync(response.body());
                    updateUI(response.body());
                    Toast.makeText(getContext(), "Data agen ekspedisi berhasil dimuat!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationModel>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Koneksi gagal. Memuat data offline.", Toast.LENGTH_SHORT).show();
                updateUI(dbHelper.getOfflineLocations());
            }
        });
    }

    private void updateUI(List<LocationModel> locations) {
        recyclerView.setAdapter(new LocationAdapter(locations, getContext()));
    }
}