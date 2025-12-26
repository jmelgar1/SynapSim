<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import PrimaryButton from '@/components/common/PrimaryButton.vue'
import { simulationService } from '@/services/simulation'
import {
  COMPOUND_INSPIRATIONS,
  THERAPEUTIC_SETTINGS,
  BRAIN_REGIONS,
  SIMULATION_DURATIONS,
  QUESTS,
} from '@/constants'

const router = useRouter()
const route = useRoute()

// Get quest info from route params
const questId = route.params.questId
const quest = QUESTS.find((q) => q.id === questId)

// Scenario parameters
const scenario = ref({
  compoundInspiration: null,
  therapeuticSetting: null,
  primaryBrainRegion: null,
  simulationDuration: null,
})

// Loading state
const isSubmitting = ref(false)
const errorMessage = ref(null)

// Validation
const isFormValid = computed(() => {
  return (
    scenario.value.compoundInspiration &&
    scenario.value.therapeuticSetting &&
    scenario.value.primaryBrainRegion &&
    scenario.value.simulationDuration
  )
})

// Tooltip visibility
const activeTooltip = ref(null)

const showTooltip = (field) => {
  activeTooltip.value = field
}

const hideTooltip = () => {
  activeTooltip.value = null
}

const handleSubmit = async () => {
  if (!isFormValid.value) return

  isSubmitting.value = true
  errorMessage.value = null

  try {
    // Prepare request payload matching backend ScenarioRequest
    const requestData = {
      questId: questId,
      compoundInspiration: scenario.value.compoundInspiration,
      therapeuticSetting: scenario.value.therapeuticSetting,
      primaryBrainRegion: scenario.value.primaryBrainRegion,
      simulationDuration: scenario.value.simulationDuration,
      integrationSteps: null, // Optional field
    }

    console.log('Submitting simulation request:', requestData)

    // Call backend API
    const response = await simulationService.createSimulation(requestData)

    console.log('Simulation created successfully:', response)

    // Save scenario to localStorage for history
    const savedScenarios = JSON.parse(localStorage.getItem('synapSimScenarios') || '[]')
    savedScenarios.push({
      questId,
      scenario: scenario.value,
      simulationId: response.id,
      createdAt: new Date().toISOString(),
    })
    localStorage.setItem('synapSimScenarios', JSON.stringify(savedScenarios))

    // Navigate to simulation results view
    router.push(`/simulation/${response.id}`)
  } catch (error) {
    console.error('Error creating simulation:', error)
    errorMessage.value = error.response?.data?.message || error.message || 'Failed to create simulation. Please ensure the backend is running.'
  } finally {
    isSubmitting.value = false
  }
}

const goBack = () => {
  router.push('/dashboard')
}
</script>

<template>
  <div class="scenario-builder-container">
    <div class="scenario-builder-content">
      <!-- Header -->
      <header class="builder-header">
        <button class="back-button" @click="goBack">← Back to Dashboard</button>
        <div class="quest-info" v-if="quest">
          <span class="quest-icon">{{ quest.icon }}</span>
          <div class="quest-details">
            <h1 class="quest-title">{{ quest.title }}</h1>
            <p class="quest-description">{{ quest.description }}</p>
          </div>
        </div>
      </header>

      <!-- Form -->
      <div class="builder-form">
        <h2 class="form-title">Build Your Scenario</h2>
        <p class="form-subtitle">
          Customize the parameters below to create a personalized neural pathway simulation
        </p>

        <!-- Compound Inspiration -->
        <div class="form-group">
          <label class="form-label">
            Compound Inspiration
            <span class="info-wrapper" @mouseenter="showTooltip('compound')" @mouseleave="hideTooltip">
              <span class="info-icon">ⓘ</span>
              <div v-if="activeTooltip === 'compound'" class="tooltip">
                <span>Each compound affects brain networks differently. Psilocybin reduces Default Mode Network activity (ego dissolution), LSD enhances sensory integration, Ketamine rapidly rewires mood circuits, and MDMA strengthens empathy pathways. Choose based on which neural changes interest you most.</span>
              </div>
            </span>
          </label>
          <div class="options-grid">
            <div
              v-for="option in COMPOUND_INSPIRATIONS"
              :key="option.value"
              class="option-card"
              :class="{ selected: scenario.compoundInspiration === option.value }"
              @click="scenario.compoundInspiration = option.value"
            >
              <div class="option-header">
                <h4>{{ option.label }}</h4>
              </div>
              <p class="option-description">{{ option.description }}</p>
            </div>
          </div>
        </div>

        <!-- Therapeutic Setting -->
        <div class="form-group">
          <label class="form-label">
            Therapeutic Setting
            <span class="info-wrapper" @mouseenter="showTooltip('setting')" @mouseleave="hideTooltip">
              <span class="info-icon">ⓘ</span>
              <div v-if="activeTooltip === 'setting'" class="tooltip">
                <span>Set and setting matter! Calm environments enhance stress reduction pathways, guided therapy strengthens emotional processing circuits, meditation spaces boost introspective networks, creative settings amplify divergent thinking, and social contexts enhance empathy connections.</span>
              </div>
            </span>
          </label>
          <div class="options-grid">
            <div
              v-for="option in THERAPEUTIC_SETTINGS"
              :key="option.value"
              class="option-card setting-card"
              :class="{ selected: scenario.therapeuticSetting === option.value }"
              @click="scenario.therapeuticSetting = option.value"
            >
              <div class="setting-icon">{{ option.icon }}</div>
              <h4>{{ option.label }}</h4>
              <p class="option-description">{{ option.description }}</p>
            </div>
          </div>
        </div>

        <!-- Primary Brain Region -->
        <div class="form-group">
          <label class="form-label">
            Primary Brain Region
            <span class="info-wrapper" @mouseenter="showTooltip('brain')" @mouseleave="hideTooltip">
              <span class="info-icon">ⓘ</span>
              <div v-if="activeTooltip === 'brain'" class="tooltip">
                <span>Focus on a specific brain region to see targeted effects. The Amygdala processes fear/anxiety, Prefrontal Cortex handles decision-making, Hippocampus manages memory, and the Default Mode Network controls self-referential thinking. This helps personalize the simulation to your interests.</span>
              </div>
            </span>
          </label>
          <select v-model="scenario.primaryBrainRegion" class="form-select">
            <option :value="null" disabled>Select a brain region</option>
            <option v-for="region in BRAIN_REGIONS" :key="region.value" :value="region.value">
              {{ region.label }} - {{ region.description }}
            </option>
          </select>
        </div>

        <!-- Simulation Duration -->
        <div class="form-group">
          <label class="form-label">
            Simulation Duration
            <span class="info-wrapper" @mouseenter="showTooltip('duration')" @mouseleave="hideTooltip">
              <span class="info-icon">ⓘ</span>
              <div v-if="activeTooltip === 'duration'" class="tooltip">
                <span>Duration affects neuroplastic depth. Short sessions (3-5 hours) show acute connectivity changes, Medium (6-8 hours) allows for deeper network reorganization, and Extended (8+ hours) simulates profound rewiring with lasting integration potential. Longer isn't always better—it depends on your learning goals.</span>
              </div>
            </span>
          </label>
          <div class="duration-options">
            <div
              v-for="duration in SIMULATION_DURATIONS"
              :key="duration.value"
              class="duration-card"
              :class="{ selected: scenario.simulationDuration === duration.value }"
              @click="scenario.simulationDuration = duration.value"
            >
              <h4>{{ duration.label }}</h4>
              <p>{{ duration.description }}</p>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="error-message">
          <span class="error-icon">⚠️</span>
          <p>{{ errorMessage }}</p>
        </div>

        <!-- Submit Button -->
        <div class="form-actions">
          <PrimaryButton
            :disabled="!isFormValid || isSubmitting"
            @click="handleSubmit"
            class="forge-button"
          >
            <span v-if="isSubmitting">⏳ Processing...</span>
            <span v-else>🧠 Forge Pathway</span>
          </PrimaryButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.scenario-builder-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  padding: 2rem;
}

.scenario-builder-content {
  max-width: 900px;
  margin: 0 auto;
}

/* Header */
.builder-header {
  margin-bottom: 2rem;
}

.back-button {
  padding: 0.75rem 1.5rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text);
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.2s ease;
  margin-bottom: 1.5rem;
}

.back-button:hover {
  border-color: #667eea;
  transform: translateX(-4px);
}

.quest-info {
  display: flex;
  gap: 1.5rem;
  padding: 2rem;
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.quest-icon {
  font-size: 4rem;
  flex-shrink: 0;
}

.quest-details {
  flex: 1;
}

.quest-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.quest-description {
  margin: 0;
  color: var(--color-text);
  line-height: 1.6;
}

/* Form */
.builder-form {
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.form-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: var(--color-heading);
}

.form-subtitle {
  margin: 0 0 2rem 0;
  color: var(--color-text);
  font-size: 0.95rem;
}

.form-group {
  margin-bottom: 2.5rem;
}

.form-label {
  display: block;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 1rem;
  position: relative;
}

.info-wrapper {
  position: relative;
  display: inline-block;
  vertical-align: middle;
}

.info-icon {
  display: inline-block;
  width: 18px;
  height: 18px;
  background: var(--color-background-soft);
  border-radius: 50%;
  text-align: center;
  line-height: 18px;
  font-size: 0.75rem;
  margin-left: 0.5rem;
  cursor: help;
  color: #667eea;
}

.tooltip {
  position: absolute;
  left: 0;
  top: 100%;
  margin-top: 0.5rem;
  padding: 0.875rem 1.125rem;
  background: #2c3e50;
  color: #ffffff;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 400;
  line-height: 1.5;
  z-index: 1000;
  max-width: 400px;
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  white-space: normal;
  word-wrap: break-word;
  overflow: visible;
}

.tooltip::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 20px;
  width: 12px;
  height: 12px;
  background: #2c3e50;
  transform: rotate(45deg);
}

/* Options Grid */
.options-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.option-card {
  padding: 1.5rem;
  border: 2px solid var(--color-border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--color-background-soft);
}

.option-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.option-card.selected {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.option-header h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

.option-description {
  margin: 0;
  font-size: 0.85rem;
  color: var(--color-text);
  line-height: 1.5;
}

.setting-card {
  text-align: center;
}

.setting-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
}

.setting-card h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
}

/* Select */
.form-select {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-background);
  color: var(--color-text);
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-select:hover,
.form-select:focus {
  border-color: #667eea;
  outline: none;
}

/* Duration Options */
.duration-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.duration-card {
  padding: 1.25rem;
  border: 2px solid var(--color-border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  background: var(--color-background-soft);
}

.duration-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.duration-card.selected {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.duration-card h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-heading);
}

.duration-card p {
  margin: 0;
  font-size: 0.8rem;
  color: var(--color-text);
}

/* Textarea */
.form-textarea {
  width: 100%;
  padding: 1rem;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-background);
  color: var(--color-text);
  font-size: 0.95rem;
  font-family: inherit;
  resize: vertical;
  transition: all 0.2s ease;
}

.form-textarea:hover,
.form-textarea:focus {
  border-color: #667eea;
  outline: none;
}

/* Error Message */
.error-message {
  background: rgba(255, 107, 107, 0.1);
  border: 2px solid #e74c3c;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  margin-top: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.error-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.error-message p {
  margin: 0;
  color: #e74c3c;
  font-weight: 500;
  line-height: 1.5;
}

/* Actions */
.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid var(--color-border);
}

.forge-button {
  font-size: 1.1rem;
  padding: 1rem 3rem;
}

.forge-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .scenario-builder-container {
    padding: 1rem;
  }

  .builder-form {
    padding: 1.5rem;
  }

  .quest-info {
    flex-direction: column;
    text-align: center;
  }

  .quest-icon {
    font-size: 3rem;
  }

  .options-grid {
    grid-template-columns: 1fr;
  }
}
</style>
