package com.synapsim.repository;

import com.synapsim.model.BrainRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BrainRegion entity
 */
@Repository
public interface BrainRegionRepository extends JpaRepository<BrainRegion, Long> {

    /**
     * Find brain region by code (e.g., "PFC", "AMY")
     */
    Optional<BrainRegion> findByCode(String code);

    /**
     * Find brain region by name
     */
    Optional<BrainRegion> findByName(String name);

    /**
     * Find all brain regions with high neuroplasticity potential
     */
    @Query("SELECT br FROM BrainRegion br WHERE br.neuroplasticityPotential >= :threshold ORDER BY br.neuroplasticityPotential DESC")
    List<BrainRegion> findHighPlasticityRegions(Double threshold);

    /**
     * Check if a brain region exists by code
     */
    boolean existsByCode(String code);
}
