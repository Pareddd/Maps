package com.example.maps.model;

import com.google.gson.annotations.SerializedName;

public class LocationModel {
    @SerializedName("place_id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("display_name")
    private String address;

    @SerializedName("lat")
    private String latitude;

    @SerializedName("lon")
    private String longitude;

    public LocationModel(long id, String name, String address, String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() { return id; }

    public String getName() {
        if (name == null || name.isEmpty()) {
            return address != null ? address.split(",")[0] : "Lokasi Tidak Diketahui";
        }
        return name;
    }

    public String getAddress() { return address; }

    public double getLatitude() {
        try { return Double.parseDouble(latitude); } catch (Exception e) { return 0.0; }
    }

    public double getLongitude() {
        try { return Double.parseDouble(longitude); } catch (Exception e) { return 0.0; }
    }
}