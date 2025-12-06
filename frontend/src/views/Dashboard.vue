<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserProgress } from '@/composables/useUserProgress'
import QuestCard from '@/components/features/QuestCard.vue'
import { QUESTS } from '@/constants'

const router = useRouter()
const { level, levelTitle, experienceProgress, focusArea, isQuestCompleted } = useUserProgress()

// Personalized greeting based on focus area
const greetingMessage = computed(() => {
  const focusMessages = {
    anxiety: 'Ready to explore pathways for calm and balance?',
    creativity: 'Let\'s unlock your creative potential!',
    depression: 'Begin your journey toward brighter neural pathways.',
    empathy: 'Discover the science of connection and understanding.',
    general: 'Ready to explore the fascinating world of neuroplasticity?',
  }
  return focusMessages[focusArea.value] || focusMessages.general
})

// Sort quests to prioritize user's focus area
const sortedQuests = computed(() => {
  return [...QUESTS].sort((a, b) => {
    const aMatches = a.focusAreas.includes(focusArea.value)
    const bMatches = b.focusAreas.includes(focusArea.value)

    if (aMatches && !bMatches) return -1
    if (!aMatches && bMatches) return 1

    // Then sort by difficulty
    const difficultyOrder = { beginner: 1, intermediate: 2, advanced: 3 }
    return difficultyOrder[a.difficulty] - difficultyOrder[b.difficulty]
  })
})

const handleQuestSelect = (questId) => {
  router.push(`/quest/${questId}`)
}
</script>

<template>
  <div class="dashboard-container">
    <div class="dashboard-content">
      <!-- Header Section -->
      <header class="dashboard-header">
        <div class="header-content">
          <h1 class="dashboard-title">Your Brain Forge</h1>
          <p class="dashboard-subtitle">{{ greetingMessage }}</p>
        </div>

        <!-- Progress Card -->
        <div class="progress-card">
          <div class="progress-info">
            <div class="level-badge">
              <span class="level-icon">⚡</span>
              <div class="level-text">
                <span class="level-label">Brain Forge Level</span>
                <span class="level-value">{{ levelTitle }}</span>
              </div>
            </div>
          </div>

          <div class="experience-bar">
            <div class="experience-fill" :style="{ width: experienceProgress + '%' }"></div>
          </div>
          <p class="experience-text">Level {{ level }} Progress</p>
        </div>
      </header>

      <!-- Quests Section -->
      <section class="quests-section">
        <div class="section-header">
          <h2 class="section-title">Available Quests</h2>
          <p class="section-subtitle">
            Complete simulations to earn badges and unlock new pathways
          </p>
        </div>

        <div class="quests-grid">
          <QuestCard
            v-for="quest in sortedQuests"
            :key="quest.id"
            :quest="quest"
            :completed="isQuestCompleted(quest.id)"
            @select="handleQuestSelect"
          />
        </div>
      </section>

      <!-- Empty State (if needed later)
      <div v-if="sortedQuests.length === 0" class="empty-state">
        <p>No quests available yet. Check back soon!</p>
      </div>
      -->
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  padding: 2rem;
}

.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* Header Section */
.dashboard-header {
  margin-bottom: 3rem;
}

.header-content {
  margin-bottom: 2rem;
}

.dashboard-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0 0 0.5rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: fadeInDown 0.6s ease;
}

.dashboard-subtitle {
  font-size: 1.25rem;
  color: var(--color-text);
  margin: 0;
  animation: fadeIn 0.6s ease 0.2s both;
}

/* Progress Card */
.progress-card {
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  animation: fadeIn 0.6s ease 0.4s both;
}

.progress-info {
  margin-bottom: 1.5rem;
}

.level-badge {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.level-icon {
  font-size: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 12px;
}

.level-text {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.level-label {
  font-size: 0.85rem;
  color: var(--color-text);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.level-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-heading);
}

.experience-bar {
  height: 12px;
  background: var(--color-background-soft);
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.experience-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 6px;
  transition: width 0.6s ease;
}

.experience-text {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text);
  text-align: right;
}

/* Quests Section */
.quests-section {
  animation: fadeIn 0.6s ease 0.6s both;
}

.section-header {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: var(--color-heading);
}

.section-subtitle {
  font-size: 1rem;
  color: var(--color-text);
  margin: 0;
}

.quests-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Animations */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 1.5rem;
  }

  .dashboard-title {
    font-size: 2rem;
  }

  .dashboard-subtitle {
    font-size: 1rem;
  }

  .progress-card {
    padding: 1.5rem;
  }

  .level-badge {
    flex-direction: column;
    text-align: center;
  }

  .level-icon {
    width: 60px;
    height: 60px;
    font-size: 2.5rem;
  }

  .section-title {
    font-size: 1.5rem;
  }
}
</style>
