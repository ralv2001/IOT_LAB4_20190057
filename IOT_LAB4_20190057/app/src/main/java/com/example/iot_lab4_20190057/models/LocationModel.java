package com.example.iot_lab4_20190057.models;

import com.google.gson.annotations.SerializedName;

public class LocationModel {
    // Cambiar el tipo de String a long para manejar IDs numéricos
    @SerializedName("id")
    private long id;

    private String name;
    private String region;
    private String country;
    private double lat;
    private double lon;
    private String url;

    // Getters y setters con el nuevo tipo long para id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}