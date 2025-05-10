package com.example.iot_lab4_20190057;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnComenzar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Validar conexión a Internet inmediatamente al ingresar
        if (!isConnectedToInternet()) {
            mostrarDialogoSinInternet();
            return; // Evita continuar con la configuración si no hay internet
        }

        // Inicializar vistas solo si hay conexión
        btnComenzar = findViewById(R.id.btnComenzar);

        // Configurar listeners
        btnComenzar.setOnClickListener(v -> {
            // Ahora solo redirige a AppActivity sin verificar nuevamente
            Intent intent = new Intent(MainActivity.this, AppActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Método para verificar si hay conexión a Internet
     */
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Mostrar diálogo cuando no hay conexión a Internet
     */
    private void mostrarDialogoSinInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sin conexión a Internet")
                .setMessage("No tienes conexión a Internet. Por favor, verifica tu conexión.")
                .setPositiveButton("Configuración", (dialog, which) -> {
                    // Redirigir a ajustes del dispositivo
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                    // Cerrar la aplicación ya que no hay conexión
                    finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Cerrar la aplicación ya que no hay conexión
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}