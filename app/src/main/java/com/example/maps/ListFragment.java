package com.example.maps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maps.api.ApiClient;
import com.example.maps.api.ApiService;
import com.example.maps.db.DatabaseHelper;
import com.example.maps.model.LocationModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFragment extends Fragment {
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private EditText etSearch;

    private LocationAdapter adapter;
    private List<LocationModel> masterDataList = new ArrayList<>(); // Menyimpan semua data asli

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        Button btnRefresh = view.findViewById(R.id.btnRefresh);
        etSearch = view.findViewById(R.id.etSearch); // Hubungkan kotak pencarian

        // =========================================================
        // INISIALISASI SWITCH TEMA GELAP
        // =========================================================
        SwitchMaterial switchTheme = view.findViewById(R.id.switchTheme);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(getContext());

        // Ambil status tema terakhir yang disimpan di SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("IsDarkMode", false);

        // Sesuaikan posisi switch dengan tema aktif
        switchTheme.setChecked(isDarkMode);

        // Aksi ketika pengguna menggeser Switch
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("IsDarkMode", isChecked);
            editor.apply(); // Simpan preferensi secara lokal

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        fetchEkspedisi();

        btnRefresh.setOnClickListener(v -> {
            etSearch.setText(""); // Kosongkan pencarian saat refresh
            fetchEkspedisi();
        });

        // =========================================================
        // FITUR MENDETEKSI KETIKAN DI KOTAK PENCARIAN
        // =========================================================
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Memanggil fungsi filter setiap kali huruf diketik
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    // Fungsi pemilah data berdasarkan kata kunci
    private void filterData(String keyword) {
        List<LocationModel> filteredList = new ArrayList<>();

        for (LocationModel loc : masterDataList) {
            String name = loc.getName() != null ? loc.getName().toLowerCase() : "";
            String address = loc.getAddress() != null ? loc.getAddress().toLowerCase() : "";

            // Cek apakah kata kunci ada di nama agen ATAU di alamatnya
            if (name.contains(keyword.toLowerCase()) || address.contains(keyword.toLowerCase())) {
                filteredList.add(loc);
            }
        }

        // Kirim data yang sudah disaring ke layar
        if (adapter != null) {
            adapter.setFilter(filteredList);
        }
    }

    private List<LocationModel> getLocalCouriers() {
        List<LocationModel> localData = new ArrayList<>();

        LocationModel jnt = new LocationModel();
        jnt.setName("J&T Express - Tamalanrea");
        jnt.setAddress("Jl. Perintis Kemerdekaan KM. 10, Tamalanrea, Makassar");
        jnt.setLatitude("-5.1321");
        jnt.setLongitude("119.4855");
        localData.add(jnt);

        LocationModel jne = new LocationModel();
        jne.setName("JNE Cabang Utama Makassar");
        jne.setAddress("Jl. A.P. Pettarani No.3, Masale, Panakkukang, Makassar");
        jne.setLatitude("-5.1502");
        jne.setLongitude("119.4357");
        localData.add(jne);

        LocationModel sicepat = new LocationModel();
        sicepat.setName("SiCepat Ekspres - Alauddin");
        sicepat.setAddress("Jl. Sultan Alauddin No.98, Pa'baeng-baeng, Tamalate");
        sicepat.setLatitude("-5.1751");
        sicepat.setLongitude("119.4208");
        localData.add(sicepat);

        LocationModel spx = new LocationModel();
        spx.setName("Shopee Xpress Hub Makassar");
        spx.setAddress("Jl. Urip Sumoharjo No.20, Tello Baru, Panakkukang");
        spx.setLatitude("-5.1404");
        spx.setLongitude("119.4601");
        localData.add(spx);

        LocationModel anteraja = new LocationModel();
        anteraja.setName("Anteraja Staging Store");
        anteraja.setAddress("Jl. Boulevard Raya, Masale, Panakkukang, Makassar");
        anteraja.setLatitude("-5.1555");
        anteraja.setLongitude("119.4452");
        localData.add(anteraja);

        return localData;
    }

    private void fetchEkspedisi() {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        apiService.getLocations("jasa ekspedisi Makassar", "json").enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationModel>> call, @NonNull Response<List<LocationModel>> response) {
                List<LocationModel> combinedData = getLocalCouriers();

                if (response.isSuccessful() && response.body() != null) {
                    combinedData.addAll(response.body());
                    dbHelper.saveLocationsAsync(response.body());
                }

                updateUI(combinedData);

                // MENCEGAH CRASH: Pastikan Context tidak null sebelum memanggil Toast
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Data agen ekspedisi berhasil dimuat!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationModel>> call, @NonNull Throwable t) {
                // MENCEGAH CRASH: Pastikan Context tidak null sebelum memanggil Toast
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Koneksi gagal. Memuat data offline.", Toast.LENGTH_SHORT).show();
                }

                List<LocationModel> fallbackData = getLocalCouriers();
                fallbackData.addAll(dbHelper.getOfflineLocations());
                updateUI(fallbackData);
            }
        });
    }

    private void updateUI(List<LocationModel> locations) {
        // Simpan data ke master list agar pencarian tidak merusak data asli
        masterDataList.clear();
        masterDataList.addAll(locations);

        // Buat salinan data untuk ditampilkan pertama kali
        List<LocationModel> displayList = new ArrayList<>(masterDataList);

        adapter = new LocationAdapter(displayList, getContext());
        recyclerView.setAdapter(adapter);
    }
}