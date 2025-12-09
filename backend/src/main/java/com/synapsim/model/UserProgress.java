package com.synapsim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks user progress, level, experience, and achievements
 */
@Entity
@Table(name = "user_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private String userId; // For future authentication; currently using session/local storage

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Integer experience = 0;

    @ElementCollection
    @CollectionTable(name = "completed_quests", joinColumns = @JoinColumn(name = "user_progress_id"))
    @Column(name = "quest_id")
    private List<String> completedQuests = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "earned_badges", joinColumns = @JoinColumn(name = "user_progress_id"))
    @Column(name = "badge_name")
    private List<String> badges = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Onboarding data
    @Column(name = "baseline_mood")
    private Integer baselineMood;

    @Enumerated(EnumType.STRING)
    @Column(name = "focus_area")
    private FocusArea focusArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style")
    private LearningStyle learningStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_intensity")
    private GoalIntensity goalIntensity;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FocusArea {
        ANXIETY, CREATIVITY, DEPRESSION, EMPATHY, GENERAL
    }

    public enum ExperienceLevel {
        NONE, BASIC, INTERMEDIATE, ADVANCED
    }

    public enum LearningStyle {
        VISUAL, READING, INTERACTIVE, MIXED
    }

    public enum GoalIntensity {
        SIMPLE, MODERATE, DETAILED
    }
}
