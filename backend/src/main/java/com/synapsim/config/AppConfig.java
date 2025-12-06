package com.synapsim.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Application-wide bean configurations
 */
@Configuration
public class AppConfig {

    /**
     * ModelMapper bean for DTO conversions
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Configure strict matching to avoid mapping errors
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);
        return modelMapper;
    }

    /**
     * WebClient for making HTTP requests to PubMed API
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer
                .build();
    }
}
