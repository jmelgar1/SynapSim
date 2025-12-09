package com.synapsim.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for onboarding quiz submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    @NotNull(message = "Baseline mood is required")
    @Min(value = 1, message = "Mood must be between 1 and 10")
    @Max(value = 10, message = "Mood must be between 1 and 10")
    private Integer mood;

    @NotNull(message = "Focus area is required")
    private String focusArea;

    @NotNull(message = "Experience level is required")
    private String experience;

    @NotNull(message = "Learning style is required")
    private String learningStyle;

    @NotNull(message = "Goal intensity is required")
    private String goalIntensity;

    private String userId; // Optional - for future authentication
}
