package com.synapsim.repository;

import com.synapsim.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Scenario entity
 */
@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    /**
     * Find scenarios by quest ID
     */
    List<Scenario> findByQuestId(String questId);

    /**
     * Find scenarios by compound inspiration type
     */
    List<Scenario> findByCompoundInspiration(Scenario.CompoundType compoundInspiration);

    /**
     * Find scenarios by therapeutic setting
     */
    List<Scenario> findByTherapeuticSetting(Scenario.TherapeuticSetting therapeuticSetting);

    /**
     * Find scenarios created after a specific date
     */
    List<Scenario> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find recent scenarios (ordered by creation date, most recent first)
     */
    @Query("SELECT s FROM Scenario s ORDER BY s.createdAt DESC")
    List<Scenario> findRecentScenarios();

    /**
     * Count scenarios by quest ID
     */
    Long countByQuestId(String questId);

    /**
     * Find scenarios targeting a specific brain region
     */
    List<Scenario> findByPrimaryBrainRegion(String primaryBrainRegion);
}
