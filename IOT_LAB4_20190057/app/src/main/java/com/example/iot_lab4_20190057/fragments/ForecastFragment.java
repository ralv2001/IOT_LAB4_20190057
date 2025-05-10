package com.example.iot_lab4_20190057.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastFragment extends Fragment implements SensorEventListener {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupWeatherApi();
        setupAccelerometer();
        checkForNavigationArguments();
        setupButtonListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registrar el listener del acelerómetro
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Desregistrar el listener del acelerómetro
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Asegurarse de desregistrar el listener
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void initializeViews(View view) {
        etIdLocation = view.findViewById(R.id.et_id_location);
        etDiasForecast = view.findViewById(R.id.et_dias_forecast);
        btnBuscarForecast = view.findViewById(R.id.btn_buscar_forecast);
        recyclerForecast = view.findViewById(R.id.recycler_forecast);
    }

    private void setupRecyclerView() {
        adapter = new ForecastAdapter(new ArrayList<>(), "", "");
        recyclerForecast.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerForecast.setAdapter(adapter);
    }

    private void setupWeatherApi() {
        weatherApi = RetrofitClient.getInstance().getWeatherApi();
    }

    private void setupAccelerometer() {
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

    private void checkForNavigationArguments() {
        if (getArguments() != null) {
            String locationId = getArguments().getString("locationId");
            if (locationId != null) {
                // Si venimos desde el fragmento de ubicaciones
                etIdLocation.setText(locationId);
                etDiasForecast.setText("14");
                // Buscar automáticamente
                getForecast(locationId, 14);
            }
        }
    }

    private void setupButtonListener() {
        btnBuscarForecast.setOnClickListener(v -> {
            String idLocation = etIdLocation.getText().toString().trim();
            String daysStr = etDiasForecast.getText().toString().trim();

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
        btnBuscarForecast.setEnabled(false);
        btnBuscarForecast.setText("Buscando...");

        weatherApi.getForecast(API_KEY, locationId, days).enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call,
                                   @NonNull Response<ForecastResponse> response) {
                btnBuscarForecast.setEnabled(true);
                btnBuscarForecast.setText("Buscar");

                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();
                    if (forecast.getForecast() != null &&
                            forecast.getForecast().getForecastday() != null &&
                            !forecast.getForecast().getForecastday().isEmpty()) {

                        // Actualizar el RecyclerView con los resultados
                        adapter.updateData(
                                forecast.getForecast().getForecastday(),
                                forecast.getLocation().getName(),
                                forecast.getLocation().getId()
                        );

                        // Opcional: Desplazarse al inicio de la lista
                        recyclerForecast.scrollToPosition(0);
                    } else {
                        Toast.makeText(getContext(), "No hay datos de pronóstico disponibles",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
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
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
        // Verificar si hay datos para eliminar
        if (adapter.getItemCount() == 0) {
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