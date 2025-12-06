package com.synapsim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a completed simulation with results
 */
@Entity
@Table(name = "simulations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SimulationStatus status;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    // Store simulation results as JSON
    @Column(name = "network_state", columnDefinition = "TEXT")
    private String networkState; // JSON representation of brain network

    @Column(name = "connection_changes", columnDefinition = "TEXT")
    private String connectionChanges; // JSON array of connection weight changes

    @Column(name = "prediction_summary", length = 2000)
    private String predictionSummary;

    @Column(name = "confidence_score")
    private Double confidenceScore; // 0.0 - 1.0

    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PubMedReference> pubmedReferences = new ArrayList<>();

    @Column(name = "success")
    private Boolean success;

    @Column(name = "badge_earned")
    private String badgeEarned;

    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }

    public enum SimulationStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
