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
        View btnRefresh = view.findViewById(R.id.btnRefresh);
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

    // =========================================================
    // DATA ALAMAT KONKRIT (LOKAL & FALLBACK DATA)
    // =========================================================
    private List<LocationModel> getLocalCouriers() {
        List<LocationModel> localData = new ArrayList<>();

        // --- KELOMPOK J&T EXPRESS ---
        localData.add(createLoc("J&T Cargo Perintis", "Samping SPBU Perintis, Jl. Perintis Kemerdekaan No.km 10, Tamalanrea Jaya, Kec. Tamalanrea, Kota Makassar", "-5.140435098549362", "119.48995327729112"));
        localData.add(createLoc("J&T Express MDP Kapasa Raya Permai", "Kapasa Raya Permai Blk. G No.01, Kelurahan Kapasa Raya, Kec. Tamalanrea, Kota Makassar", "-5.100382695001246", "119.49687934841029"));
        localData.add(createLoc("J&T Express Telkomas", "Jl. Perintis Kemerdekaan No.4, Daya, Kec. Biringkanaya, Kota Makassar", "-5.12178647661976", "119.50772741447769"));
        localData.add(createLoc("J&T Pajjaiang", "VGQ8+F3J, Jl. Pajjaiang, Paccerakkang, Kec. Biringkanaya, Kota Makassar", "-5.10977061191489", "119.5152290724015"));
        localData.add(createLoc("J&T Express Paccerakkang", "Jl. Paccerakkang, RW.3, Paccerakkang, Kec. Biringkanaya, Kota Makassar", "-5.115727310765139", "119.51898069938918"));
        localData.add(createLoc("J&T Express cabang btp", "Blk. AC Jl. Keindahan No.53, Paccerakkang, Kec. Biringkanaya, Kota Makassar", "-5.1324880775788975", "119.51857658995205"));
        localData.add(createLoc("J&T & LION PARCEL ANTANG", "Jl. Dr. J Leimena No.113, Tello Baru, Kec. Panakkukang, Kota Makassar", "-5.151974464103622", "119.47873700100037"));
        localData.add(createLoc("JNT UPG039A Antang", "RFRR+6VQ, Jl. Nipa-Nipa, Antang, Kec. Manggala, Kota Makassar", "-5.15752701077795", "119.49232294922466"));
        localData.add(createLoc("J&T express upg 04A", "RFW5+HMF, Jl. Batua Raya No.51, Batua, Kec. Manggala, Kota Makassar", "-5.150761913489756", "119.45927638024929"));
        localData.add(createLoc("JNT drop point masale", "RCWX+WCW, Jl. Abdullah Daeng Sirua, Pandang, Kec. Panakkukang, Kota Makassar", "-5.150863502197091", "119.44832715174928"));
        localData.add(createLoc("J&T Express Pengayoman", "Komp. Akik Hijau, Jl. Pengayoman No.10 Blok E, Pandang, Kec. Panakkukang, Kota Makassar", "-5.1586375386349586", "119.44934161836242"));
        localData.add(createLoc("J&T Express Toddopuli Raya", "Jl. Toddopuli Raya No.15, Pandang, Kec. Panakkukang, Kota Makassar", "-5.163483746429437", "119.45066007062111"));
        localData.add(createLoc("J&T Express Pettarani", "RCWQ+R2H, Jl. A. P. Pettarani, Masale, Kec. Panakkukang, Kota Makassar", "-5.150258092048712", "119.43869674168948"));
        localData.add(createLoc("J&T Express Drop Point VIP", "VC9F+78V, Jl. Lobak, Wajo Baru, Kec. Bontoala, Kota Makassar", "-5.1305712162434745", "119.4225784114322"));

        // --- KELOMPOK JNE EXPRESS ---
        localData.add(createLoc("JNE Tamalanrea", "Jl. Perintis Kemerdekaan No.Km.11 No.245, Tamalanrea, Kec. Tamalanrea, Kota Makassar", "-5.131345121519572", "119.49748427464235"));
        localData.add(createLoc("JNE Agen Abdesir", "Bara-Baraya Timur, Jl. Abdullah Daeng Sirua No.440, Batua, Kec. Manggala, Kota Makassar", "-5.147193365128173", "119.46771566693518"));
        localData.add(createLoc("JNE Bukit Baruga", "Jl. Raya Baruga No.Raya 61, Antang, Manggala, Kota Makassar", "-5.1539658846231875", "119.4818308913793"));
        localData.add(createLoc("JNE Antang", "Jl. Antang Raya No.47, Antang, Kec. Manggala, Kota Makassar", "-5.1574165179029965", "119.47669842207324"));
        localData.add(createLoc("JNE Hertasning", "Jalan Hertasning baru, Kassi-Kassi, Kec. Rappocini, Kota Makassar", "-5.166233458999299", "119.44962830041801"));
        localData.add(createLoc("JNE Kantor Cabang Makassar", "Jl. Yusuf Daeng Ngawing No.6, Tidung, Kec. Rappocini, Kota Makassar", "-5.1643168271838835", "119.44026049824535"));
        localData.add(createLoc("JNE Express", "RCMH+J42, Jl. Andi Djemma, Banta-Bantaeng, Kec. Rappocini, Kota Makassar", "-5.16316700445746", "119.42819860547858"));
        localData.add(createLoc("JNE Daeng Tata", "Jl. Daeng Tata Raya No.10, Parang Tambung, Kec. Tamalate, Kota Makassar", "-5.1801638152557405", "119.41934584836339"));
        localData.add(createLoc("JNE Urip Sumoharjo", "Jl. Urip Sumoharjo Jl. Maccini Raya No.73B, Malimongan Baru, Kec. Bontoala, Kota Makassar", "-5.131219468672117", "119.42922355671814"));
        localData.add(createLoc("JNE Rappokalling", "Jl. Rappokalling Raya No.20b, Rappokalling, Kec. Tallo, Kota Makassar", "-5.125979475638908", "119.43743515132157"));

        // --- KELOMPOK SICEPAT & SPX EXPRESS ---
        localData.add(createLoc("SiCepat Express Makassar", "Jl. Perintis Kemerdekaan Keluaran No.KM 14, Daya, Kec. Biringkanaya, Kota Makassar", "-5.109437834502319", "119.51191226704387"));
        localData.add(createLoc("SPX Express Biringkanaya HUB", "Jl. Kima XVI, Daya, Kec. Biringkanaya, Kota Makassar", "-5.095675104514196", "119.5014393346636"));
        localData.add(createLoc("SPX Express Bangkala", "Bangkala, Kec. Manggala, Kota Makassar", "-5.172444148973032", "119.48084455304834"));
        localData.add(createLoc("SPX Express Makassar 2 HUB", "VC6H+V2M, Maccini Gusung, Kec. Makassar, Kota Makassar", "-5.133291584895679", "119.4269394232379"));
        localData.add(createLoc("SPX Express Wajo HUB", "Jl. Tentara Pelajar Kel No.Blok 2A, RT.000/RW.000, Butung, Kec. Wajo, Kota Makassar", "-5.118588813743252", "119.40942920028239"));

        return localData;
    }

    // Fungsi Pembantu Otomatisasi Instansiasi Objek Lokasi
    private LocationModel createLoc(String name, String address, String lat, String lng) {
        LocationModel model = new LocationModel();
        model.setName(name);
        model.setAddress(address);
        model.setLatitude(lat);
        model.setLongitude(lng);
        return model;
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