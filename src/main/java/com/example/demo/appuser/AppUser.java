package com.example.demo.appuser;

import com.example.demo.location.Location;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString(exclude = {"password"}) // Exclude sensitive data from logs
@Entity
@EntityListeners(AuditingEntityListener.class) // Enable JPA Auditing
public class AppUser implements UserDetails {

    @SequenceGenerator(
            name = "student_sequence",
            sequenceName = "student_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "student_sequence"
    )
    private Long id;

    @NotEmpty(message = "First name cannot be empty")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phoneNumber;

    @CreatedDate // Automatically set when the entity is created
    private LocalDateTime createdAt;

    @LastModifiedDate // Automatically set when the entity is updated
    private LocalDateTime lastLogin;

    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;

    private Boolean locked = false;
    private Boolean enabled = false;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = true) // Allow temporary null value
    private Location location;

    @ElementCollection
    private List<String> crops = new ArrayList<>();

    // Constructor with all parameters
    @Builder
    public AppUser(String firstName,
                   String lastName,
                   String email,
                   String password,
                   String phoneNumber,
                   LocalDateTime createdAt,
                   LocalDateTime lastLogin,
                   AppUserRole appUserRole,
                   Location location,
                   List<String> crops) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now(); // Default to now
        this.lastLogin = lastLogin;
        this.appUserRole = appUserRole;
        this.location = location;
        this.crops = crops != null ? crops : new ArrayList<>();
    }

    // Automatically set the creation timestamp
    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(appUserRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
