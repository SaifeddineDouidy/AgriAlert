package com.example.demo.appuser;

import com.example.demo.appuser.profile.LocationUpdateRequest;
import com.example.demo.appuser.profile.ProfileUpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/user")
@AllArgsConstructor
@Validated
public class AppUserController {

    private final AppUserService appUserService;
    private final AppUserRepository appUserRepository;

    /**
     * Get the current user's profile information.
     *
     * @return the profile of the currently authenticated user
     */
    @GetMapping("/profile")
    public ResponseEntity<AppUser> getProfile() {
        // Get the currently authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Fetch the user's details
        Optional<AppUser> appUserOptional = appUserRepository.findByEmail(email);
        return appUserOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    /**
     * Update the current user's profile information.
     *
     * @param profileUpdateRequest object containing the updated profile information
     * @return the updated profile
     */
    @PutMapping("/profile")
    public ResponseEntity<AppUser> updateProfile(@Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        // Get the currently authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Update user profile
        Optional<AppUser> appUserOptional = appUserRepository.findByEmail(email);
        if (appUserOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();

            // Update fields based on the request
            appUser.setFirstName(profileUpdateRequest.getFirstName());
            appUser.setLastName(profileUpdateRequest.getLastName());
            appUser.setPhoneNumber(profileUpdateRequest.getPhoneNumber());

            // Save updated user
            appUserRepository.save(appUser);

            return ResponseEntity.ok(appUser);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    /**
     * Update the user's location.
     *
     * @param locationUpdateRequest object containing latitude and longitude
     * @return success message
     */
    @PutMapping("/location")
    public ResponseEntity<String> updateLocation(@Valid @RequestBody LocationUpdateRequest locationUpdateRequest) {
        // Get the currently authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Update the location through the service method
        String response = appUserService.updateLocation(email, locationUpdateRequest.getLatitude(), locationUpdateRequest.getLongitude());

        return ResponseEntity.ok(response);
    }
}
