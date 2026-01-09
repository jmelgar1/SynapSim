package com.synapsim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evidence of a connection between two brain regions found in research
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionEvidence {

    private String sourceRegion;
    private String targetRegion;
    private ConfidenceLevel confidence;
    private String evidenceText;  // The actual sentence from research
    private String pubmedId;
    private String articleTitle;

    public enum ConfidenceLevel {
        HIGH,    // Both regions + connectivity keyword in same sentence
        MEDIUM,  // Both regions in same sentence, no keyword
        LOW      // Both regions in same paragraph
    }
}
