package com.example.maps.api;

import com.example.maps.model.LocationModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("search")
    Call<List<LocationModel>> getLocations(
            @Query("q") String query,
            @Query("format") String format
    );
}