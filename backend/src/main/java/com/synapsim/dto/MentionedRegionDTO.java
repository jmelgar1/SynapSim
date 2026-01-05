package com.synapsim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for brain regions mentioned in research with associated excerpts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentionedRegionDTO {

    private String regionCode;
    private String regionName;
    private List<ResearchMention> mentions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResearchMention {
        private String articleTitle;
        private String pubmedId;
        private String excerpt;
        private String context; // e.g., "connectivity", "activity", "neuroplasticity"
    }

    public void addMention(ResearchMention mention) {
        if (this.mentions == null) {
            this.mentions = new ArrayList<>();
        }
        this.mentions.add(mention);
    }
}
