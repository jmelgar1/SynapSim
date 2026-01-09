<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import PrimaryButton from '@/components/common/PrimaryButton.vue'
import { simulationService } from '@/services/simulation'
import {
  COMPOUND_INSPIRATIONS,
  THERAPEUTIC_SETTINGS,
  RESEARCH_FOCUS,
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
  researchFocus: null, // Optional
})

// Loading state
const isSubmitting = ref(false)
const errorMessage = ref(null)
const showNoResearchModal = ref(false)

// Validation
const isFormValid = computed(() => {
  return (
    scenario.value.compoundInspiration &&
    scenario.value.therapeuticSetting
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
      researchFocus: scenario.value.researchFocus, // Optional
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

    // Check if it's a "no research found" error
    if (error.response?.data?.error === 'NO_RESEARCH_FOUND') {
      showNoResearchModal.value = true
    } else {
      errorMessage.value = error.response?.data?.message || error.message || 'Failed to create simulation. Please ensure the backend is running.'
    }
  } finally {
    isSubmitting.value = false
  }
}

const closeNoResearchModal = () => {
  showNoResearchModal.value = false
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
              class="option-card compound-card"
              :class="{ selected: scenario.compoundInspiration === option.value }"
              @click="scenario.compoundInspiration = option.value"
            >
              <div class="compound-icon">{{ option.icon }}</div>
              <h4>{{ option.label }}</h4>
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

        <!-- Research Focus (Optional) -->
        <div class="form-group">
          <label class="form-label">
            Research Focus
            <span class="optional-badge">Optional</span>
            <span class="info-wrapper" @mouseenter="showTooltip('focus')" @mouseleave="hideTooltip">
              <span class="info-icon">ⓘ</span>
              <div v-if="activeTooltip === 'focus'" class="tooltip">
                <span>Optionally focus your simulation on a specific therapeutic area. This will prioritize research articles and brain pathways related to your chosen focus. Leave unselected for a general exploration.</span>
              </div>
            </span>
          </label>
          <div class="options-grid focus-grid">
            <div
              v-for="option in RESEARCH_FOCUS"
              :key="option.value"
              class="option-card focus-card"
              :class="{ selected: scenario.researchFocus === option.value }"
              @click="scenario.researchFocus = scenario.researchFocus === option.value ? null : option.value"
            >
              <div class="focus-icon">{{ option.icon }}</div>
              <h4>{{ option.label }}</h4>
              <p class="option-description">{{ option.description }}</p>
            </div>
          </div>
          <p v-if="scenario.researchFocus" class="focus-hint">
            Click again to deselect
          </p>
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
            <span class="button-content">
              <span v-if="isSubmitting" class="spinner"></span>
              <span>{{ isSubmitting ? 'Processing...' : 'Create Simulation' }}</span>
            </span>
          </PrimaryButton>
        </div>
      </div>
    </div>

    <!-- No Research Found Modal -->
    <div v-if="showNoResearchModal" class="modal-overlay" @click="closeNoResearchModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>No Relevant Research Found</h2>
          <button class="modal-close" @click="closeNoResearchModal">×</button>
        </div>
        <div class="modal-body">
          <div class="modal-icon">🔬</div>
          <p class="modal-message">
            We couldn't find any relevant research articles for your selected combination of parameters.
          </p>
          <p class="modal-suggestion">
            Please try adjusting your compound inspiration or therapeutic setting to explore different pathways.
          </p>
        </div>
        <div class="modal-actions">
          <PrimaryButton @click="closeNoResearchModal">
            Try Different Parameters
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
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.option-card {
  padding: 1.5rem;
  border: 2px solid var(--color-border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--color-background-soft);
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.option-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.option-card.selected {
  /* Glass window pane effect */
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.25) 0%,
    rgba(255, 255, 255, 0.08) 50%,
    rgba(255, 255, 255, 0.15) 100%
  );
  border-radius: 12px;
  box-shadow:
    0 4px 30px rgba(0, 0, 0, 0.1),
    inset 0 0 20px rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  border: 2px solid rgba(255, 255, 255, 0.5);
  position: relative;
  overflow: hidden;
}

.option-card.selected > * {
  position: relative;
  z-index: 2;
}

/* Glass shine/reflection effect - diagonal slant */
.option-card.selected::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.35) 0%,
    rgba(255, 255, 255, 0.15) 45%,
    transparent 50%,
    transparent 100%
  );
  border-radius: 12px;
  pointer-events: none;
}

/* Corner light highlights */
.option-card.selected::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background:
    radial-gradient(ellipse at 0% 0%, rgba(255, 255, 255, 0.4) 0%, transparent 50%),
    radial-gradient(ellipse at 100% 0%, rgba(255, 255, 255, 0.3) 0%, transparent 50%),
    radial-gradient(ellipse at 0% 100%, rgba(255, 255, 255, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at 100% 100%, rgba(255, 255, 255, 0.2) 0%, transparent 50%);
  border-radius: 12px;
  pointer-events: none;
  z-index: 1;
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

/* Compound Cards */
.compound-card {
  text-align: center;
  min-height: 140px;
}

.compound-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.compound-card h4 {
  margin: 0 0 0.5rem 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-heading);
}

/* Setting Cards */
.setting-card {
  text-align: center;
  min-height: 140px;
}

.setting-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.setting-card h4 {
  margin: 0 0 0.5rem 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-heading);
}

/* Research Focus Cards */
.optional-badge {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  background: var(--color-background-soft);
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
  color: var(--color-text);
  margin-left: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.focus-grid {
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}

.focus-card {
  text-align: center;
  min-height: 140px;
}

.focus-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.focus-card h4 {
  margin: 0 0 0.5rem 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-heading);
}

.focus-card .option-description {
  font-size: 0.8rem;
}

.focus-hint {
  margin: 0.75rem 0 0 0;
  font-size: 0.8rem;
  color: var(--color-text);
  font-style: italic;
  text-align: center;
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

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top: 3px solid white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--color-background);
  border-radius: 16px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--color-text);
}

.modal-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.modal-close:hover {
  background: var(--color-border);
  color: var(--color-text);
}

.modal-body {
  padding: 2rem;
  text-align: center;
}

.modal-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.modal-message {
  font-size: 1.1rem;
  color: var(--color-text);
  margin-bottom: 1rem;
  line-height: 1.6;
}

.modal-suggestion {
  font-size: 0.95rem;
  color: var(--color-text-secondary);
  margin-bottom: 0;
  line-height: 1.5;
}

.modal-actions {
  padding: 1.5rem 2rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: center;
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
