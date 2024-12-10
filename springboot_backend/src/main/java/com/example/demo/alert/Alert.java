package com.example.demo.alert;


public class Alert {
    private AlertType type;
    private String title;
    private String message;
    private AlertSeverity severity;

    public Alert(AlertType type, String title, String message, AlertSeverity severity) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.severity = severity;
    }

    public AlertType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }
}

enum AlertType {
    INFO, WARNING, ERROR
}



