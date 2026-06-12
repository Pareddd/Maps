package com.example.maps;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maps.db.DatabaseHelper;
import com.example.maps.model.LocationModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final List<LocationModel> locationList;
    private final Context context;
    private final DatabaseHelper dbHelper;

    public LocationAdapter(List<LocationModel> locationList, Context context) {
        this.locationList = locationList;
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.dbHelper.insertDummyReviewsIfEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    private int getLogoResource(String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("j&t") || lowerName.contains("jnt")) {
            return R.drawable.logo_jnt;
        } else if (lowerName.contains("jne")) {
            return R.drawable.logo_jne;
        } else if (lowerName.contains("sicepat")) {
            return R.drawable.logo_sicepat;
        } else if (lowerName.contains("spx") || lowerName.contains("shopee")) {
            return R.drawable.logo_spx;
        } else {
            return android.R.drawable.ic_menu_send;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationModel location = locationList.get(position);
        String name = location.getName() != null ? location.getName() : "Agen Ekspedisi";
        String address = location.getAddress() != null ? location.getAddress() : "Melayani area sekitar.";

        holder.tvName.setText(name);
        holder.tvAddress.setText(address);

        DatabaseHelper.ReviewData reviewData = dbHelper.getReviewStats(name);
        holder.tvRatingAngka.setText(String.format(Locale.getDefault(), "%.1f", reviewData.averageRating));
        holder.ratingBar.setRating(reviewData.averageRating);
        holder.tvJumlahUlasan.setText("(" + reviewData.totalReviews + " ulasan)");
        holder.tvKomentar.setText(reviewData.latestComment);

        holder.ivIcon.setImageResource(getLogoResource(name));

        holder.btnCekOngkir.setOnClickListener(v -> showCekOngkirDialog(name));
        holder.itemView.setOnClickListener(v -> showReviewPageDialog(location, name, address, position, v));
        holder.itemView.setOnLongClickListener(v -> {
            showAddReviewDialog(name, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return locationList != null ? locationList.size() : 0;
    }

    public void setFilter(List<LocationModel> filterList) {
        this.locationList.clear();
        this.locationList.addAll(filterList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvRatingAngka, tvJumlahUlasan, tvKomentar;
        ImageView ivIcon;
        Button btnCekOngkir;
        CardView cardIconBg;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRatingAngka = itemView.findViewById(R.id.tvRatingAngka);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvJumlahUlasan = itemView.findViewById(R.id.tvJumlahUlasan);
            tvKomentar = itemView.findViewById(R.id.tvKomentar);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            cardIconBg = itemView.findViewById(R.id.cardIconBg);
            btnCekOngkir = itemView.findViewById(R.id.btnCekOngkir);
        }
    }

    private void showReviewPageDialog(LocationModel location, String name, String address, int position, View rootView) {
        // MENGGUNAKAN TEMA BAWAAN APLIKASI (R.style.Theme_Maps) AGAR DARK MODE BERFUNGSI!
        final android.app.Dialog dialog = new android.app.Dialog(context, R.style.Theme_Maps);
        dialog.setContentView(R.layout.dialog_review_detail);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.parseColor("#1A237E"));
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDetailTitle);
        TextView tvAddress = dialog.findViewById(R.id.tvDetailAddress);
        ImageView ivDetailLogo = dialog.findViewById(R.id.ivDetailLogo);
        LinearLayout containerKomentar = dialog.findViewById(R.id.containerKomentar);

        tvTitle.setText(name);
        tvAddress.setText(address);
        ivDetailLogo.setImageResource(getLogoResource(name));

        Runnable loadReviewsRunnable = new Runnable() {
            @Override
            public void run() {
                containerKomentar.removeAllViews();
                List<DatabaseHelper.ReviewItem> reviewList = dbHelper.getAllReviews(name);
                LayoutInflater inflater = LayoutInflater.from(context);
                for (DatabaseHelper.ReviewItem item : reviewList) {
                    View lineView = inflater.inflate(R.layout.item_review_line, containerKomentar, false);

                    ((RatingBar) lineView.findViewById(R.id.lineRatingBar)).setRating(item.rating);
                    ((TextView) lineView.findViewById(R.id.lineTeksKomentar)).setText(item.comment);

                    TextView tvAngka = lineView.findViewById(R.id.lineRatingAngka);
                    if (tvAngka != null) {
                        tvAngka.setText(String.valueOf(item.rating));
                    }

                    containerKomentar.addView(lineView);
                }
            }
        };

        loadReviewsRunnable.run();
        dialog.findViewById(R.id.btnBack).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnTulisKomentarBaru).setOnClickListener(v -> {
            showAddReviewDialog(name, position);
            containerKomentar.postDelayed(loadReviewsRunnable, 500);
        });
        dialog.findViewById(R.id.btnLihatPetaDetail).setOnClickListener(v -> {
            dialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putDouble("lat", location.getLatitude());
            bundle.putDouble("lng", location.getLongitude());
            bundle.putString("name", name);
            Navigation.findNavController(rootView).navigate(R.id.mapFragment, bundle);
        });
        dialog.show();
    }

    private void showAddReviewDialog(String locationName, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Tambah Ulasan");

        final RatingBar rb = new RatingBar(context);
        rb.setNumStars(5);
        rb.setStepSize(0.5f);
        rb.setRating(5.0f);

        final EditText et = new EditText(context);
        et.setHint("Tulis pengalamanmu di sini...");

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(50, 20, 50, 20);
        ll.addView(rb); ll.addView(et);
        builder.setView(ll);

        builder.setPositiveButton("Kirim", null);
        builder.setNegativeButton("Batal", (d, w) -> d.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String komentar = et.getText().toString().trim();
            if (komentar.isEmpty()) {
                et.setError("Komentar tidak boleh kosong!");
            } else {
                dbHelper.addReview(locationName, rb.getRating(), komentar);
                notifyItemChanged(position);
                Toast.makeText(context, "Terima kasih atas ulasanmu!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void showCekOngkirDialog(String agenName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Estimasi Tarif Pengiriman");
        builder.setMessage("Agen: " + agenName + "\nAsal: Makassar");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 20);

        final EditText etTujuan = new EditText(context);
        etTujuan.setHint("Kota Tujuan (Misal: Aceh, Palu)");
        layout.addView(etTujuan);

        final EditText etBerat = new EditText(context);
        etBerat.setHint("Berat Paket (Kg)");
        etBerat.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etBerat);

        final TextView tvHasil = new TextView(context);
        tvHasil.setTextSize(15f);
        tvHasil.setPadding(0, 40, 0, 0);

        // SAYA HAPUS KODE WARNA DKGRAY DI SINI AGAR TEKSNYA BISA JADI PUTIH SAAT DARK MODE

        layout.addView(tvHasil);

        builder.setView(layout);

        builder.setPositiveButton("Hitung Tarif", null);
        builder.setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String tujuan = etTujuan.getText().toString().trim().toLowerCase();
            String beratStr = etBerat.getText().toString().trim();

            if (tujuan.isEmpty() || beratStr.isEmpty()) {
                Toast.makeText(context, "Harap isi Kota Tujuan dan Berat Paket!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float berat = Float.parseFloat(beratStr);
                int hargaDasarAgen = 18000;

                String lowerName = agenName.toLowerCase();
                if (lowerName.contains("jne")) {
                    hargaDasarAgen = 20000;
                } else if (lowerName.contains("j&t") || lowerName.contains("jnt")) {
                    hargaDasarAgen = 19000;
                } else if (lowerName.contains("sicepat")) {
                    hargaDasarAgen = 18000;
                } else if (lowerName.contains("spx") || lowerName.contains("shopee")) {
                    hargaDasarAgen = 16000;
                }

                double multiplierZona = 1.2;
                String namaZona = "Regional Sulawesi";

                if (tujuan.contains("makassar") || tujuan.contains("gowa") || tujuan.contains("maros")) {
                    hargaDasarAgen = 10000;
                    multiplierZona = 1.0;
                    namaZona = "Lokal (Sulsel)";
                } else if (tujuan.contains("jakarta") || tujuan.contains("jawa") || tujuan.contains("bandung") || tujuan.contains("surabaya") || tujuan.contains("jogja")) {
                    multiplierZona = 2.0;
                    namaZona = "Pulau Jawa";
                } else if (tujuan.contains("sumatera") || tujuan.contains("medan") || tujuan.contains("aceh") || tujuan.contains("padang") || tujuan.contains("palembang") || tujuan.contains("riau")) {
                    multiplierZona = 3.5;
                    namaZona = "Pulau Sumatera";
                } else if (tujuan.contains("papua") || tujuan.contains("maluku") || tujuan.contains("jayapura") || tujuan.contains("ambon")) {
                    multiplierZona = 5.0;
                    namaZona = "Indonesia Timur";
                } else if (tujuan.contains("kalimantan") || tujuan.contains("balikpapan") || tujuan.contains("samarinda") || tujuan.contains("banjarmasin") || tujuan.contains("pontianak")) {
                    multiplierZona = 2.5;
                    namaZona = "Pulau Kalimantan";
                } else if (tujuan.contains("palu") || tujuan.contains("kendari") || tujuan.contains("manado") || tujuan.contains("gorontalo")) {
                    multiplierZona = 1.5;
                    namaZona = "Sulawesi (Antar Provinsi)";
                }

                int hargaPerKg = (int) (hargaDasarAgen * multiplierZona);
                int totalOngkir = (int) (berat * hargaPerKg);

                NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("id", "ID"));
                String strHargaKg = formatRupiah.format(hargaPerKg);
                String strTotal = formatRupiah.format(totalOngkir);

                String hasil = "📍 Tujuan: " + tujuan.toUpperCase() + " (" + namaZona + ")\n" +
                        "📦 Berat: " + berat + " Kg\n" +
                        "💸 Tarif /Kg: Rp " + strHargaKg + "\n" +
                        "----------------------------------------\n" +
                        "🔥 Total Estimasi: Rp " + strTotal;

                tvHasil.setText(hasil);

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Format berat tidak valid!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}