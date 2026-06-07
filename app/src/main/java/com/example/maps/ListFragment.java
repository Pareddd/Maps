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
import java.util.ArrayList;
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
        Button btnRefresh = view.findViewById(R.id.btnRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(getContext());

        fetchEkspedisi();

        btnRefresh.setOnClickListener(v -> fetchEkspedisi());

        return view;
    }

    // Fungsi untuk membuat data kurir asli (Hardcoded)
    private List<LocationModel> getLocalCouriers() {
        List<LocationModel> localData = new ArrayList<>();

        LocationModel jnt = new LocationModel();
        jnt.setName("J&T Express - Tamalanrea");
        jnt.setAddress("Jl. Perintis Kemerdekaan KM. 10, Tamalanrea, Makassar");
        localData.add(jnt);

        LocationModel jne = new LocationModel();
        jne.setName("JNE Cabang Utama Makassar");
        jne.setAddress("Jl. A.P. Pettarani No.3, Masale, Panakkukang, Makassar");
        localData.add(jne);

        LocationModel sicepat = new LocationModel();
        sicepat.setName("SiCepat Ekspres - Alauddin");
        sicepat.setAddress("Jl. Sultan Alauddin No.98, Pa'baeng-baeng, Tamalate");
        localData.add(sicepat);

        LocationModel spx = new LocationModel();
        spx.setName("Shopee Xpress Hub Makassar");
        spx.setAddress("Jl. Urip Sumoharjo No.20, Tello Baru, Panakkukang");
        localData.add(spx);

        LocationModel anteraja = new LocationModel();
        anteraja.setName("Anteraja Staging Store");
        anteraja.setAddress("Jl. Boulevard Raya, Masale, Panakkukang, Makassar");
        localData.add(anteraja);

        return localData;
    }

    private void fetchEkspedisi() {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        apiService.getLocations("jasa ekspedisi Makassar", "json").enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationModel>> call, @NonNull Response<List<LocationModel>> response) {
                // Siapkan data kurir asli di urutan teratas
                List<LocationModel> combinedData = getLocalCouriers();

                // Gabungkan dengan data dari API OpenStreetMap
                if (response.isSuccessful() && response.body() != null) {
                    combinedData.addAll(response.body());
                    dbHelper.saveLocationsAsync(response.body());
                }

                updateUI(combinedData);
                Toast.makeText(getContext(), "Data agen ekspedisi berhasil dimuat!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationModel>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Koneksi gagal. Memuat data offline.", Toast.LENGTH_SHORT).show();

                // Jika gagal API, tetap tampilkan kurir asli ditambah data offline dari database
                List<LocationModel> fallbackData = getLocalCouriers();
                fallbackData.addAll(dbHelper.getOfflineLocations());
                updateUI(fallbackData);
            }
        });
    }

    private void updateUI(List<LocationModel> locations) {
        recyclerView.setAdapter(new LocationAdapter(locations, getContext()));
    }
}