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
    private static final int DATABASE_VERSION = 2; // Naikkan versi karena perubahan struktur tabel

    public final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Menggunakan AUTOINCREMENT agar ID dibuat otomatis oleh SQLite
        db.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, lat REAL, lng REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS locations");
        onCreate(db);
    }

    public void saveLocationsAsync(List<LocationModel> locations) {
        executorService.execute(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM locations"); // Hapus data lama sebelum simpan data baru
                for (LocationModel loc : locations) {
                    ContentValues values = new ContentValues();
                    // Kita tidak perlu memasukkan ID secara manual karena sudah AUTOINCREMENT
                    values.put("name", loc.getName());
                    values.put("address", loc.getAddress());
                    values.put("lat", loc.getLatitude());
                    values.put("lng", loc.getLongitude());
                    db.insert("locations", null, values);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        });
    }

    public List<LocationModel> getOfflineLocations() {
        List<LocationModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM locations", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new LocationModel(
                        cursor.getInt(0), // ID
                        cursor.getString(1), // Name
                        cursor.getString(2), // Address
                        String.valueOf(cursor.getDouble(3)), // Lat
                        String.valueOf(cursor.getDouble(4))  // Lng
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}