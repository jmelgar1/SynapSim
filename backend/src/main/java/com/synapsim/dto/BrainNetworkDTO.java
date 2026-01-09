package com.synapsim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for brain network graph visualization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrainNetworkDTO {

    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;
    private NetworkMetricsDTO metrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NodeDTO {
        private String id;
        private String name;
        private String code;
        private String description;
        private Double activityLevel;
        private Double positionX;
        private Double positionY;
        private String color;
        private Integer size;
        private List<String> connectedRegions;  // List of connected region codes
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EdgeDTO {
        private String id;
        private String source;
        private String target;
        private Double weight;
        private String connectionType;
        private Boolean isBidirectional;
        private String color;
        private Integer thickness;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NetworkMetricsDTO {
        private Integer totalNodes;
        private Integer totalConnections;
        private Double averageConnectionStrength;
        private Double networkDensity;
        private String dominantPattern;
    }
}
