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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        // Agregar este log para detectar llamadas inesperadas
        Log.d(TAG, "StackTrace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        initializeViews(view);
        setupEmptyRecyclerView(); // Configurar RecyclerView vacío por defecto
        setupWeatherApi();
        setupAccelerometer();
        checkForNavigationArguments();
        setupButtonListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Registrar el listener del acelerómetro
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        // Desregistrar el listener del acelerómetro
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        // Asegurarse de desregistrar el listener
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        adapter = null; // Limpiar referencia del adaptador
        hasLoadedData = false; // Resetear la bandera
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
        // Configurar RecyclerView vacío
        adapter = new ForecastAdapter(new ArrayList<>(), "", "");
        recyclerForecast.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerForecast.setAdapter(adapter);
    }

    private void setupRecyclerView(ForecastResponse forecast) {
        Log.d(TAG, "setupRecyclerView called with forecast for location: " + forecast.getLocation().getName());
        if (adapter != null) {
            // Si ya existe un adaptador, actualizar sus datos
            adapter.updateData(
                    forecast.getForecast().getForecastday(),
                    forecast.getLocation().getName(),
                    forecast.getLocation().getId()
            );
        } else {
            // Crear nuevo adaptador
            adapter = new ForecastAdapter(
                    forecast.getForecast().getForecastday(),
                    forecast.getLocation().getName(),
                    forecast.getLocation().getId()
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
        // Obtener el SensorManager
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        // Obtener el sensor del acelerómetro
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

            // Validar que el locationId sea un ID válido y no esté vacío
            if (locationId != null && !locationId.isEmpty() && !locationId.equals("null") && !hasLoadedData) {
                // Verificar que tenga el formato correcto 'id:número'
                if (locationId.startsWith("id:") && locationId.length() > 3) {
                    Log.d(TAG, "Valid locationId found, loading data automatically");
                    originalLocationId = locationId; // Guardar el ID original
                    etIdLocation.setText(locationId);
                    etDiasForecast.setText("14");
                    getForecast(locationId, 14);
                    hasLoadedData = true;
                    getArguments().clear();
                } else {
                    Log.e(TAG, "Invalid locationId format: " + locationId);
                    // Limpiar argumentos inválidos
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
                        // Verificar si el ID tiene el formato correcto
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
    }

    private void getForecast(String locationId, int days) {
        Log.d(TAG, "getForecast called with locationId: " + locationId + ", days: " + days);
        Log.d(TAG, "Using API_KEY: " + API_KEY.substring(0, 8) + "...");
        Log.d(TAG, "Full URL: forecast.json?key=HIDDEN&q=" + locationId + "&days=" + days);

        Log.d(TAG, "getForecast called with locationId: " + locationId + ", days: " + days);

        // Verificar conexión a Internet antes de hacer la llamada
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

        btnBuscarForecast.setEnabled(false);
        btnBuscarForecast.setText("Buscando...");

        weatherApi.getForecast(API_KEY, locationId, days).enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call,
                                   @NonNull Response<ForecastResponse> response) {
                btnBuscarForecast.setEnabled(true);
                btnBuscarForecast.setText("Buscar");

                Log.d(TAG, "API Response received - Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();

                    // Validación mejorada de los datos
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

                        // Si el ID viene null desde la API, usamos el ID original sin el prefijo "id:"
                        if (locationId == null || locationId.trim().isEmpty()) {
                            // Extraer el ID del campo de búsqueda que ya tiene el formato "id:número"
                            String currentText = etIdLocation.getText().toString().trim();
                            if (currentText.startsWith("id:") && currentText.length() > 3) {
                                locationId = currentText.substring(3); // Remueve "id:" para obtener solo el número
                            } else {
                                locationId = "ID no disponible";
                            }
                        }

                        Log.d(TAG, "Valid forecast data received - Location: " + locationName + ", ID: " + locationId);

                        Log.d(TAG, "Valid forecast data received for: " + forecast.getLocation().getName());
                        setupRecyclerView(forecast);
                        recyclerForecast.scrollToPosition(0);
                    } else {
                        Log.e(TAG, "Invalid forecast data received");
                        if (forecast.getLocation() != null) {
                            Log.e(TAG, "Location name: " + forecast.getLocation().getName());
                            Log.e(TAG, "Location ID: " + forecast.getLocation().getId());
                        }
                        Toast.makeText(getContext(), "Datos de pronóstico inválidos recibidos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API Error - Code: " + response.code());
                    if (response.code() == 400) {
                        Toast.makeText(getContext(), "ID de ubicación no válido. Use el formato 'id:número'",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error al obtener pronósticos",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                btnBuscarForecast.setEnabled(true);
                btnBuscarForecast.setText("Buscar");

                Log.e(TAG, "API Call failed: " + t.getClass().getSimpleName() + " - " + t.getMessage());

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
}