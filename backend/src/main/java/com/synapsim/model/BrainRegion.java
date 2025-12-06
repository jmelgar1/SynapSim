package com.synapsim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a brain region (node) in the neural network graph
 */
@Entity
@Table(name = "brain_regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrainRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String code; // e.g., "PFC", "AMY", "HPC"

    @Column(length = 1000)
    private String description;

    @Column(name = "function_description", length = 1000)
    private String functionDescription;

    // Position for visualization (x, y coordinates)
    private Double positionX;
    private Double positionY;

    @Column(name = "baseline_activity")
    private Double baselineActivity; // Baseline activity level (0.0 - 1.0)

    @Column(name = "neuroplasticity_potential")
    private Double neuroplasticityPotential; // How easily this region forms new connections (0.0 - 1.0)
}
