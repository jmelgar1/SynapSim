package com.synapsim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS configuration to allow frontend access
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials
        config.setAllowCredentials(true);

        // Allow frontend origin (adjust port if needed)
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // Vite default dev server
            "http://localhost:3000",  // Alternative dev port
            "http://localhost:4173"   // Vite preview
        ));

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Apply CORS configuration to all paths
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
