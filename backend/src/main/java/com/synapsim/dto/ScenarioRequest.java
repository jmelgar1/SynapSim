package com.synapsim.dto;

import com.synapsim.model.Scenario;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new scenario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioRequest {

    private String questId;

    @NotNull(message = "Compound inspiration is required")
    private String compoundInspiration;

    @NotNull(message = "Therapeutic setting is required")
    private String therapeuticSetting;

    private String primaryBrainRegion;

    @NotNull(message = "Simulation duration is required")
    private String simulationDuration;

    private String integrationSteps;

    // Helper method to convert string to enum
    public Scenario.CompoundType getCompoundType() {
        return Scenario.CompoundType.valueOf(compoundInspiration.toUpperCase().replace("-", "_"));
    }

    public Scenario.TherapeuticSetting getSettingType() {
        return Scenario.TherapeuticSetting.valueOf(therapeuticSetting.toUpperCase().replace("-", "_"));
    }

    public Scenario.SimulationDuration getDurationType() {
        return Scenario.SimulationDuration.valueOf(simulationDuration.toUpperCase());
    }
}
