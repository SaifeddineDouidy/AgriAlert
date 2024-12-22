package com.example.demo.crops;

import com.example.demo.alert.Alert;
import com.example.demo.alert.AlertSeverity;
import com.example.demo.alert.Recommendation;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CropWeatherResponse {
    private Map<String, CropAnalysis> cropAnalyses;
    private List<String> errors;  // Added this field


    @Data
    public static class CropAnalysis {
        private AlertSeverity overallSeverity;
        private List<Alert> alerts;
        private List<Recommendation> recommendations;
        private List<String> insights;
    }
}

