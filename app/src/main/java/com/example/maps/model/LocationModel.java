package com.example.maps.model;

public class LocationModel {
    private int id;
    private String name, address, latitude, longitude;

    // Konstruktor kosong untuk Retrofit
    public LocationModel() {}

    // Konstruktor untuk Data Offline
    public LocationModel(int id, String name, String address, String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // Helper untuk DatabaseHelper (mengubah String ke Double)
    public double getLatitude() {
        try { return Double.parseDouble(latitude); } catch (Exception e) { return 0.0; }
    }
    public double getLongitude() {
        try { return Double.parseDouble(longitude); } catch (Exception e) { return 0.0; }
    }

    // Setter untuk String (agar kompatibel)
    public void setLatitude(String lat) { this.latitude = lat; }
    public void setLongitude(String lng) { this.longitude = lng; }
}