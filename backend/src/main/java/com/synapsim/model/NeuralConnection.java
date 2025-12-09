package com.synapsim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a connection (edge) between two brain regions in the neural network
 */
@Entity
@Table(name = "neural_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeuralConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_region_id", nullable = false)
    private BrainRegion sourceRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_region_id", nullable = false)
    private BrainRegion targetRegion;

    @Column(nullable = false)
    private Double weight; // Connection strength (0.0 - 1.0)

    @Column(name = "baseline_weight", nullable = false)
    private Double baselineWeight; // Default/baseline connection strength

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type")
    private ConnectionType connectionType;

    @Column(name = "is_bidirectional")
    private Boolean isBidirectional = true;

    @Column(length = 500)
    private String description;

    public enum ConnectionType {
        EXCITATORY,    // Increases activity
        INHIBITORY,    // Decreases activity
        MODULATORY     // Modifies other connections
    }
}
