package com.example.demo.alert;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Recommendation {
    private String message;

    public Recommendation(String message) {
        this.message = message;
    }

}
