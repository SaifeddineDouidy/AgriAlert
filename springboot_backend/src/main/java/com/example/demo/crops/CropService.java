package com.example.demo.crops;

import com.example.demo.alert.AlertSeverity;
import com.example.demo.alert.severities.RainfallAlertSeverity;
import com.example.demo.alert.severities.TemperatureAlertSeverity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CropService {

    private static final Map<String, Crops> cropData = new HashMap<>();

    static {
        // Populate crop data
        cropData.put("Rice", new Crops("Rice", 15, 27, 100, 150));
        cropData.put("Wheat", new Crops("Wheat", 12, 25, 25, 75));
        cropData.put("Maize", new Crops("Maize", 15, 27, 65, 125));
        cropData.put("Millets", new Crops("Millets", 20, 35, 25, 75));
        cropData.put("Bajra (Pearl Millet)", new Crops("Bajra (Pearl Millet)", 25, 35, 25, 60));
        cropData.put("Pulses (Kharif)", new Crops("Pulses (Kharif)", 20, 27, 25, 60));
        cropData.put("Lentil (Rabi)", new Crops("Lentil (Rabi)", 15, 25, 25, 50));
        cropData.put("Oilseeds", new Crops("Oilseeds", 15, 30, 30, 50));
        cropData.put("Groundnut", new Crops("Groundnut", 20, 30, 50, 75));
        cropData.put("Sugarcane", new Crops("Sugarcane", 20, 35, 85, 165));
        cropData.put("Sugar beet", new Crops("Sugar beet", 10, 25, 25, 50));
        cropData.put("Cotton", new Crops("Cotton", 18, 27, 60, 110));
        cropData.put("Tea", new Crops("Tea", 15, 35, 100, 250));
        cropData.put("Coffee", new Crops("Coffee", 15, 28, 125, 225));
        cropData.put("Cocoa", new Crops("Cocoa", 18, 35, 100, 250));
        cropData.put("Rubber", new Crops("Rubber", 27, 27, 150, 250));
        cropData.put("Jute", new Crops("Jute", 25, 35, 150, 250));
        cropData.put("Flax", new Crops("Flax", 10, 20, 15, 20));
        cropData.put("Coconut", new Crops("Coconut", 27, 27, 100, 250));
        cropData.put("Oil-palm", new Crops("Oil-palm", 27, 33, 250, 400));
        cropData.put("Clove", new Crops("Clove", 25, 35, 200, 250));
        cropData.put("Black Pepper", new Crops("Black Pepper", 15, 40, 200, 300));
        cropData.put("Cardamom", new Crops("Cardamom", 10, 35, 150, 400));
        cropData.put("Turmeric", new Crops("Turmeric", 20, 30, 150, 250));
    }

    public static String checkWeatherForCrop(String cropName, double maxTemp, double minTemp, double maxRain, double minRain) {
        Crops crop = cropData.get(cropName);
        if (crop == null) {
            return "Crop not found in our database.";
        }

        WeatherAnalysisResult result = new WeatherAnalysisResult();

        // Perform analysis for temperature and rainfall
        analyzeTemperature(crop, maxTemp, minTemp, result); // Modify the temperature analysis
        analyzeRainfall(crop, maxRain, minRain, result); // Modify the rainfall analysis

        // Generate and return a detailed report
        return generateDetailedReport(cropName, result);
    }

    private static void analyzeTemperature(Crops crop, double maxTemp, double minTemp, WeatherAnalysisResult result) {
        double tempDeviationMax = calculateDeviation(maxTemp, crop.getMinTemp(), crop.getMaxTemp());
        double tempDeviationMin = calculateDeviation(minTemp, crop.getMinTemp(), crop.getMaxTemp());

        // Handle max temperature
        if (maxTemp > crop.getMaxTemp()) {
            result.addTemperatureAlert(TemperatureAlertSeverity.HIGH);
            handleHighTemperature(maxTemp, crop, result);
        } else if (maxTemp < crop.getMinTemp()) {
            result.addTemperatureAlert(TemperatureAlertSeverity.LOW);
            handleLowTemperature(maxTemp, crop, result);
        }

        // Handle min temperature
        if (minTemp > crop.getMaxTemp()) {
            result.addTemperatureAlert(TemperatureAlertSeverity.HIGH);
            handleHighTemperature(minTemp, crop, result);
        } else if (minTemp < crop.getMinTemp()) {
            result.addTemperatureAlert(TemperatureAlertSeverity.LOW);
            handleLowTemperature(minTemp, crop, result);
        }

        // Add growth impact insights
        result.addInsight(String.format("Max temperature deviation: %.1f%%, Min temperature deviation: %.1f%%", Math.abs(tempDeviationMax) * 10, Math.abs(tempDeviationMin) * 10));
    }

    private static void analyzeRainfall(Crops crop, double maxRain, double minRain, WeatherAnalysisResult result) {
        double rainDeviationMax = calculateDeviation(maxRain, crop.getMinRain(), crop.getMaxRain());
        double rainDeviationMin = calculateDeviation(minRain, crop.getMinRain(), crop.getMaxRain());

        // Handle max rainfall
        if (maxRain > crop.getMaxRain()) {
            result.addRainfallAlert(RainfallAlertSeverity.HIGH);
            handleHighRainfall(maxRain, crop, result);
        } else if (maxRain < crop.getMinRain()) {
            result.addRainfallAlert(RainfallAlertSeverity.LOW);
            handleLowRainfall(maxRain, crop, result);
        }

        // Handle min rainfall
        if (minRain > crop.getMaxRain()) {
            result.addRainfallAlert(RainfallAlertSeverity.HIGH);
            handleHighRainfall(minRain, crop, result);
        } else if (minRain < crop.getMinRain()) {
            result.addRainfallAlert(RainfallAlertSeverity.LOW);
            handleLowRainfall(minRain, crop, result);
        }

        // Add moisture impact insights
        result.addInsight(String.format("Max rainfall deviation: %.1f%%, Min rainfall deviation: %.1f%%", Math.abs(rainDeviationMax) * 10, Math.abs(rainDeviationMin) * 10));
    }

    private static void handleLowTemperature(double userTemp, Crops crop, WeatherAnalysisResult result) {
        if (userTemp < crop.getMinTemp() - 5) {
            result.addRecommendation("Extreme cold detected. Use frost blankets or greenhouse protection.");
            result.addRecommendation("Consider using heating systems for sensitive crops.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            result.addRecommendation("Implement frost protection measures.");
            result.addRecommendation("Cover crops with mulch to retain soil warmth.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }

    private static void handleHighTemperature(double userTemp, Crops crop, WeatherAnalysisResult result) {
        if (userTemp > crop.getMaxTemp() + 5) {
            result.addRecommendation("Extreme heat warning. Implement emergency cooling strategies.");
            result.addRecommendation("Use shade nets, increase irrigation, and avoid midday activities.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            result.addRecommendation("Protect crops from heat stress.");
            result.addRecommendation("Provide temporary shade and increase watering frequency.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }

    private static void handleLowRainfall(double userRain, Crops crop, WeatherAnalysisResult result) {
        if (userRain < crop.getMinRain() - 50) {
            result.addRecommendation("Severe drought conditions detected. Immediate irrigation required.");
            result.addRecommendation("Consider emergency water conservation and crop protection measures.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            result.addRecommendation("Implement water conservation techniques.");
            result.addRecommendation("Use drip irrigation or micro-sprinkler systems.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }

    private static void handleHighRainfall(double userRain, Crops crop, WeatherAnalysisResult result) {
        if (userRain > crop.getMaxRain() + 50) {
            result.addRecommendation("Flood risk detected. Implement immediate drainage solutions.");
            result.addRecommendation("Prepare for potential crop relocation or protection.");
            result.setSeverity(AlertSeverity.HIGH);
        } else {
            result.addRecommendation("Ensure proper drainage to prevent waterlogging.");
            result.addRecommendation("Use raised beds or improve soil drainage.");
            result.setSeverity(AlertSeverity.MEDIUM);
        }
    }

    private static double calculateDeviation(double currentValue, double minValue, double maxValue) {
        if (currentValue < minValue) {
            return (minValue - currentValue) / minValue;
        } else if (currentValue > maxValue) {
            return (currentValue - maxValue) / maxValue;
        }
        return 0;
    }

    private static String generateDetailedReport(String cropName, WeatherAnalysisResult result) {
        StringBuilder report = new StringBuilder();

        // Title and Crop Identification
        report.append("Weather Analysis Report for ").append(cropName).append("\n\n");

        // Severity Assessment
        report.append("Overall Severity: ").append(result.getSeverity()).append("\n\n");

        // Alerts Section
        if (!result.getTemperatureAlerts().isEmpty()) {
            report.append("Temperature Alerts:\n");
            result.getTemperatureAlerts().forEach(alert -> report.append("- ").append(alert).append("\n"));
            report.append("\n");
        }

        if (!result.getRainfallAlerts().isEmpty()) {
            report.append("Rainfall Alerts:\n");
            result.getRainfallAlerts().forEach(alert -> report.append("- ").append(alert).append("\n"));
            report.append("\n");
        }

        // Recommendations Section
        if (!result.getRecommendations().isEmpty()) {
            report.append("Recommended Actions:\n");
            result.getRecommendations().forEach(rec -> report.append("- ").append(rec).append("\n"));
            report.append("\n");
        }

        // Insights Section
        if (!result.getInsights().isEmpty()) {
            report.append("Additional Insights:\n");
            result.getInsights().forEach(insight -> report.append("- ").append(insight).append("\n"));
        }

        return report.toString();
    }


    @Getter
    static class WeatherAnalysisResult {
        @Setter
        private AlertSeverity severity = AlertSeverity.LOW;
        private final List<TemperatureAlertSeverity> temperatureAlerts = new ArrayList<>();
        private final List<RainfallAlertSeverity> rainfallAlerts = new ArrayList<>();
        private final List<String> recommendations = new ArrayList<>();
        private final List<String> insights = new ArrayList<>();

        public void addTemperatureAlert(TemperatureAlertSeverity alert) {
            temperatureAlerts.add(alert);
        }

        public void addRainfallAlert(RainfallAlertSeverity alert) {
            rainfallAlerts.add(alert);
        }

        public void addRecommendation(String recommendation) {
            recommendations.add(recommendation);
        }

        public void addInsight(String insight) {
            insights.add(insight);
        }
    }
}
