package com.example.iot_lab4_20190057.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab4_20190057.R;
import com.example.iot_lab4_20190057.models.Forecastday;
import com.bumptech.glide.Glide;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<Forecastday> forecasts;
    private String locationName;
    private String locationId;

    public ForecastAdapter(List<Forecastday> forecasts, String locationName, String locationId) {
        this.forecasts = forecasts;
        this.locationName = locationName;
        this.locationId = locationId;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        Forecastday forecast = forecasts.get(position);
        holder.bind(forecast, locationName, locationId);
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    public void updateData(List<Forecastday> newForecasts, String locationName, String locationId) {
        this.forecasts = newForecasts;
        this.locationName = locationName;
        this.locationId = locationId;
        notifyDataSetChanged();
    }

    class ForecastViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvLocation;
        private TextView tvLocationId;
        private TextView tvMaxTemp;
        private TextView tvMinTemp;
        private TextView tvAvgTemp;
        private TextView tvCondition;
        private TextView tvHumidity;
        private TextView tvWind;
        private TextView tvPrecipitation;
        private ImageView ivCondition;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvLocationId = itemView.findViewById(R.id.tv_location_id);
            tvMaxTemp = itemView.findViewById(R.id.tv_max_temp);
            tvMinTemp = itemView.findViewById(R.id.tv_min_temp);
            tvAvgTemp = itemView.findViewById(R.id.tv_avg_temp);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            tvHumidity = itemView.findViewById(R.id.tv_humidity);
            tvWind = itemView.findViewById(R.id.tv_wind);
            tvPrecipitation = itemView.findViewById(R.id.tv_precipitation);
            ivCondition = itemView.findViewById(R.id.iv_condition);
        }

        public void bind(Forecastday forecast, String locationName, String locationId) {
            Log.e("ForecastViewHolder", "Binding forecast: " + locationId + " - " + locationName + " - " + forecast);
            tvDate.setText(forecast.getDate());
            tvLocation.setText(locationName);
            tvLocationId.setText("ID: " + locationId);
            tvMaxTemp.setText(String.format("Máx: %.1f°C", forecast.getDay().getMaxtemp_c()));
            tvMinTemp.setText(String.format("Mín: %.1f°C", forecast.getDay().getMintemp_c()));
            tvAvgTemp.setText(String.format("Promedio: %.1f°C", forecast.getDay().getAvgtemp_c()));
            tvCondition.setText(forecast.getDay().getCondition().getText());
            tvHumidity.setText(String.format("Humedad: %.0f%%", forecast.getDay().getAvghumidity()));
            tvWind.setText(String.format("Viento: %.1f km/h", forecast.getDay().getMaxwind_kph()));
            tvPrecipitation.setText(String.format("Precipitación: %.1f mm", forecast.getDay().getTotalprecip_mm()));

            // Cargar icono del clima
            String iconUrl = "https:" + forecast.getDay().getCondition().getIcon();
            Glide.with(itemView.getContext())
                    .load(iconUrl)
                    .into(ivCondition);
        }
    }
}