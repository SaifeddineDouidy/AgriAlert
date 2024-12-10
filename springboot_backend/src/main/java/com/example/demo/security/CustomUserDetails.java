package com.example.demo.security;

import com.example.demo.appuser.AppUser;
import com.example.demo.appuser.AppUserRole;
import com.example.demo.location.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetails.class);
    private final AppUser appUser;

    public CustomUserDetails(AppUser appUser) {
        this.appUser = appUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return appUser.getAuthorities();
    }

    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    public String getPhoneNumber() {
        return appUser.getPhoneNumber();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !appUser.getLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return appUser.getEnabled();
    }

    public String getFirstName() {
        return appUser.getFirstName();
    }

    public String getLastName() {
        return appUser.getLastName();
    }

    public List<String> getCrops() {
        return appUser.getCrops();
    }

    public AppUserRole getAppUserRole() {
        return appUser.getAppUserRole();
    }

    public List<Location> getLocations() {
        if (appUser.getLocation() != null) {
            List<Location> locations = new java.util.ArrayList<>();
            locations.add(appUser.getLocation());
            return locations;
        } else {
            List<Location> locations = new java.util.ArrayList<>();
            return locations;
        }
    }
}

