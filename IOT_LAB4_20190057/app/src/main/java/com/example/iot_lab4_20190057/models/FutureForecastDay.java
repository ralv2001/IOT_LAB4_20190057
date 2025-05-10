package com.example.iot_lab4_20190057.models;

public class FutureForecastDay {
    private String date;
    private long date_epoch;
    private FutureDay day;
    private Astro astro;
    private FutureHour[] hour; // Para el pronóstico por hora

    // Getters y setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDate_epoch() {
        return date_epoch;
    }

    public void setDate_epoch(long date_epoch) {
        this.date_epoch = date_epoch;
    }

    public FutureDay getDay() {
        return day;
    }

    public void setDay(FutureDay day) {
        this.day = day;
    }

    public Astro getAstro() {
        return astro;
    }

    public void setAstro(Astro astro) {
        this.astro = astro;
    }

    public FutureHour[] getHour() {
        return hour;
    }

    public void setHour(FutureHour[] hour) {
        this.hour = hour;
    }
}