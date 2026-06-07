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
import androidx.navigation.Navigation;
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
        Button btnRefresh = view.findViewById(R.id.btnRefresh);
        Button btnToMap = view.findViewById(R.id.btnToMap);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(getContext());

        fetchGadgetStores();

        btnRefresh.setOnClickListener(v -> fetchGadgetStores());
        btnToMap.setOnClickListener(v -> {
            try {
                Navigation.findNavController(view).navigate(R.id.action_listFragment_to_mapFragment);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Navigasi ke peta belum siap", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchGadgetStores() {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        apiService.getLocations("toko elektronik Makassar", "json").enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationModel>> call, @NonNull Response<List<LocationModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.saveLocationsAsync(response.body());
                    updateUI(response.body());
                    Toast.makeText(getContext(), "Data toko gadget berhasil dimuat!", Toast.LENGTH_SHORT).show();
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