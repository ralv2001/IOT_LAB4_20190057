package com.example.iot_lab4_20190057.models;

public class FutureHour {
    private long time_epoch;
    private String time;
    private double temp_c;
    private double temp_f;
    private Condition condition;
    private double wind_mph;
    private double wind_kph;
    private int wind_degree;
    private String wind_dir;
    private double pressure_mb;
    private double pressure_in;
    private double precip_mm;
    private double precip_in;
    private int humidity;
    private int cloud;
    private double feelslike_c;
    private double feelslike_f;
    private double windchill_c;
    private double windchill_f;
    private double heatindex_c;
    private double heatindex_f;
    private double dewpoint_c;
    private double dewpoint_f;
    private int will_it_rain;
    private int will_it_snow;
    private int is_day;
    private double vis_km;
    private double vis_miles;
    private int chance_of_rain;
    private int chance_of_snow;
    private double gust_mph;
    private double gust_kph;
    private double uv;

    // Getters básicos para lo que necesitemos mostrar
    public long getTime_epoch() {
        return time_epoch;
    }

    public void setTime_epoch(long time_epoch) {
        this.time_epoch = time_epoch;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getTemp_c() {
        return temp_c;
    }

    public void setTemp_c(double temp_c) {
        this.temp_c = temp_c;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getPrecip_mm() {
        return precip_mm;
    }

    public void setPrecip_mm(double precip_mm) {
        this.precip_mm = precip_mm;
    }

    public double getWind_kph() {
        return wind_kph;
    }

    public void setWind_kph(double wind_kph) {
        this.wind_kph = wind_kph;
    }

    public int getChance_of_rain() {
        return chance_of_rain;
    }

    public void setChance_of_rain(int chance_of_rain) {
        this.chance_of_rain = chance_of_rain;
    }
}