package com.synapsim.repository;

import com.synapsim.model.BrainRegion;
import com.synapsim.model.NeuralConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for NeuralConnection entity
 */
@Repository
public interface NeuralConnectionRepository extends JpaRepository<NeuralConnection, Long> {

    /**
     * Find all connections from a specific brain region
     */
    List<NeuralConnection> findBySourceRegion(BrainRegion sourceRegion);

    /**
     * Find all connections to a specific brain region
     */
    List<NeuralConnection> findByTargetRegion(BrainRegion targetRegion);

    /**
     * Find connection between two specific regions
     */
    @Query("SELECT nc FROM NeuralConnection nc WHERE nc.sourceRegion.id = :sourceId AND nc.targetRegion.id = :targetId")
    Optional<NeuralConnection> findConnectionBetween(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    /**
     * Find all connections involving a specific region (either source or target)
     */
    @Query("SELECT nc FROM NeuralConnection nc WHERE nc.sourceRegion.id = :regionId OR nc.targetRegion.id = :regionId")
    List<NeuralConnection> findConnectionsInvolvingRegion(@Param("regionId") Long regionId);

    /**
     * Find all connections with weight above a threshold
     */
    @Query("SELECT nc FROM NeuralConnection nc WHERE nc.weight >= :threshold")
    List<NeuralConnection> findStrongConnections(@Param("threshold") Double threshold);

    /**
     * Find all connections by type
     */
    List<NeuralConnection> findByConnectionType(NeuralConnection.ConnectionType connectionType);

    /**
     * Get average connection weight across all connections
     */
    @Query("SELECT AVG(nc.weight) FROM NeuralConnection nc")
    Double getAverageConnectionWeight();

    /**
     * Count total number of connections
     */
    @Query("SELECT COUNT(nc) FROM NeuralConnection nc")
    Long getTotalConnectionCount();
}
