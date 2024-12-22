package com.example.demo.crops;

import com.example.demo.alert.Alert;
import com.example.demo.alert.AlertSeverity;
import com.example.demo.alert.AlertType;
import com.example.demo.alert.Recommendation;
import com.example.demo.utils.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crops")
@Validated
public class CropController {

    private static final Logger logger = LoggerFactory.getLogger(CropController.class);

    private final CropService cropService;

    @Autowired
    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping("/weather-analysis")
    public ResponseEntity<?> analyzeWeatherForCrops(@Valid @RequestBody CropWeatherRequest request) {
        logger.info("Received weather analysis request for crops: {}", request.getCropNames());

        try {
            validateRequest(request);

            CropWeatherResponse response = new CropWeatherResponse();
            Map<String, CropWeatherResponse.CropAnalysis> analyses = new HashMap<>();
            List<String> errors = new ArrayList<>();

            for (String cropName : request.getCropNames()) {
                try {
                    CropWeatherResponse.CropAnalysis analysis = analyzeSingleCrop(
                            cropName,
                            request.getMaxTemp(),
                            request.getMinTemp(),
                            request.getMaxRain(),
                            request.getMinRain()
                    );
                    analyses.put(cropName, analysis);
                } catch (Exception e) {
                    logger.error("Error analyzing crop: " + cropName, e);
                    errors.add(String.format("Failed to analyze crop '%s': %s", cropName, e.getMessage()));
                }
            }

            response.setCropAnalyses(analyses);
            response.setErrors(errors);

            if (analyses.isEmpty() && !errors.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(
                        LocalDateTime.now(),
                        400,
                        "Analysis Failed",
                        errors
                ));
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    LocalDateTime.now(),
                    400,
                    "Invalid Request",
                    Collections.singletonList(e.getMessage())
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during weather analysis", e);
            return ResponseEntity.status(500).body(new ErrorResponse(
                    LocalDateTime.now(),
                    500,
                    "Internal Server Error",
                    Collections.singletonList("An unexpected error occurred while processing your request")
            ));
        }
    }

    private void validateRequest(CropWeatherRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getMinTemp() > request.getMaxTemp()) {
            errors.add("Minimum temperature cannot be greater than maximum temperature");
        }

        if (request.getMinRain() > request.getMaxRain()) {
            errors.add("Minimum rainfall cannot be greater than maximum rainfall");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }

    private CropWeatherResponse.CropAnalysis analyzeSingleCrop(
            String cropName,
            double maxTemp,
            double minTemp,
            double maxRain,
            double minRain) {

        logger.debug("Analyzing crop: {} with temp range: {}-{}, rain range: {}-{}",
                cropName, minTemp, maxTemp, minRain, maxRain);

        String report = cropService.checkWeatherForCrop(cropName, maxTemp, minTemp, maxRain, minRain);

        if (report == null || report.trim().isEmpty()) {
            throw new CropAnalysisException("No analysis report generated for crop: " + cropName);
        }

        CropWeatherResponse.CropAnalysis analysis = new CropWeatherResponse.CropAnalysis();

        // Parse overall severity
        analysis.setOverallSeverity(parseOverallSeverity(report));

        // Parse alerts
        analysis.setAlerts(parseAlerts(report));

        // Parse recommendations
        analysis.setRecommendations(parseRecommendations(report));

        // Parse insights
        analysis.setInsights(parseInsights(report));

        return analysis;
    }

    private AlertSeverity parseOverallSeverity(String report) {
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

    private List<Alert> parseAlerts(String report) {
        List<Alert> alerts = new ArrayList<>();
        Pattern alertPattern = Pattern.compile("(❌|⚠️|ℹ️)\\s+([^\\n]{1,100})\\n\\s+([^\\n]{1,200})\\n\\s+Severity:\\s+(LOW|MEDIUM|HIGH)");
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

    private List<Recommendation> parseRecommendations(String report) {
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

    private List<String> parseInsights(String report) {
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Validation Error",
                errors
        ));
    }
}
