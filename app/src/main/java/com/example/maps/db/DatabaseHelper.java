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
    private static final int DATABASE_VERSION = 3;

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

    // INI YANG TADI DIBUTUHKAN DI ADAPTER
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

    public void insertDummyReviewsIfEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(*) FROM reviews", null);
        cursor.moveToFirst();
        if (cursor.getInt(0) == 0) {
            addReview("J&T Pajjaiang", 1.0f, "Paket saya nyasar ke rumah mantan!");
            addReview("J&T Express Telkomas", 5.0f, "Wah gila sih ini, sehari langsung sampai!");
        }
        cursor.close();
    }
}