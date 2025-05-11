package com.example.iot_lab4_20190057.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.weatherapi.com/v1/";
    private static RetrofitClient instance;
    private Retrofit retrofit;

    private RetrofitClient() {
        // Configurar OkHttpClient con timeout más largo
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Timeout de conexión
                .readTimeout(30, TimeUnit.SECONDS)    // Timeout de lectura
                .writeTimeout(30, TimeUnit.SECONDS)   // Timeout de escritura
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // Agregar el cliente personalizado
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public WeatherApi getWeatherApi() {
        return retrofit.create(WeatherApi.class);
    }
}