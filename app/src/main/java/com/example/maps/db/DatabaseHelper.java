package com.example.maps.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.maps.model.LocationModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NavDB";
    private static final int DATABASE_VERSION = 6; // Naik ke 6 agar otomatis reset database

    public final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, lat REAL, lng REAL)");
        db.execSQL("CREATE TABLE reviews (id INTEGER PRIMARY KEY AUTOINCREMENT, location_name TEXT, rating REAL, comment TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS locations");
        db.execSQL("DROP TABLE IF EXISTS reviews");
        onCreate(db);
    }

    // --- FUNGSI LOKASI ---
    public void saveLocationsAsync(List<LocationModel> locations) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM locations");
                for (LocationModel loc : locations) {
                    ContentValues values = new ContentValues();
                    values.put("name", loc.getName());
                    values.put("address", loc.getAddress());
                    values.put("lat", loc.getLatitude());
                    values.put("lng", loc.getLongitude());
                    db.insert("locations", null, values);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) { e.printStackTrace(); } finally { db.endTransaction(); }
        });
    }

    public List<LocationModel> getOfflineLocations() {
        List<LocationModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM locations", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new LocationModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), String.valueOf(cursor.getDouble(3)), String.valueOf(cursor.getDouble(4))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // --- FUNGSI RATING & KOMENTAR ---
    public void addReview(String locationName, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("location_name", locationName);
        values.put("rating", rating);
        values.put("comment", comment);
        db.insert("reviews", null, values);
        db.close();
    }

    public static class ReviewData {
        public float averageRating = 0f;
        public int totalReviews = 0;
        public String latestComment = "Belum ada ulasan.";
    }

    public ReviewData getReviewStats(String locationName) {
        ReviewData data = new ReviewData();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT rating, comment FROM reviews WHERE location_name = ? ORDER BY id DESC", new String[]{locationName});
        if (cursor.moveToFirst()) {
            data.totalReviews = cursor.getCount();
            data.latestComment = "\"" + cursor.getString(1) + "\"";
            float totalRating = 0;
            do { totalRating += cursor.getFloat(0); } while (cursor.moveToNext());
            data.averageRating = totalRating / data.totalReviews;
        }
        cursor.close();
        return data;
    }

    public static class ReviewItem {
        public float rating;
        public String comment;
        public ReviewItem(float rating, String comment) {
            this.rating = rating;
            this.comment = comment;
        }
    }

    public List<ReviewItem> getAllReviews(String locationName) {
        List<ReviewItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT rating, comment FROM reviews WHERE location_name = ? ORDER BY id DESC", new String[]{locationName});
        if (cursor.moveToFirst()) {
            do {
                list.add(new ReviewItem(cursor.getFloat(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // =========================================================
    // SUNTIKAN DATA DUMMY UNTUK SEMUA LOKASI
    // =========================================================
    public void insertDummyReviewsIfEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(*) FROM reviews", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            // --- J&T EXPRESS ---
            addReview("J&T Cargo Perintis", 4.5f, "Mantap, paket berat aman jaya di sini.");
            addReview("J&T Cargo Perintis", 5.0f, "Sering pakai untuk kirim motor, adminnya solutif.");
            addReview("J&T Cargo Perintis", 3.0f, "Harganya lumayan, tapi sampainya sesuai estimasi.");

            addReview("J&T Express MDP Kapasa Raya Permai", 2.5f, "Susah dicari lokasinya kodong, nyempil.");
            addReview("J&T Express MDP Kapasa Raya Permai", 4.0f, "Kurirnya ramah banget tawwa.");

            addReview("J&T Express Telkomas", 5.0f, "Mantap mentong, sehari langsung sampai tujuan!");
            addReview("J&T Express Telkomas", 4.5f, "Kurirnya selalu konfirmasi via WA.");
            addReview("J&T Express Telkomas", 5.0f, "Paket aman tanpa lecet sedikitpun.");
            addReview("J&T Express Telkomas", 3.0f, "Standar lah, pengiriman sesuai estimasi.");
            addReview("J&T Express Telkomas", 1.0f, "Kurirnya malas masuk lorong, disuruh ambil di jalan raya.");
            addReview("J&T Express Telkomas", 4.0f, "Adminnya lumayan responsif.");

            addReview("J&T Pajjaiang", 1.0f, "Paket saya nyasar ke rumah mantan! Parah banget.");
            addReview("J&T Pajjaiang", 2.0f, "Kardus penyok pas sampai, untung isinya aman.");

            addReview("J&T Express Paccerakkang", 4.5f, "Drop point langganan, pelayanannya sat-set.");
            addReview("J&T Express Paccerakkang", 4.0f, "Cepat proses scannya.");
            addReview("J&T Express Paccerakkang", 5.0f, "Luar biasa cepat.");

            addReview("J&T Express cabang btp", 3.5f, "Rame terus kalau sore, harus antre agak lama.");
            addReview("J&T Express cabang btp", 4.0f, "Lumayan dekat dari kampus Unhas.");

            addReview("J&T & LION PARCEL ANTANG", 5.0f, "Bisa pilih ekspedisi, praktis.");
            addReview("J&T & LION PARCEL ANTANG", 4.0f, "Parkir luas, enak bawa barang besar.");
            addReview("J&T & LION PARCEL ANTANG", 4.5f, "Karyawannya murah senyum.");

            addReview("JNT UPG039A Antang", 2.0f, "Terkadang lambat update resi.");
            addReview("JNT UPG039A Antang", 3.0f, "Paket ketahan 2 hari di sini, kenapa ya?");

            addReview("J&T express upg 04A", 4.0f, "Sip, pelayanan memuaskan.");
            addReview("J&T express upg 04A", 5.0f, "Cepat sampainya, tidak ada kendala.");

            addReview("JNT drop point masale", 5.0f, "Andalan warga Panakkukang!");
            addReview("JNT drop point masale", 4.5f, "Selalu buka tepat waktu.");

            addReview("J&T Express Pengayoman", 3.5f, "Lalu lintas depan agen macet parah.");
            addReview("J&T Express Pengayoman", 5.0f, "Aman, paket dokumen sampai tanpa lecek.");

            addReview("J&T Express Toddopuli Raya", 4.0f, "Bagus, sering promo ongkir.");
            addReview("J&T Express Toddopuli Raya", 1.5f, "Masa paketku ditaruh di atas pagar rumah, kehujanan kodong.");

            addReview("J&T Express Pettarani", 4.0f, "Parkirannya agak susah karena di jalan utama.");
            addReview("J&T Express Pettarani", 5.0f, "Sangat strategis, kurir gesit.");
            addReview("J&T Express Pettarani", 3.5f, "Standar pelayanan J&T.");

            addReview("J&T Express Drop Point VIP", 5.0f, "Pelayanan sesuai namanya, VIP.");
            addReview("J&T Express Drop Point VIP", 4.5f, "Amanah, barang pecah belah dikasih bubble wrap tebal.");

            // --- JNE EXPRESS ---
            addReview("JNE Tamalanrea", 5.0f, "Selalu andalan kalau mau kirim dokumen penting.");
            addReview("JNE Tamalanrea", 4.0f, "Buka sampai malam, aman buat yang sibuk kerja.");
            addReview("JNE Tamalanrea", 4.5f, "Packing kayunya juara, sangat rapi.");
            addReview("JNE Tamalanrea", 5.0f, "Cepat prosesnya, nggak antre lama.");
            addReview("JNE Tamalanrea", 3.5f, "Harganya lumayan mahal dibanding yang lain.");
            addReview("JNE Tamalanrea", 5.0f, "Kurirnya hafal rumah saya. Top banget!");
            addReview("JNE Tamalanrea", 4.0f, "Amanah selalu, belum pernah kecewa.");

            addReview("JNE Agen Abdesir", 4.0f, "Gampang ditemukan di Maps.");
            addReview("JNE Agen Abdesir", 3.0f, "Cukup baik pelayanannya.");

            addReview("JNE Bukit Baruga", 5.0f, "Warga Baruga pasti ke sini, dekat dan cepat.");
            addReview("JNE Bukit Baruga", 4.5f, "Kurir area sini ramah-ramah.");
            addReview("JNE Bukit Baruga", 4.0f, "Mantap, paket selalu sampai sebelum estimasi.");

            addReview("JNE Antang", 4.5f, "Aman banget packingnya, langganan tetap.");
            addReview("JNE Antang", 2.5f, "Pernah salah kirim, tapi untung diganti.");

            addReview("JNE Hertasning", 5.0f, "Tempatnya bersih dan dingin AC-nya.");
            addReview("JNE Hertasning", 4.5f, "Dekat dari rumah, mantap.");
            addReview("JNE Hertasning", 4.0f, "Sore selalu ramai tapi cepat ditangani.");
            addReview("JNE Hertasning", 5.0f, "Andalan kalau urusan paketan.");

            addReview("JNE Kantor Cabang Makassar", 4.0f, "Kantor cabangnya besar, pelayanan profesional.");
            addReview("JNE Kantor Cabang Makassar", 4.5f, "Gampang claim asuransi paket di sini.");
            addReview("JNE Kantor Cabang Makassar", 3.5f, "Parkiran penuh terus.");

            addReview("JNE Express", 3.0f, "Adminnya agak cuek.");
            addReview("JNE Express", 4.0f, "Paket sampai dengan aman.");

            addReview("JNE Daeng Tata", 5.0f, "Kurir langgananku baik banget, hujan-hujan tetap diantar.");
            addReview("JNE Daeng Tata", 4.5f, "Jarang bermasalah kirim lewat sini.");

            addReview("JNE Urip Sumoharjo", 4.0f, "Lokasinya gampang dicari, di pinggir jalan raya.");
            addReview("JNE Urip Sumoharjo", 3.0f, "Standar.");

            addReview("JNE Rappokalling", 2.0f, "Lama proses sortirnya.");
            addReview("JNE Rappokalling", 4.0f, "Bagus untuk kiriman reguler.");

            // --- SICEPAT & SPX EXPRESS ---
            addReview("SiCepat Express Makassar", 4.0f, "Sesuai namanya, memang si cepat.");
            addReview("SiCepat Express Makassar", 5.0f, "Sering promo gratis ongkir di e-commerce.");
            addReview("SiCepat Express Makassar", 1.0f, "Adminnya jutek, disuruh cek resi sendiri.");
            addReview("SiCepat Express Makassar", 3.0f, "Lokasi susah dicari.");
            addReview("SiCepat Express Makassar", 4.5f, "CS-nya cukup membantu.");

            addReview("SPX Express Biringkanaya HUB", 2.5f, "Gudangnya agak berantakan.");
            addReview("SPX Express Biringkanaya HUB", 4.0f, "Kurir SPX lumayan cepat kalau area Kima.");
            addReview("SPX Express Biringkanaya HUB", 3.5f, "Sering nyangkut lama di HUB ini.");

            addReview("SPX Express Bangkala", 1.5f, "Kurirnya galak, saya dimarahin karena lama buka pagar.");
            addReview("SPX Express Bangkala", 3.0f, "Agak lama sampainya dibanding ekspedisi lain.");
            addReview("SPX Express Bangkala", 5.0f, "Wah malah punyaku cepat sampainya, langganan!");

            addReview("SPX Express Makassar 2 HUB", 4.0f, "Paket Shopee aman mendarat.");
            addReview("SPX Express Makassar 2 HUB", 3.0f, "Kadang cepat kadang lambat, gacha.");

            addReview("SPX Express Wajo HUB", 4.5f, "Hubungannya langsung ke pelabuhan, cepat.");
            addReview("SPX Express Wajo HUB", 4.0f, "Kurirnya gesit tawwa.");
        }
    }
}