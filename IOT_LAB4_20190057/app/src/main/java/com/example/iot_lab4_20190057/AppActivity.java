package com.example.iot_lab4_20190057;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.iot_lab4_20190057.fragments.LocationsFragment;
import com.example.iot_lab4_20190057.fragments.ForecastFragment;
import com.example.iot_lab4_20190057.fragments.FutureFragment;

public class AppActivity extends AppCompatActivity {

    private NavController navController;
    private Button btnLocations, btnPronosticos, btnFuturo;
    private Button[] navButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        // Configurar Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        navController = navHostFragment.getNavController();

        // Inicializar botones de navegación
        btnLocations = findViewById(R.id.btn_locations);
        btnPronosticos = findViewById(R.id.btn_pronosticos);
        btnFuturo = findViewById(R.id.btn_futuro);

        navButtons = new Button[]{btnLocations, btnPronosticos, btnFuturo};

        // Configurar listeners para los botones
        setupNavigationButtons();

        // Configurar el botón de ubicaciones como seleccionado por defecto
        updateButtonSelection(0);
    }

    private void setupNavigationButtons() {
        btnLocations.setOnClickListener(v -> {
            navController.popBackStack(R.id.locationsFragment, false);
            updateButtonSelection(0);
        });

        btnPronosticos.setOnClickListener(v -> {
            navController.navigate(R.id.forecastFragment);
            updateButtonSelection(1);
        });

        btnFuturo.setOnClickListener(v -> {
            navController.navigate(R.id.futureFragment);
            updateButtonSelection(2);
        });
    }

    private void updateButtonSelection(int selectedIndex) {
        for (int i = 0; i < navButtons.length; i++) {
            if (i == selectedIndex) {
                navButtons[i].setTextColor(getResources().getColor(R.color.purple_dark, null));
                navButtons[i].setSelected(true);
            } else {
                navButtons[i].setTextColor(getResources().getColor(android.R.color.white, null));
                navButtons[i].setSelected(false);
            }
        }
    }
}