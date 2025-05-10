package com.example.iot_lab4_20190057.models;

public class Current {
    private long last_updated_epoch;
    private String last_updated;
    private double temp_c;
    private double temp_f;
    private Condition condition;

    // Getters básicos
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
}