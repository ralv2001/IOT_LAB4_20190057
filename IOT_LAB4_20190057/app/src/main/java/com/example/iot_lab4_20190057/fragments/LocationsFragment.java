package com.example.iot_lab4_20190057.fragments;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab4_20190057.AppActivity;
import com.example.iot_lab4_20190057.R;
import com.example.iot_lab4_20190057.adapters.LocationAdapter;
import com.example.iot_lab4_20190057.api.RetrofitClient;
import com.example.iot_lab4_20190057.api.WeatherApi;
import com.example.iot_lab4_20190057.models.LocationModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationsFragment extends Fragment {

    private static final String API_KEY = "ec24b1c6dd8a4d528c1205500250305";

    private EditText etSearchLocation;
    private Button btnBuscar;
    private RecyclerView recyclerLocations;
    private LocationAdapter adapter;
    private WeatherApi weatherApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        // setupRecyclerView();
        setupWeatherApi();
        setupButtonListener();
    }

    private void initializeViews(View view) {
        etSearchLocation = view.findViewById(R.id.et_search_location);
        btnBuscar = view.findViewById(R.id.btn_buscar);
        recyclerLocations = view.findViewById(R.id.recycler_locations);
    }

    private void setupRecyclerView(List<LocationModel> locations) {
        adapter = new LocationAdapter(locations, location -> {
            // Navegar al fragmento de pronósticos pasando el ID de la locación
            Bundle args = new Bundle();
            args.putString("locationId", "id:" + location.getId());
            Navigation.findNavController(getView())
                    .navigate(R.id.action_locations_to_forecast, args);
            // Actualizamos el botón de menú seleccionado
            ((AppActivity) getActivity()).updateButtonSelection(1);
        });

        recyclerLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerLocations.setAdapter(adapter);
    }

    private void setupWeatherApi() {
        weatherApi = RetrofitClient.getInstance().getWeatherApi();
    }

    private void setupButtonListener() {
        btnBuscar.setOnClickListener(v -> {
            String query = etSearchLocation.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                searchLocations(query);
            } else {
                Toast.makeText(getContext(), "Por favor ingrese una ubicación",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchLocations(String query) {
        btnBuscar.setEnabled(false);
        btnBuscar.setText("Buscando...");

        weatherApi.searchLocations(API_KEY, query).enqueue(new Callback<List<LocationModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationModel>> call,
                                   @NonNull Response<List<LocationModel>> response) {
                btnBuscar.setEnabled(true);
                btnBuscar.setText("Buscar");

                if (response.isSuccessful() && response.body() != null) {
                    List<LocationModel> locations = response.body();
                    if (!locations.isEmpty()) {
                        setupRecyclerView(locations);
                    } else {
                        Toast.makeText(getContext(), "No se encontraron ubicaciones",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al buscar ubicaciones",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationModel>> call, @NonNull Throwable t) {
                btnBuscar.setEnabled(true);
                btnBuscar.setText("Buscar");
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("Error al llamar servicio", Objects.requireNonNull(t.getMessage()));
            }
        });
    }
}