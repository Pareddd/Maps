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
    private static final int DATABASE_VERSION = 1;

    public final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY, name TEXT, address TEXT, lat REAL, lng REAL)");
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
                db.execSQL("DELETE FROM locations");
                for (LocationModel loc : locations) {
                    ContentValues values = new ContentValues();
                    values.put("id", loc.getId());
                    values.put("name", loc.getName());
                    values.put("address", loc.getAddress());
                    values.put("lat", loc.getLatitude());
                    values.put("lng", loc.getLongitude());
                    db.insert("locations", null, values);
                }
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        });
    }

    public List<LocationModel> getOfflineLocations() {
        List<LocationModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM locations", null);

        if (cursor.getCount() == 0) {
            list.add(new LocationModel(1, "Data Offline", "Nyalakan internet dulu", "0.0", "0.0"));
            return list;
        }

        if (cursor.moveToFirst()) {
            do {
                list.add(new LocationModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        String.valueOf(cursor.getDouble(3)),
                        String.valueOf(cursor.getDouble(4))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}