import { ref, computed } from 'vue'
import { useLocalStorage } from './useLocalStorage'

/**
 * Composable for managing user progress, quests, and badges
 */
export function useUserProgress() {
  const { data: onboardingData } = useLocalStorage('synapSimOnboarding', null)
  const { data: progressData, save: saveProgress } = useLocalStorage('synapSimProgress', {
    level: 1,
    experience: 0,
    completedQuests: [],
    badges: [],
  })

  // Level titles
  const levelTitles = {
    1: 'Beginner',
    2: 'Explorer',
    3: 'Practitioner',
    4: 'Expert',
    5: 'Master Forger',
  }

  // Calculate current level title
  const levelTitle = computed(() => {
    return levelTitles[progressData.value?.level || 1] || 'Beginner'
  })

  // Calculate experience progress (0-100)
  const experienceProgress = computed(() => {
    const exp = progressData.value?.experience || 0
    const levelThreshold = (progressData.value?.level || 1) * 100
    return Math.min((exp / levelThreshold) * 100, 100)
  })

  // Get user's focus area from onboarding
  const focusArea = computed(() => {
    return onboardingData.value?.answers?.focusArea || 'general'
  })

  // Check if a quest is completed
  const isQuestCompleted = (questId) => {
    return progressData.value?.completedQuests?.includes(questId) || false
  }

  // Mark a quest as completed
  const completeQuest = (questId, badgeName) => {
    const currentProgress = progressData.value || {
      level: 1,
      experience: 0,
      completedQuests: [],
      badges: [],
    }

    if (!currentProgress.completedQuests.includes(questId)) {
      currentProgress.completedQuests.push(questId)
      currentProgress.experience += 50

      // Add badge if provided
      if (badgeName && !currentProgress.badges.includes(badgeName)) {
        currentProgress.badges.push(badgeName)
      }

      // Level up check
      const levelThreshold = currentProgress.level * 100
      if (currentProgress.experience >= levelThreshold) {
        currentProgress.level += 1
        currentProgress.experience = 0
      }

      saveProgress(currentProgress)
    }
  }

  // Get all badges
  const badges = computed(() => {
    return progressData.value?.badges || []
  })

  // Reset progress (for testing or user request)
  const resetProgress = () => {
    saveProgress({
      level: 1,
      experience: 0,
      completedQuests: [],
      badges: [],
    })
  }

  return {
    level: computed(() => progressData.value?.level || 1),
    levelTitle,
    experience: computed(() => progressData.value?.experience || 0),
    experienceProgress,
    completedQuests: computed(() => progressData.value?.completedQuests || []),
    badges,
    focusArea,
    isQuestCompleted,
    completeQuest,
    resetProgress,
  }
}
