package com.example.iot_lab4_20190057.api;

import com.example.iot_lab4_20190057.models.LocationModel;
import com.example.iot_lab4_20190057.models.ForecastResponse;
import com.example.iot_lab4_20190057.models.FutureResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("search.json")
    Call<List<LocationModel>> searchLocations(
            @Query("key") String apiKey,
            @Query("q") String query
    );

    @GET("forecast.json")
    Call<ForecastResponse> getForecast(
            @Query("key") String apiKey,
            @Query("q") String locationId,
            @Query("days") int days
    );

    @GET("future.json")
    Call<FutureResponse> getFutureWeather(
            @Query("key") String apiKey,
            @Query("q") String locationId,
            @Query("dt") String date
    );
}