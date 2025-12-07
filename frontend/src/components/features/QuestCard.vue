<script setup>
import { computed } from 'vue'

const props = defineProps({
  quest: {
    type: Object,
    required: true,
  },
  completed: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['select'])

const difficultyColor = computed(() => {
  switch (props.quest.difficulty) {
    case 'beginner':
      return '#4ade80'
    case 'intermediate':
      return '#fbbf24'
    case 'advanced':
      return '#f87171'
    default:
      return '#667eea'
  }
})

const handleSelect = () => {
  emit('select', props.quest.id)
}
</script>

<template>
  <div class="quest-card" :class="{ completed }" @click="handleSelect">
    <div class="quest-icon">{{ quest.icon }}</div>

    <div class="quest-content">
      <div class="quest-header">
        <h3 class="quest-title">{{ quest.title }}</h3>
        <span class="quest-difficulty" :style="{ color: difficultyColor }">
          {{ quest.difficulty }}
        </span>
      </div>

      <p class="quest-description">{{ quest.description }}</p>

      <div class="quest-footer">
        <div class="quest-badge">
          <span class="badge-icon">🏆</span>
          <span class="badge-text">{{ quest.badge }}</span>
        </div>

        <div v-if="completed" class="completed-badge">
          ✓ Completed
        </div>
        <button v-else class="quest-action">
          Start Quest →
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quest-card {
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  gap: 1.25rem;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.quest-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.quest-card:hover {
  border-color: #667eea;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
}

.quest-card:hover::before {
  opacity: 1;
}

.quest-card.completed {
  opacity: 0.7;
  border-color: #4ade80;
  background: rgba(74, 222, 128, 0.05);
}

.quest-card.completed::before {
  background: #4ade80;
  opacity: 1;
}

.quest-icon {
  font-size: 3rem;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: var(--color-background-soft);
  border-radius: 12px;
  transition: transform 0.3s ease;
}

.quest-card:hover .quest-icon {
  transform: scale(1.1);
}

.quest-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.quest-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.quest-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-heading);
}

.quest-difficulty {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 0.25rem 0.75rem;
  background: var(--color-background-soft);
  border-radius: 12px;
  white-space: nowrap;
}

.quest-description {
  margin: 0;
  color: var(--color-text);
  line-height: 1.6;
  font-size: 0.95rem;
}

.quest-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-top: auto;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}

.quest-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--color-text);
}

.badge-icon {
  font-size: 1rem;
}

.badge-text {
  font-style: italic;
}

.completed-badge {
  color: #4ade80;
  font-weight: 600;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.quest-action {
  padding: 0.5rem 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quest-action:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .quest-card {
    flex-direction: column;
    text-align: center;
  }

  .quest-icon {
    width: 60px;
    height: 60px;
    font-size: 2rem;
    margin: 0 auto;
  }

  .quest-header {
    flex-direction: column;
    align-items: center;
  }

  .quest-footer {
    flex-direction: column;
    gap: 0.75rem;
  }

  .quest-action {
    width: 100%;
  }
}
</style>
