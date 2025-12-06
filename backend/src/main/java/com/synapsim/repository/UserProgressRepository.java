package com.synapsim.repository;

import com.synapsim.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserProgress entity
 */
@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    /**
     * Find user progress by user ID
     */
    Optional<UserProgress> findByUserId(String userId);

    /**
     * Check if user progress exists for a user ID
     */
    boolean existsByUserId(String userId);

    /**
     * Find all users by level
     */
    List<UserProgress> findByLevel(Integer level);

    /**
     * Find users with experience above a threshold
     */
    @Query("SELECT up FROM UserProgress up WHERE up.experience >= :threshold ORDER BY up.experience DESC")
    List<UserProgress> findTopPerformers(@Param("threshold") Integer threshold);

    /**
     * Find users by focus area
     */
    List<UserProgress> findByFocusArea(UserProgress.FocusArea focusArea);

    /**
     * Find users who completed a specific quest
     */
    @Query("SELECT up FROM UserProgress up WHERE :questId MEMBER OF up.completedQuests")
    List<UserProgress> findUsersWhoCompletedQuest(@Param("questId") String questId);

    /**
     * Find users who earned a specific badge
     */
    @Query("SELECT up FROM UserProgress up WHERE :badgeName MEMBER OF up.badges")
    List<UserProgress> findUsersWithBadge(@Param("badgeName") String badgeName);

    /**
     * Get leaderboard (ordered by level desc, then experience desc)
     */
    @Query("SELECT up FROM UserProgress up ORDER BY up.level DESC, up.experience DESC")
    List<UserProgress> getLeaderboard();

    /**
     * Count total number of completed quests across all users
     */
    @Query("SELECT SUM(SIZE(up.completedQuests)) FROM UserProgress up")
    Long getTotalCompletedQuestsCount();

    /**
     * Count total number of badges earned across all users
     */
    @Query("SELECT SUM(SIZE(up.badges)) FROM UserProgress up")
    Long getTotalBadgesEarnedCount();
}
