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

    private String integrationSteps;

    // Optional research focus
    private String researchFocus;

    // Helper method to convert string to enum
    public Scenario.CompoundType getCompoundType() {
        return Scenario.CompoundType.valueOf(compoundInspiration.toUpperCase().replace("-", "_"));
    }

    public Scenario.TherapeuticSetting getSettingType() {
        return Scenario.TherapeuticSetting.valueOf(therapeuticSetting.toUpperCase().replace("-", "_"));
    }

    // Helper method for research focus (returns null if not set)
    public Scenario.ResearchFocus getResearchFocusType() {
        if (researchFocus == null || researchFocus.isEmpty()) {
            return null;
        }
        return Scenario.ResearchFocus.valueOf(researchFocus.toUpperCase().replace("-", "_"));
    }
}
