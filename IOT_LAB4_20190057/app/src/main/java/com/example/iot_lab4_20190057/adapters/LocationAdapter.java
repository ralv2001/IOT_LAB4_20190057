package com.example.iot_lab4_20190057.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab4_20190057.R;
import com.example.iot_lab4_20190057.models.LocationModel;

import java.util.List;

//USO DE CLAUDE (INTELEGENCIA ARTIFICIAL) PARA PARCHEAR BUGS

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LocationModel> locations;
    private OnLocationClickListener listener;

    public interface OnLocationClickListener {
        void onLocationClick(LocationModel location);
    }

    public LocationAdapter(List<LocationModel> locations, OnLocationClickListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationModel location = locations.get(position);
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void updateData(List<LocationModel> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLocationName;
        private TextView tvRegion;
        private TextView tvCountry;
        private TextView tvCoordinates;
        private TextView tvLocationId;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tv_location_name);
            tvRegion = itemView.findViewById(R.id.tv_region);
            tvCountry = itemView.findViewById(R.id.tv_country);
            tvCoordinates = itemView.findViewById(R.id.tv_coordinates);
            tvLocationId = itemView.findViewById(R.id.tv_location_id);
        }

        public void bind(LocationModel location) {
            tvLocationName.setText(location.getName());
            tvRegion.setText(location.getRegion());
            tvCountry.setText(location.getCountry());
            tvCoordinates.setText(String.format("Lat: %.2f, Lon: %.2f",
                    location.getLat(), location.getLon()));
            tvLocationId.setText("ID: " + location.getId());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocationClick(location);
                }
            });
        }
    }
}