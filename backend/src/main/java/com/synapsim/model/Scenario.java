package com.synapsim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Represents a user-created scenario with parameters for simulation
 */
@Entity
@Table(name = "scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quest_id")
    private String questId;

    @Enumerated(EnumType.STRING)
    @Column(name = "compound_inspiration", nullable = false)
    private CompoundType compoundInspiration;

    @Enumerated(EnumType.STRING)
    @Column(name = "therapeutic_setting", nullable = false)
    private TherapeuticSetting therapeuticSetting;

    @Column(name = "primary_brain_region")
    private String primaryBrainRegion;

    @Enumerated(EnumType.STRING)
    @Column(name = "simulation_duration")
    private SimulationDuration simulationDuration;

    @Column(name = "integration_steps", length = 1000)
    private String integrationSteps;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum CompoundType {
        PSILOCYBIN("psilocybin"),
        LSD("lsd"),
        KETAMINE("ketamine"),
        MDMA("mdma");

        private final String value;

        CompoundType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum TherapeuticSetting {
        CALM_NATURE("calm-nature"),
        GUIDED_THERAPY("guided-therapy"),
        MEDITATION_SPACE("meditation-space"),
        CREATIVE_STUDIO("creative-studio"),
        SOCIAL_GATHERING("social-gathering");

        private final String value;

        TherapeuticSetting(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum SimulationDuration {
        SHORT("short"),
        MEDIUM("medium"),
        EXTENDED("extended");

        private final String value;

        SimulationDuration(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
