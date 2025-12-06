package com.synapsim.repository;

import com.synapsim.model.PubMedReference;
import com.synapsim.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PubMedReference entity
 */
@Repository
public interface PubMedReferenceRepository extends JpaRepository<PubMedReference, Long> {

    /**
     * Find all references for a specific simulation
     */
    List<PubMedReference> findBySimulation(Simulation simulation);

    /**
     * Find references by simulation ID
     */
    @Query("SELECT pr FROM PubMedReference pr WHERE pr.simulation.id = :simulationId ORDER BY pr.relevanceScore DESC")
    List<PubMedReference> findBySimulationId(@Param("simulationId") Long simulationId);

    /**
     * Find reference by PubMed ID
     */
    Optional<PubMedReference> findByPubmedId(String pubmedId);

    /**
     * Check if reference exists by PubMed ID
     */
    boolean existsByPubmedId(String pubmedId);

    /**
     * Find highly relevant references (above threshold)
     */
    @Query("SELECT pr FROM PubMedReference pr WHERE pr.relevanceScore >= :threshold ORDER BY pr.relevanceScore DESC")
    List<PubMedReference> findHighlyRelevantReferences(@Param("threshold") Double threshold);

    /**
     * Find references containing specific keywords
     */
    @Query("SELECT pr FROM PubMedReference pr WHERE LOWER(pr.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PubMedReference> findByKeyword(@Param("keyword") String keyword);

    /**
     * Find references by partial title match
     */
    @Query("SELECT pr FROM PubMedReference pr WHERE LOWER(pr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PubMedReference> searchByTitle(@Param("searchTerm") String searchTerm);

    /**
     * Get most cited references (assuming we implement citation tracking later)
     */
    @Query("SELECT pr FROM PubMedReference pr ORDER BY pr.relevanceScore DESC")
    List<PubMedReference> findTopReferences();
}
