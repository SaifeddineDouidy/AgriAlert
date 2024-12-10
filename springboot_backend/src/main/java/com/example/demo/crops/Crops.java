package com.example.demo.crops;

public class Crops {
    private String name;
    private double minTemp;
    private double maxTemp;
    private double minRain;
    private double maxRain;

    // Constructor, getters, and setters
    public Crops(String name, double minTemp, double maxTemp, double minRain, double maxRain) {
        this.name = name;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minRain = minRain;
        this.maxRain = maxRain;
    }

    public String getName() {
        return name;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getMinRain() {
        return minRain;
    }

    public double getMaxRain() {
        return maxRain;
    }
}
