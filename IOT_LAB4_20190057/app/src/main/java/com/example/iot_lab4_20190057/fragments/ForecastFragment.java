package com.example.iot_lab4_20190057.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab4_20190057.R;
import com.example.iot_lab4_20190057.adapters.ForecastAdapter;
import com.example.iot_lab4_20190057.api.RetrofitClient;
import com.example.iot_lab4_20190057.api.WeatherApi;
import com.example.iot_lab4_20190057.models.ForecastResponse;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

//USO DE CLAUDE (INTELEGENCIA ARTIFICIAL) PARA PARCHEAR BUGS

public class ForecastFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "ForecastFragment";
    private static final String API_KEY = "ec24b1c6dd8a4d528c1205500250305";
    private static final float SHAKE_THRESHOLD = 20.0f; // m/s²

    private EditText etIdLocation;
    private EditText etDiasForecast;
    private Button btnBuscarForecast;
    private RecyclerView recyclerForecast;
    private ForecastAdapter adapter;
    private WeatherApi weatherApi;

    // Variables para el acelerómetro
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_INTERVAL = 1000; // Intervalo mínimo entre detecciones (ms)

    // Variable para evitar cargas automáticas
    private boolean hasLoadedData = false;

    // Variable para guardar el ID original
    private String originalLocationId = null;

    // Para manejar la intermitencia de CDN
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        initializeViews(view);
        setupEmptyRecyclerView();
        setupWeatherApi();
        setupAccelerometer();
        checkForNavigationArguments();
        setupButtonListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        adapter = null;
        hasLoadedData = false;
    }

    private void initializeViews(View view) {
        Log.d(TAG, "initializeViews called");
        etIdLocation = view.findViewById(R.id.et_id_location);
        etDiasForecast = view.findViewById(R.id.et_dias_forecast);
        btnBuscarForecast = view.findViewById(R.id.btn_buscar_forecast);
        recyclerForecast = view.findViewById(R.id.recycler_forecast);
    }

    private void setupEmptyRecyclerView() {
        Log.d(TAG, "setupEmptyRecyclerView called");
        adapter = new ForecastAdapter(new ArrayList<>(), "", "");
        recyclerForecast.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerForecast.setAdapter(adapter);
    }

    private void setupRecyclerView(ForecastResponse forecast) {
        Log.d(TAG, "setupRecyclerView called with forecast for location: " + forecast.getLocation().getName());

        // Obtener el ID procesado
        String locationName = forecast.getLocation().getName();
        String locationId = forecast.getLocation().getId();

        // Aplicar la misma lógica que usamos en onResponse para el ID
        if (locationId == null || locationId.trim().isEmpty()) {
            if (originalLocationId != null && originalLocationId.startsWith("id:")) {
                locationId = originalLocationId.substring(3); // Remueve "id:" para obtener solo el número
                Log.d(TAG, "Using processed location ID for adapter: " + locationId);
            } else {
                locationId = "ID no disponible";
            }
        }

        if (adapter != null) {
            adapter.updateData(
                    forecast.getForecast().getForecastday(),
                    locationName,
                    locationId // Usar el ID procesado aquí
            );
        } else {
            adapter = new ForecastAdapter(
                    forecast.getForecast().getForecastday(),
                    locationName,
                    locationId // Usar el ID procesado aquí
            );
            recyclerForecast.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerForecast.setAdapter(adapter);
        }
    }

    private void setupWeatherApi() {
        Log.d(TAG, "setupWeatherApi called");
        weatherApi = RetrofitClient.getInstance().getWeatherApi();
    }

    private void setupAccelerometer() {
        Log.d(TAG, "setupAccelerometer called");
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (accelerometer == null) {
                Toast.makeText(getContext(),
                        "El dispositivo no tiene acelerómetro",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                Toast.makeText(getContext(),
                        "Sin conexión a Internet. Por favor, verifica tu conexión.",
                        Toast.LENGTH_LONG).show();
            }

            return isConnected;
        }

        return false;
    }

    private void checkForNavigationArguments() {
        Log.d(TAG, "checkForNavigationArguments called");
        if (getArguments() != null) {
            String locationId = getArguments().getString("locationId");
            Log.d(TAG, "locationId from arguments: " + locationId);

            if (locationId != null && !locationId.isEmpty() && !locationId.equals("null") && !hasLoadedData) {
                if (locationId.startsWith("id:") && locationId.length() > 3) {
                    Log.d(TAG, "Valid locationId found, loading data automatically");
                    originalLocationId = locationId;
                    etIdLocation.setText(locationId);
                    etDiasForecast.setText("14");
                    getForecast(locationId, 14);
                    hasLoadedData = true;
                    getArguments().clear();
                } else {
                    Log.e(TAG, "Invalid locationId format: " + locationId);
                    getArguments().clear();
                }
            } else {
                Log.d(TAG, "No valid locationId found, already loaded, or invalid: " + locationId);
            }
        } else {
            Log.d(TAG, "No arguments provided");
        }
    }

    private void setupButtonListener() {
        Log.d(TAG, "setupButtonListener called");
        btnBuscarForecast.setOnClickListener(v -> {
            String idLocation = etIdLocation.getText().toString().trim();
            String daysStr = etDiasForecast.getText().toString().trim();

            Log.d(TAG, "Search button clicked - idLocation: " + idLocation + ", days: " + daysStr);

            if (!TextUtils.isEmpty(idLocation) && !TextUtils.isEmpty(daysStr)) {
                try {
                    int days = Integer.parseInt(daysStr);
                    if (days > 0 && days <= 14) {
                        String searchQuery = idLocation;
                        if (!idLocation.startsWith("id:")) {
                            searchQuery = "id:" + idLocation;
                        }
                        getForecast(searchQuery, days);
                    } else {
                        Toast.makeText(getContext(), "Los días deben ser entre 1 y 14",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Por favor ingrese un número válido",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Por favor complete todos los campos",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Cuando presionan Enter en ambos EditText, ejecutar búsqueda
        TextView.OnEditorActionListener searchActionListener = (v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                // Simular click del botón
                btnBuscarForecast.performClick();
                return true; // Consumir el evento
            }
            return false; // No consumir el evento
        };

        etIdLocation.setOnEditorActionListener(searchActionListener);
        etDiasForecast.setOnEditorActionListener(searchActionListener);
    }

    private void getForecast(String locationId, int days) {
        Log.d(TAG, "getForecast called with locationId: " + locationId + ", days: " + days);

        // Verificar conexión a Internet
        if (!checkInternetConnection()) {
            return;
        }

        // Validar que el locationId sea válido
        if (locationId == null || locationId.isEmpty() || locationId.equals("null")) {
            Log.e(TAG, "Invalid locationId provided: " + locationId);
            Toast.makeText(getContext(), "ID de ubicación no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato del ID
        if (!locationId.startsWith("id:") || locationId.length() <= 3) {
            Log.e(TAG, "Invalid locationId format: " + locationId);
            Toast.makeText(getContext(), "Formato de ID inválido. Use 'id:número'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar el ID original para usar después
        originalLocationId = locationId;

        btnBuscarForecast.setEnabled(false);
        btnBuscarForecast.setText("Buscando...");

        // Log de detalles antes de la llamada
        Log.d(TAG, "=== API CALL DETAILS ===");
        Log.d(TAG, "Base URL: https://api.weatherapi.com/v1/");
        Log.d(TAG, "Endpoint: forecast.json");
        Log.d(TAG, "API Key: " + API_KEY.substring(0, 8) + "...[HIDDEN]");
        Log.d(TAG, "Query Parameters:");
        Log.d(TAG, "  - key: [HIDDEN]");
        Log.d(TAG, "  - q: " + locationId);
        Log.d(TAG, "  - days: " + days);
        Log.d(TAG, "Full URL (aproximado): https://api.weatherapi.com/v1/forecast.json?key=[HIDDEN]&q=" + locationId + "&days=" + days);
        Log.d(TAG, "Retry count: " + retryCount);
        Log.d(TAG, "========================");

        weatherApi.getForecast(API_KEY, locationId, days).enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call,
                                   @NonNull Response<ForecastResponse> response) {
                btnBuscarForecast.setEnabled(true);
                btnBuscarForecast.setText("Buscar");

                Log.d(TAG, "=== API RESPONSE DETAILS ===");
                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Response Message: " + response.message());
                Log.d(TAG, "Response Success: " + response.isSuccessful());

                // Log headers importantes para el debug de CDN
                Log.d(TAG, "Important CDN headers:");
                Log.d(TAG, "  cdn-cache: " + response.headers().get("cdn-cache"));
                Log.d(TAG, "  cdn-status: " + response.headers().get("cdn-status"));
                Log.d(TAG, "  x-weatherapi-qpm-left: " + response.headers().get("x-weatherapi-qpm-left"));

                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();

                    // Log detalles de la respuesta exitosa
                    Log.d(TAG, "SUCCESS - Forecast Data Received");
                    if (forecast.getLocation() != null) {
                        Log.d(TAG, "Location Name: " + forecast.getLocation().getName());
                        Log.d(TAG, "Location ID: " + forecast.getLocation().getId());
                        Log.d(TAG, "Location Country: " + forecast.getLocation().getCountry());
                        Log.d(TAG, "Location Region: " + forecast.getLocation().getRegion());
                    }

                    // AQUÍ ES DONDE DEBES LLAMAR A validateForecastResponse
                    validateForecastResponse(forecast);

                    // Validación de datos
                    if (forecast.getLocation() != null &&
                            forecast.getForecast() != null &&
                            forecast.getForecast().getForecastday() != null &&
                            !forecast.getForecast().getForecastday().isEmpty()) {

                        String locationName = forecast.getLocation().getName();
                        String locationId = forecast.getLocation().getId();

                        // Validar nombre y ID
                        if (locationName == null || locationName.trim().isEmpty()) {
                            locationName = "Ubicación Desconocida";
                        }

                        // FIX: La API NO devuelve el ID cuando haces query por ID
                        // Usamos el ID original que guardamos antes
                        if (locationId == null || locationId.trim().isEmpty()) {
                            // Extraer el ID del originalLocationId
                            if (originalLocationId != null && originalLocationId.startsWith("id:")) {
                                locationId = originalLocationId.substring(3); // Remueve "id:" para obtener solo el número
                                Log.d(TAG, "Using original location ID: " + locationId);
                            } else {
                                locationId = "ID no disponible";
                            }
                        }

                        Log.d(TAG, "Valid forecast data received - Location: " + locationName + ", ID: " + locationId);
                        setupRecyclerView(forecast);
                        recyclerForecast.scrollToPosition(0);

                        // Reset retry count on success
                        retryCount = 0;

                    } else {
                        Log.e(TAG, "Invalid forecast data received");
                        Toast.makeText(getContext(), "Datos de pronóstico inválidos recibidos", Toast.LENGTH_SHORT).show();

                        // Reset retry count
                        retryCount = 0;
                    }
                } else if (response.code() == 502) {
                    // Handle 502 specifically - retry logic
                    Log.e(TAG, "CDN Gateway error - attempting retry");

                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        Toast.makeText(getContext(),
                                "Problema temporal con el servidor. Reintentando... (" + retryCount + "/" + MAX_RETRIES + ")",
                                Toast.LENGTH_SHORT).show();

                        // Retry after a short delay
                        new android.os.Handler().postDelayed(() -> {
                            getForecast(originalLocationId, days);
                        }, 2000);
                    } else {
                        retryCount = 0;
                        Toast.makeText(getContext(),
                                "Servidor temporalmente no disponible. Por favor, intente más tarde.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Logging detallado del error
                    Log.e(TAG, "ERROR - API Call Failed");
                    Log.e(TAG, "Error Code: " + response.code());
                    Log.e(TAG, "Error Message: " + response.message());

                    // Reset retry count
                    retryCount = 0;

                    // Manejo de errores según código
                    if (response.code() == 400) {
                        Toast.makeText(getContext(), "ID de ubicación no válido. Use el formato 'id:número'",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error al obtener pronósticos (Código: " + response.code() + ")",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                Log.d(TAG, "=========================");
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                btnBuscarForecast.setEnabled(true);
                btnBuscarForecast.setText("Buscar");

                Log.e(TAG, "=== API CALL FAILURE DETAILS ===");
                Log.e(TAG, "Exception Type: " + t.getClass().getName());
                Log.e(TAG, "Exception Message: " + t.getMessage());

                // Log la request que falló
                if (call.request() != null) {
                    Log.e(TAG, "Failed Request URL: " + call.request().url());
                    Log.e(TAG, "Failed Request Method: " + call.request().method());
                }

                Log.e(TAG, "===========================");

                // Reset retry count
                retryCount = 0;

                // Mensaje más específico según el tipo de error
                String errorMessage;
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "La conexión tardó demasiado. Por favor, intenta nuevamente.";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "Sin conexión a Internet. Verifica tu conexión.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "No se pudo conectar al servidor. Intenta más tarde.";
                } else {
                    errorMessage = "Error de red: " + (t.getMessage() != null ? t.getMessage() : "Error desconocido");
                }

                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Implementación de SensorEventListener
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calcular la magnitud de la aceleración
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            // Verificar si supera el umbral
            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();

                // Evitar múltiples detecciones en un corto período
                if (currentTime - lastShakeTime > SHAKE_INTERVAL) {
                    lastShakeTime = currentTime;
                    showShakeDialog();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No necesitamos implementar nada aquí
    }

    private void showShakeDialog() {
        Log.d(TAG, "showShakeDialog called");

        // Verificar si hay datos para eliminar
        if (adapter == null || adapter.getItemCount() == 0) {
            Toast.makeText(getContext(), "No hay pronósticos para eliminar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmar acción")
                .setMessage("¿Está seguro que desea eliminar los últimos pronósticos obtenidos?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Limpiar los datos del adaptador
                    adapter.updateData(new ArrayList<>(), "", "");

                    // Limpiar los campos de búsqueda
                    etIdLocation.setText("");
                    etDiasForecast.setText("");

                    // Resetear la bandera
                    hasLoadedData = false;

                    Toast.makeText(getContext(), "Pronósticos eliminados",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void validateForecastResponse(ForecastResponse forecast) {
        Log.d(TAG, "=== VALIDATING FORECAST RESPONSE ===");

        if (forecast == null) {
            Log.e(TAG, "Forecast is NULL");
            return;
        }

        Log.d(TAG, "Location: " + (forecast.getLocation() != null ? "OK" : "NULL"));
        if (forecast.getLocation() != null) {
            Log.d(TAG, "  - Name: " + (forecast.getLocation().getName() != null ? forecast.getLocation().getName() : "NULL"));
            Log.d(TAG, "  - ID: " + (forecast.getLocation().getId() != null ? forecast.getLocation().getId() : "NULL"));

            // Log adicional para el problema del ID
            if (forecast.getLocation().getId() == null) {
                Log.w(TAG, "  - API BUG: Location ID is NULL when querying by ID");
                Log.w(TAG, "  - Using original locationId as fallback");
            }
        }

        Log.d(TAG, "Forecast: " + (forecast.getForecast() != null ? "OK" : "NULL"));
        if (forecast.getForecast() != null) {
            Log.d(TAG, "  - Forecastday: " + (forecast.getForecast().getForecastday() != null ? "OK" : "NULL"));
            if (forecast.getForecast().getForecastday() != null) {
                Log.d(TAG, "  - Forecastday Count: " + forecast.getForecast().getForecastday().size());

                // Validar primer día del forecast
                if (!forecast.getForecast().getForecastday().isEmpty()) {
                    var firstDay = forecast.getForecast().getForecastday().get(0);
                    Log.d(TAG, "  - First Day Date: " + (firstDay.getDate() != null ? firstDay.getDate() : "NULL"));
                    Log.d(TAG, "  - First Day Data: " + (firstDay.getDay() != null ? "OK" : "NULL"));

                    if (firstDay.getDay() != null) {
                        Log.d(TAG, "    - Condition: " + (firstDay.getDay().getCondition() != null ?
                                firstDay.getDay().getCondition().getText() : "NULL"));
                    }
                }
            }
        }

        Log.d(TAG, "================================");
    }
}