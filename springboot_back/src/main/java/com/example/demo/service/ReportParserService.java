package com.example.demo.service;

import com.example.demo.utils.crops.CropWeatherResponse;
import com.example.demo.model.Alert;
import com.example.demo.model.AlertSeverity;
import com.example.demo.model.AlertType;
import com.example.demo.model.Recommendation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportParserService {

    private static final Logger logger = LoggerFactory.getLogger(ReportParserService.class);

    public CropWeatherResponse.CropAnalysis parseReport(String report) {
        if (report == null || report.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty report cannot be parsed");
        }

        CropWeatherResponse.CropAnalysis analysis = new CropWeatherResponse.CropAnalysis();

        // Parse each section
        analysis.setOverallSeverity(parseOverallSeverity(report));
        analysis.setAlerts(parseAlerts(report));
        analysis.setRecommendations(parseRecommendations(report));
        analysis.setInsights(parseInsights(report));

        return analysis;
    }
    public AlertSeverity parseOverallSeverity(String report) {
        Pattern severityPattern = Pattern.compile("Overall Severity:\\s+(\\w+)");
        Matcher matcher = severityPattern.matcher(report);

        if (matcher.find()) {
            try {
                return AlertSeverity.valueOf(matcher.group(1));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid severity found in report: {}", matcher.group(1));
                return AlertSeverity.LOW;
            }
        }
        return AlertSeverity.LOW;
    }

    public List<Alert> parseAlerts(String report) {
        List<Alert> alerts = new ArrayList<>();
        Pattern alertPattern = Pattern.compile(
                "(❌|⚠️|ℹ️)\\s+([^\\n]{1,100})\\n\\s+([^\\n]{1,200})\\n\\s+Severity:\\s+(LOW|MEDIUM|HIGH)"
        );
        Matcher matcher = alertPattern.matcher(report);

        while (matcher.find()) {
            Alert alert = new Alert();

            // Set alert type based on emoji
            switch (matcher.group(1)) {
                case "❌":
                    alert.setType(AlertType.ERROR);
                    break;
                case "⚠️":
                    alert.setType(AlertType.WARNING);
                    break;
                case "ℹ️":
                default:
                    alert.setType(AlertType.INFO);
                    break;
            }

            alert.setTitle(matcher.group(2).trim());
            alert.setMessage(matcher.group(3).trim());
            try {
                alert.setSeverity(AlertSeverity.valueOf(matcher.group(4).trim()));
            } catch (IllegalArgumentException e) {
                alert.setSeverity(AlertSeverity.LOW);
            }

            alerts.add(alert);
        }

        return alerts;
    }

    public List<Recommendation> parseRecommendations(String report) {
        List<Recommendation> recommendations = new ArrayList<>();

        Pattern sectionPattern = Pattern.compile("Recommended Actions:\\n((?:-[^\\n]{1,200}\\n?){1,10})");
        Matcher sectionMatcher = sectionPattern.matcher(report);

        if (sectionMatcher.find()) {
            Pattern recommendationPattern = Pattern.compile("-\\s+([^\\n]+)");
            Matcher matcher = recommendationPattern.matcher(sectionMatcher.group(1));

            while (matcher.find()) {
                recommendations.add(new Recommendation(matcher.group(1).trim()));
            }
        }

        return recommendations;
    }

    public List<String> parseInsights(String report) {
        List<String> insights = new ArrayList<>();

        Pattern sectionPattern = Pattern.compile("Additional Insights:\\n((?:-[^\\n]+\\n?)+)");
        Matcher sectionMatcher = sectionPattern.matcher(report);

        if (sectionMatcher.find()) {
            Pattern insightPattern = Pattern.compile("-\\s+([^\\n]+)");
            Matcher matcher = insightPattern.matcher(sectionMatcher.group(1));

            while (matcher.find()) {
                insights.add(matcher.group(1).trim());
            }
        }

        return insights;
    }
}
