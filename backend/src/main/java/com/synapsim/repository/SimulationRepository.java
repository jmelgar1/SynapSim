package com.synapsim.repository;

import com.synapsim.model.Scenario;
import com.synapsim.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Simulation entity
 */
@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {

    /**
     * Find simulations by scenario
     */
    List<Simulation> findByScenario(Scenario scenario);

    /**
     * Find simulations by status
     */
    List<Simulation> findByStatus(Simulation.SimulationStatus status);

    /**
     * Find successful simulations
     */
    List<Simulation> findBySuccessTrue();

    /**
     * Find failed simulations
     */
    List<Simulation> findBySuccessFalse();

    /**
     * Find simulations executed after a specific date
     */
    List<Simulation> findByExecutedAtAfter(LocalDateTime dateTime);

    /**
     * Find recent simulations (ordered by execution date, most recent first)
     */
    @Query("SELECT s FROM Simulation s ORDER BY s.executedAt DESC")
    List<Simulation> findRecentSimulations();

    /**
     * Find simulations with high confidence scores
     */
    @Query("SELECT s FROM Simulation s WHERE s.confidenceScore >= :threshold ORDER BY s.confidenceScore DESC")
    List<Simulation> findHighConfidenceSimulations(@Param("threshold") Double threshold);

    /**
     * Get average processing time for completed simulations
     */
    @Query("SELECT AVG(s.processingTimeMs) FROM Simulation s WHERE s.status = 'COMPLETED'")
    Double getAverageProcessingTime();

    /**
     * Count simulations by status
     */
    Long countByStatus(Simulation.SimulationStatus status);

    /**
     * Find simulations that earned a specific badge
     */
    List<Simulation> findByBadgeEarned(String badgeEarned);

    /**
     * Find simulations by scenario ID
     */
    @Query("SELECT s FROM Simulation s WHERE s.scenario.id = :scenarioId ORDER BY s.executedAt DESC")
    List<Simulation> findByScenarioId(@Param("scenarioId") Long scenarioId);
}
