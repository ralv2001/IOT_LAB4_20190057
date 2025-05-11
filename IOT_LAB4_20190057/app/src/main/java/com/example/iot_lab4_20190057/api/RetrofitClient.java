package com.example.iot_lab4_20190057.api;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//USO DE CLAUDE (INTELEGENCIA ARTIFICIAL) PARA EL AÑADIDO DE LOGS DE DEBUGS
public class RetrofitClient {
    private static final String BASE_URL = "https://api.weatherapi.com/v1/";
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private static final String TAG = "RetrofitClient";

    private RetrofitClient() {
        // Interceptor para logging detallado
        Interceptor loggingInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                long t1 = System.nanoTime();

                // Log de la request
                String method = request.method();
                HttpUrl url = request.url();
                RequestBody requestBody = request.body();

                Log.d(TAG, String.format("---> REQUEST %s %s", method, url));

                // Log headers de request
                Log.d(TAG, "Request Headers:");
                for (String name : request.headers().names()) {
                    // Ocultar API key en logs
                    if (name.toLowerCase().equals("authorization") || name.toLowerCase().equals("x-api-key")) {
                        Log.d(TAG, name + ": [HIDDEN]");
                    } else {
                        Log.d(TAG, name + ": " + request.headers().get(name));
                    }
                }

                // Log parameters de URL
                Log.d(TAG, "URL Parameters:");
                for (String paramName : url.queryParameterNames()) {
                    String value = url.queryParameter(paramName);
                    // Ocultar API key en logs
                    if (paramName.toLowerCase().equals("key") || paramName.toLowerCase().equals("apikey")) {
                        Log.d(TAG, paramName + ": [HIDDEN]");
                    } else {
                        Log.d(TAG, paramName + ": " + value);
                    }
                }

                // Log body de request si existe
                if (requestBody != null) {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);
                    Charset charset = Charset.forName("UTF-8");
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(Charset.forName("UTF-8"));
                    }
                    Log.d(TAG, "Request Body: " + buffer.readString(charset));
                }

                // Hacer la request
                Response response = chain.proceed(request);

                long t2 = System.nanoTime();
                double durationMs = (t2 - t1) / 1e6d;

                // Log de la response
                Log.d(TAG, String.format("<--- RESPONSE %s %s (%.1fms)",
                        response.code(), response.request().url(), durationMs));

                // Log headers de response
                Log.d(TAG, "Response Headers:");
                for (String name : response.headers().names()) {
                    Log.d(TAG, name + ": " + response.headers().get(name));
                }

                // Log body de response
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String responseBodyString = responseBody.string();

                    // Log del response body (solo los primeros 1000 caracteres si es muy largo)
                    if (responseBodyString.length() > 1000) {
                        Log.d(TAG, "Response Body (truncated): " + responseBodyString.substring(0, 1000) + "...");
                        Log.d(TAG, "Full Response Body Length: " + responseBodyString.length() + " characters");
                    } else {
                        Log.d(TAG, "Response Body: " + responseBodyString);
                    }

                    // Recrear el body para que Retrofit pueda usarlo
                    MediaType contentType = responseBody.contentType();
                    ResponseBody newBody = ResponseBody.create(contentType, responseBodyString);
                    response = response.newBuilder().body(newBody).build();
                }

                return response;
            }
        };

        // Configurar OkHttpClient con el interceptor de logging
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)   // Agregar nuestro interceptor personalizado
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
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