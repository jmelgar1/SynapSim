package com.synapsim.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a unique connection between two brain regions (order-independent)
 * Used as a key in maps to store connection evidence
 */
@Getter
@EqualsAndHashCode
public class ConnectionKey {

    private final String region1;
    private final String region2;

    /**
     * Constructor ensures alphabetical ordering for consistency
     */
    public ConnectionKey(String regionA, String regionB) {
        if (regionA.compareTo(regionB) < 0) {
            this.region1 = regionA;
            this.region2 = regionB;
        } else {
            this.region1 = regionB;
            this.region2 = regionA;
        }
    }

    @Override
    public String toString() {
        return region1 + "-" + region2;
    }
}
