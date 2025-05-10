package com.example.iot_lab4_20190057.models;

public class FutureResponse {
    private Location location;
    private Current current;
    private FutureForecast forecast;

    // Getters y setters
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Current getCurrent() {
        return current;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public FutureForecast getForecast() {
        return forecast;
    }

    public void setForecast(FutureForecast forecast) {
        this.forecast = forecast;
    }
}