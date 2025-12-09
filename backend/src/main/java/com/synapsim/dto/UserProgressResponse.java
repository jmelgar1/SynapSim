package com.synapsim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for user progress information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressResponse {

    private Long id;
    private String userId;
    private Integer level;
    private String levelTitle;
    private Integer experience;
    private Integer experienceToNextLevel;
    private Double experienceProgress;
    private List<String> completedQuests;
    private List<String> badges;
    private String focusArea;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
