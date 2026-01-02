package com.synapsim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for simulation results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResponse {

    private Long id;
    private Long scenarioId;
    private String status;
    private LocalDateTime executedAt;
    private Long processingTimeMs;

    // Network visualization data
    private BrainNetworkDTO networkState;

    // Brain regions mentioned in research
    private List<MentionedRegionDTO> mentionedRegions;

    // Analysis results
    private String predictionSummary;
    private Double confidenceScore;
    private Boolean success;
    private String badgeEarned;

    // Research references
    private List<PubMedArticleDTO> pubmedReferences;
}
