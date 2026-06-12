package com.example.maps;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        String lowerName = name.toLowerCase();
        if (lowerName.contains("j&t") || lowerName.contains("jnt")) {
            holder.ivIcon.setImageResource(R.drawable.logo_jnt);
        } else if (lowerName.contains("jne")) {
            holder.ivIcon.setImageResource(R.drawable.logo_jne);
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_send);
        }

        // =========================================================
        // KLIK TOMBOL HIJAU "CEK ONGKIR"
        // =========================================================
        holder.btnCekOngkir.setOnClickListener(v -> showCekOngkirDialog(name));

        // Klik sembarang tempat di kartu untuk melihat detail ulasan & rute
        holder.itemView.setOnClickListener(v -> showReviewPageDialog(location, name, address, position, v));

        // Tekan tahan kartunya untuk menambah ulasan baru
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
        Button btnCekOngkir; // <--- Sekarang pakai Button, bukan ImageView lagi
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
        final android.app.Dialog dialog = new android.app.Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_review_detail);

        TextView tvTitle = dialog.findViewById(R.id.tvDetailTitle);
        TextView tvAddress = dialog.findViewById(R.id.tvDetailAddress);
        LinearLayout containerKomentar = dialog.findViewById(R.id.containerKomentar);

        tvTitle.setText(name);
        tvAddress.setText(address);

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
        final EditText et = new EditText(context);
        et.setHint("Tulis komentarmu...");
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(50, 20, 50, 20);
        ll.addView(rb); ll.addView(et);
        builder.setView(ll);
        builder.setPositiveButton("Kirim", (d, w) -> {
            dbHelper.addReview(locationName, rb.getRating(), et.getText().toString());
            notifyItemChanged(position);
            Toast.makeText(context, "Ulasan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showCekOngkirDialog(String agenName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Estimasi Ongkir");
        builder.setMessage("Agen: " + agenName);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 20);

        final EditText etTujuan = new EditText(context);
        etTujuan.setHint("Kota Tujuan (Misal: Jakarta)");
        layout.addView(etTujuan);

        final EditText etBerat = new EditText(context);
        etBerat.setHint("Berat Paket (Kg)");
        etBerat.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etBerat);

        final TextView tvHasil = new TextView(context);
        tvHasil.setTextSize(16f);
        tvHasil.setPadding(0, 40, 0, 0);
        tvHasil.setTextColor(android.graphics.Color.BLACK);
        layout.addView(tvHasil);

        builder.setView(layout);

        builder.setPositiveButton("Hitung Tarif", null);
        builder.setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String tujuan = etTujuan.getText().toString().trim();
            String beratStr = etBerat.getText().toString().trim();

            if (tujuan.isEmpty() || beratStr.isEmpty()) {
                Toast.makeText(context, "Harap isi Kota Tujuan dan Berat Paket!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float berat = Float.parseFloat(beratStr);
                int hargaPerKg = 15000;

                String lowerName = agenName.toLowerCase();
                if (lowerName.contains("jne")) {
                    hargaPerKg = 18000;
                } else if (lowerName.contains("j&t") || lowerName.contains("jnt")) {
                    hargaPerKg = 17000;
                } else if (lowerName.contains("sicepat")) {
                    hargaPerKg = 16000;
                } else if (lowerName.contains("spx") || lowerName.contains("shopee")) {
                    hargaPerKg = 14000;
                }

                int totalOngkir = (int) (berat * hargaPerKg);

                String hasil = "📍 Tujuan: " + tujuan + "\n" +
                        "📦 Berat: " + berat + " Kg\n" +
                        "💸 Tarif /Kg: Rp " + hargaPerKg + "\n" +
                        "----------------------------------------\n" +
                        "🔥 Total Estimasi: Rp " + totalOngkir;

                tvHasil.setText(hasil);

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Format berat tidak valid!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}