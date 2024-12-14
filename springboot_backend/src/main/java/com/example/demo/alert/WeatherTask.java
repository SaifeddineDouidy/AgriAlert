package com.example.demo.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.appuser.AppUser;
import com.example.demo.appuser.AppUserRepository;
import com.example.demo.location.Location;
import com.example.demo.crops.CropService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
public class WeatherTask {

    private final AppUserRepository appUserRepository;
    private final CropService cropService;

    // Constructor with FirebaseService injected
    public WeatherTask(AppUserRepository appUserRepository, CropService cropService) {
        this.appUserRepository = appUserRepository;
        this.cropService = cropService;
    }

    @Scheduled(cron = "0 0 6 * * ?")
    public void fetchWeatherData() {
        List<AppUser> users = appUserRepository.findAll();

        for (AppUser user : users) {
            Location location = user.getLocation();
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Fetch weather data for the user's location
                String weatherData = fetchWeatherForLocation(latitude, longitude);

                // Extract max and min temperatures and rainfall from the fetched weather data
                double[] temperatures = extractTemperatures(weatherData);
                double[] rainfall = extractRainfall(weatherData);

                // Process the weather data for the user with both min and max temperatures and rainfall
                processWeatherData(user, temperatures[0], temperatures[1], rainfall[0], rainfall[1]);
            }
        }
    }

    private String fetchWeatherForLocation(double latitude, double longitude) {
        String url = "https://api.open-meteo.com/v1/forecast";
        String params = String.format("?latitude=%f&longitude=%f&daily=temperature_2m_max,temperature_2m_min,rain_sum&forecast_days=2", latitude, longitude);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url + params, String.class);
    }

    public void processWeatherData(AppUser user, double maxTemperature, double minTemperature, double maxRainfall, double minRainfall) {
        // Logic to parse the weather data and compare with crop thresholds
        System.out.println("Weather data for user " + user.getEmail() + ": Max Temp: " + maxTemperature + ", Min Temp: " + minTemperature +
                ", Max Rainfall: " + maxRainfall + ", Min Rainfall: " + minRainfall);

        // Example of how you might generate an alert for a user's crops
        for (String cropName : user.getCrops()) {
            String weatherReport = cropService.checkWeatherForCrop(cropName, maxTemperature, minTemperature, maxRainfall, minRainfall);
            System.out.println("Weather report for " + cropName + ": \n" + weatherReport);

        }
    }

    private double[] extractTemperatures(String weatherData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(weatherData);

            // Get the "daily" node where the temperature data is located
            JsonNode dailyNode = rootNode.path("daily");

            double maxTemperature = dailyNode.path("temperature_2m_max").get(0).asDouble();
            double minTemperature = dailyNode.path("temperature_2m_min").get(0).asDouble();

            return new double[]{maxTemperature, minTemperature};
        } catch (IOException e) {
            e.printStackTrace();
            return new double[]{0.0, 0.0};
        }
    }

    private double[] extractRainfall(String weatherData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(weatherData);

            JsonNode hourlyNode = rootNode.path("hourly");
            JsonNode rainData = hourlyNode.path("rain");

            double maxRainfall = 0.0;
            double minRainfall = Double.MAX_VALUE;

            for (int i = 24; i < rainData.size(); i++) {
                double rainfall = rainData.get(i).asDouble();
                if (rainfall > maxRainfall) {
                    maxRainfall = rainfall;
                }
                if (rainfall < minRainfall) {
                    minRainfall = rainfall;
                }
            }

            return new double[]{maxRainfall, minRainfall};
        } catch (IOException e) {
            e.printStackTrace();
            return new double[]{0.0, 0.0};
        }
    }
}
