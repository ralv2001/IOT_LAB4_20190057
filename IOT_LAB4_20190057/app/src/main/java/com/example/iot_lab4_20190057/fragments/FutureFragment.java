package com.example.iot_lab4_20190057.fragments;

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
import com.example.iot_lab4_20190057.adapters.FutureHourAdapter;
import com.example.iot_lab4_20190057.api.RetrofitClient;
import com.example.iot_lab4_20190057.api.WeatherApi;
import com.example.iot_lab4_20190057.models.FutureResponse;
import com.example.iot_lab4_20190057.models.FutureForecastDay;
import com.example.iot_lab4_20190057.models.FutureHour;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public class FutureFragment extends Fragment {

    private static final String API_KEY = "ec24b1c6dd8a4d528c1205500250305";

    private EditText etIdLocationFuture;
    private EditText etDiaInteres;
    private Button btnBuscarFuture;
    private RecyclerView recyclerFuture;
    private FutureHourAdapter adapter;
    private WeatherApi weatherApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_future, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupWeatherApi();
        setupButtonListener();
    }

    private void initializeViews(View view) {
        etIdLocationFuture = view.findViewById(R.id.et_id_location_future);
        etDiaInteres = view.findViewById(R.id.et_dia_interes);
        btnBuscarFuture = view.findViewById(R.id.btn_buscar_future);
        recyclerFuture = view.findViewById(R.id.recycler_future);
    }

    private void setupRecyclerView() {
        adapter = new FutureHourAdapter(new ArrayList<>(), "", "");
        recyclerFuture.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerFuture.setAdapter(adapter);
    }

    private void setupWeatherApi() {
        weatherApi = RetrofitClient.getInstance().getWeatherApi();
    }

    private void setupButtonListener() {
        btnBuscarFuture.setOnClickListener(v -> {
            String idLocation = etIdLocationFuture.getText().toString().trim();
            String dateStr = etDiaInteres.getText().toString().trim();

            if (!TextUtils.isEmpty(idLocation) && !TextUtils.isEmpty(dateStr)) {
                // Validar el formato de fecha
                if (!isValidDateFormat(dateStr)) {
                    Toast.makeText(getContext(), "Por favor ingrese la fecha en formato YYYY-MM-DD",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validar que la fecha sea entre 14 y 300 días en el futuro
                if (!isValidFutureDate(dateStr)) {
                    Toast.makeText(getContext(), "La fecha debe ser entre 14 y 300 días en el futuro",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Verificar si el ID tiene el formato correcto
                String searchQuery = idLocation;
                if (!idLocation.startsWith("id:")) {
                    searchQuery = "id:" + idLocation;
                }

                getFutureWeather(searchQuery, dateStr);
            } else {
                Toast.makeText(getContext(), "Por favor complete todos los campos",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // AGREGAR ESTE CÓDIGO NUEVO:
        // Cuando presionan Enter en ambos EditText, ejecutar búsqueda
        TextView.OnEditorActionListener searchActionListener = (v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                // Simular click del botón
                btnBuscarFuture.performClick();
                return true; // Consumir el evento
            }
            return false; // No consumir el evento
        };

        etIdLocationFuture.setOnEditorActionListener(searchActionListener);
        etDiaInteres.setOnEditorActionListener(searchActionListener);
    }

    private boolean isValidDateFormat(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidFutureDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate inputDate = LocalDate.parse(dateStr, formatter);
            LocalDate today = LocalDate.now();

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, inputDate);

            return daysBetween >= 14 && daysBetween <= 300;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void getFutureWeather(String locationId, String date) {
        btnBuscarFuture.setEnabled(false);
        btnBuscarFuture.setText("Buscando...");

        weatherApi.getFutureWeather(API_KEY, locationId, date).enqueue(new Callback<FutureResponse>() {
            @Override
            public void onResponse(@NonNull Call<FutureResponse> call,
                                   @NonNull Response<FutureResponse> response) {
                btnBuscarFuture.setEnabled(true);
                btnBuscarFuture.setText("Buscar");

                if (response.isSuccessful() && response.body() != null) {
                    FutureResponse futureResponse = response.body();
                    if (futureResponse.getForecast() != null &&
                            futureResponse.getForecast().getForecastday() != null &&
                            !futureResponse.getForecast().getForecastday().isEmpty()) {

                        FutureForecastDay forecastDay = futureResponse.getForecast().getForecastday().get(0);

                        if (forecastDay.getHour() != null && forecastDay.getHour().length > 0) {
                            // Convertir el array a lista
                            List<FutureHour> hoursList = Arrays.asList(forecastDay.getHour());

                            // Actualizar el RecyclerView con los resultados
                            adapter.updateData(
                                    hoursList,
                                    futureResponse.getLocation().getName(),
                                    futureResponse.getLocation().getId()
                            );

                            // Opcional: Desplazarse al inicio de la lista
                            recyclerFuture.scrollToPosition(0);
                        } else {
                            Toast.makeText(getContext(), "No hay datos de pronóstico por hora disponibles",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No hay datos de pronóstico disponibles para esta fecha",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 400) {
                        Toast.makeText(getContext(), "ID de ubicación o fecha no válidos. " +
                                        "Use el formato 'id:número' para la ubicación y 'YYYY-MM-DD' para la fecha",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error al obtener pronóstico futuro",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FutureResponse> call, @NonNull Throwable t) {
                btnBuscarFuture.setEnabled(true);
                btnBuscarFuture.setText("Buscar");
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}