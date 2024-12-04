package com.example.demo.registration;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping(consumes = "application/json")
    public String register(@RequestBody RegistrationRequest request, @RequestHeader HttpHeaders headers) {
        // You can now access the raw request body and the headers
        System.out.println("Received body: " + request);
        System.out.println("Content-Type: " + headers.getContentType());
        return registrationService.register(request);
    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }

}
