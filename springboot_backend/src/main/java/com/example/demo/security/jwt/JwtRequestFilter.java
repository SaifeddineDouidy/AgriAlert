package com.example.demo.security.jwt;


import com.example.demo.appuser.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtil; // JwtUtil is the utility class to generate/validate JWT tokens

    @Autowired
    private AppUserService appUserService; // Service to load user details based on token

    @Override
    protected void doFilterInternal(HttpServletRequest request, javax.servlet.http.HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get the Authorization header
        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // If the header is in the form of "Bearer token"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract token
            username = jwtUtil.extractUsername(jwt); // Extract username (email in this case)
        }

        // If the token is valid and not already authenticated, authenticate the user
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, appUserService.loadUserByUsername(username))) {
                // If the token is valid, set the authentication in the security context
                WebAuthenticationDetailsSource detailsSource = new WebAuthenticationDetailsSource();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, appUserService.loadUserByUsername(username).getAuthorities())
                );
            }
        }

        // Proceed with the request
        filterChain.doFilter(request, response);
    }
}

