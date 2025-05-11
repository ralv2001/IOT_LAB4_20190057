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
import com.example.iot_lab4_20190057.models.FutureHour;
import com.bumptech.glide.Glide;

import java.util.List;

public class FutureHourAdapter extends RecyclerView.Adapter<FutureHourAdapter.FutureHourViewHolder> {

    private List<FutureHour> futureHours;
    private String locationName;
    private String locationId;

    public FutureHourAdapter(List<FutureHour> futureHours, String locationName, String locationId) {
        this.futureHours = futureHours;
        this.locationName = locationName;
        this.locationId = locationId;
    }

    @NonNull
    @Override
    public FutureHourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_future_hour, parent, false);
        return new FutureHourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FutureHourViewHolder holder, int position) {
        FutureHour hour = futureHours.get(position);
        holder.bind(hour, locationName, locationId);
    }

    @Override
    public int getItemCount() {
        return futureHours.size();
    }

    public void updateData(List<FutureHour> newFutureHours, String locationName, String locationId) {
        this.futureHours = newFutureHours;
        this.locationName = locationName;
        this.locationId = locationId;
        notifyDataSetChanged();
    }

    class FutureHourViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;
        private TextView tvLocation;
        private TextView tvLocationId;
        private TextView tvTemp;
        private TextView tvCondition;
        private TextView tvHumidity;
        private TextView tvPrecipMm;
        private TextView tvRainChance;
        private TextView tvWindKph;
        private ImageView ivCondition;

        public FutureHourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvLocationId = itemView.findViewById(R.id.tv_location_id);
            tvTemp = itemView.findViewById(R.id.tv_temp);
            tvCondition = itemView.findViewById(R.id.tv_condition);
            tvHumidity = itemView.findViewById(R.id.tv_humidity);
            tvPrecipMm = itemView.findViewById(R.id.tv_precip_mm);
            tvRainChance = itemView.findViewById(R.id.tv_rain_chance);
            tvWindKph = itemView.findViewById(R.id.tv_wind_kph);
            ivCondition = itemView.findViewById(R.id.iv_condition);
        }

        public void bind(FutureHour hour, String locationName, String locationId) {
            // Validación y formateo seguro
            if (hour == null || hour.getCondition() == null) {
                Log.e("FutureHourAdapter", "Invalid hour data");
                return;
            }

            // Formatear la hora con validación
            String time = hour.getTime();
            String hourOnly = "";
            if (time != null && time.contains(" ")) {
                String[] parts = time.split(" ");
                if (parts.length > 1) {
                    hourOnly = parts[1];
                }
            }

            // Manejar datos nulos o vacíos
            if (locationName == null || locationName.trim().isEmpty()) {
                locationName = "Ubicación Desconocida";
            }

            if (locationId == null || locationId.trim().isEmpty()) {
                locationId = "ID no disponible";
            }

            tvTime.setText(hourOnly);
            tvLocation.setText(locationName);
            tvLocationId.setText("ID: " + locationId);


            tvTemp.setText(String.format("%.1f°C", hour.getTemp_c()));
            tvCondition.setText(hour.getCondition().getText());
            tvHumidity.setText(String.format("Humedad: %d%%", hour.getHumidity()));
            tvPrecipMm.setText(String.format("Precipitación: %.1f mm", hour.getPrecip_mm()));
            tvRainChance.setText(String.format("Prob. lluvia: %d%%", hour.getChance_of_rain()));
            tvWindKph.setText(String.format("Viento: %.1f km/h", hour.getWind_kph()));

            // Cargar icono del clima
            String iconUrl = "https:" + hour.getCondition().getIcon();
            Glide.with(itemView.getContext())
                    .load(iconUrl)
                    .into(ivCondition);
        }
    }
}