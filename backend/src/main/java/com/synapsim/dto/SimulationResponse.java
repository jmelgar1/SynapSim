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

    // Changes in connections
    private List<ConnectionChangeDTO> connectionChanges;

    // Analysis results
    private String predictionSummary;
    private Double confidenceScore;
    private Boolean success;
    private String badgeEarned;

    // Research references
    private List<PubMedArticleDTO> pubmedReferences;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConnectionChangeDTO {
        private String sourceRegion;
        private String targetRegion;
        private Double beforeWeight;
        private Double afterWeight;
        private Double changePercentage;
        private String changeType; // INCREASED, DECREASED, STABLE
    }
}
