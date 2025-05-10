package com.example.iot_lab4_20190057.models;

import java.util.List;

public class FutureForecast {
    private List<FutureForecastDay> forecastday;

    public List<FutureForecastDay> getForecastday() {
        return forecastday;
    }

    public void setForecastday(List<FutureForecastDay> forecastday) {
        this.forecastday = forecastday;
    }
}