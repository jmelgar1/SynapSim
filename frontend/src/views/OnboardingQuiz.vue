<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import PrimaryButton from '@/components/common/PrimaryButton.vue'

const router = useRouter()

const currentQuestion = ref(0)
const answers = ref({
  mood: null,
  focusArea: null,
  experience: null,
  learningStyle: null,
  goalIntensity: null,
})

const questions = [
  {
    id: 'mood',
    question: 'How would you rate your baseline mood today?',
    type: 'scale',
    min: 1,
    max: 10,
    labels: { 1: 'Very Low', 10: 'Very High' },
  },
  {
    id: 'focusArea',
    question: 'What area would you like to focus on?',
    type: 'multiple-choice',
    options: [
      { value: 'anxiety', label: 'Anxiety & Stress Relief' },
      { value: 'creativity', label: 'Creativity & Problem Solving' },
      { value: 'depression', label: 'Depression & Mood Enhancement' },
      { value: 'empathy', label: 'Empathy & Social Connection' },
      { value: 'general', label: 'General Brain Health' },
    ],
  },
  {
    id: 'experience',
    question: 'How familiar are you with psychedelic research?',
    type: 'multiple-choice',
    options: [
      { value: 'none', label: 'No knowledge - completely new to this' },
      { value: 'basic', label: 'Basic understanding from articles/videos' },
      { value: 'intermediate', label: 'Moderate - read several studies' },
      { value: 'advanced', label: 'Advanced - actively follow research' },
    ],
  },
  {
    id: 'learningStyle',
    question: 'How do you prefer to learn?',
    type: 'multiple-choice',
    options: [
      { value: 'visual', label: 'Visual (graphs, diagrams, animations)' },
      { value: 'reading', label: 'Reading (text descriptions, studies)' },
      { value: 'interactive', label: 'Interactive (hands-on exploration)' },
      { value: 'mixed', label: 'Mixed approach' },
    ],
  },
  {
    id: 'goalIntensity',
    question: 'What level of detail interests you most?',
    type: 'multiple-choice',
    options: [
      { value: 'simple', label: 'Simple overview - keep it accessible' },
      { value: 'moderate', label: 'Moderate detail - balanced approach' },
      { value: 'detailed', label: 'Deep dive - scientific details please' },
    ],
  },
]

const progress = computed(() => {
  return ((currentQuestion.value + 1) / questions.length) * 100
})

const currentQuestionData = computed(() => {
  return questions[currentQuestion.value]
})

const canProceed = computed(() => {
  const answer = answers.value[currentQuestionData.value.id]
  return answer !== null && answer !== undefined
})

const selectOption = (value) => {
  answers.value[currentQuestionData.value.id] = value
}

const selectScale = (value) => {
  answers.value[currentQuestionData.value.id] = value
}

const nextQuestion = () => {
  if (currentQuestion.value < questions.length - 1) {
    currentQuestion.value++
  } else {
    completeQuiz()
  }
}

const previousQuestion = () => {
  if (currentQuestion.value > 0) {
    currentQuestion.value--
  }
}

const completeQuiz = () => {
  // Save to local storage
  localStorage.setItem('synapSimOnboarding', JSON.stringify({
    answers: answers.value,
    completedAt: new Date().toISOString(),
  }))

  // Navigate to dashboard
  router.push('/dashboard')
}
</script>

<template>
  <div class="onboarding-container">
    <div class="onboarding-card">
      <!-- Header -->
      <div class="quiz-header">
        <h1>Personalize Your Experience</h1>
        <p class="quiz-subtitle">Answer a few quick questions to tailor your SynapSim journey</p>

        <!-- Progress Bar -->
        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <span class="progress-text">Question {{ currentQuestion + 1 }} of {{ questions.length }}</span>
        </div>
      </div>

      <!-- Question Content -->
      <div class="question-content">
        <h2 class="question-text">{{ currentQuestionData.question }}</h2>

        <!-- Scale Type Question -->
        <div v-if="currentQuestionData.type === 'scale'" class="scale-container">
          <div class="scale-labels">
            <span>{{ currentQuestionData.labels[currentQuestionData.min] }}</span>
            <span>{{ currentQuestionData.labels[currentQuestionData.max] }}</span>
          </div>
          <div class="scale-options">
            <button
              v-for="n in (currentQuestionData.max - currentQuestionData.min + 1)"
              :key="n"
              class="scale-button"
              :class="{ active: answers[currentQuestionData.id] === (n + currentQuestionData.min - 1) }"
              @click="selectScale(n + currentQuestionData.min - 1)"
            >
              {{ n + currentQuestionData.min - 1 }}
            </button>
          </div>
        </div>

        <!-- Multiple Choice Question -->
        <div v-if="currentQuestionData.type === 'multiple-choice'" class="options-container">
          <button
            v-for="option in currentQuestionData.options"
            :key="option.value"
            class="option-button"
            :class="{ active: answers[currentQuestionData.id] === option.value }"
            @click="selectOption(option.value)"
          >
            <span class="option-check">✓</span>
            {{ option.label }}
          </button>
        </div>
      </div>

      <!-- Navigation -->
      <div class="quiz-navigation">
        <button
          class="nav-button secondary"
          :disabled="currentQuestion === 0"
          @click="previousQuestion"
        >
          ← Previous
        </button>

        <PrimaryButton
          :disabled="!canProceed"
          @click="nextQuestion"
        >
          {{ currentQuestion === questions.length - 1 ? 'Complete' : 'Next →' }}
        </PrimaryButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.onboarding-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.onboarding-card {
  background: var(--color-background);
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  max-width: 700px;
  width: 100%;
  padding: 3rem;
  animation: slideUp 0.5s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Header */
.quiz-header {
  margin-bottom: 3rem;
  text-align: center;
}

.quiz-header h1 {
  font-size: 2rem;
  margin: 0 0 0.5rem 0;
  color: var(--color-heading);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.quiz-subtitle {
  color: var(--color-text);
  margin: 0 0 2rem 0;
  font-size: 1rem;
}

/* Progress Bar */
.progress-container {
  margin-top: 2rem;
}

.progress-bar {
  height: 8px;
  background: var(--color-border);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
  border-radius: 4px;
}

.progress-text {
  font-size: 0.9rem;
  color: var(--color-text);
}

/* Question Content */
.question-content {
  margin-bottom: 3rem;
  min-height: 300px;
}

.question-text {
  font-size: 1.5rem;
  color: var(--color-heading);
  margin: 0 0 2rem 0;
  text-align: center;
  font-weight: 600;
}

/* Scale Options */
.scale-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.scale-labels {
  display: flex;
  justify-content: space-between;
  color: var(--color-text);
  font-size: 0.9rem;
  font-weight: 500;
}

.scale-options {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.scale-button {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  border: 2px solid var(--color-border);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.scale-button:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.scale-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: scale(1.1);
}

/* Multiple Choice Options */
.options-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.option-button {
  padding: 1.25rem 1.5rem;
  border-radius: 12px;
  border: 2px solid var(--color-border);
  background: var(--color-background);
  color: var(--color-text);
  font-size: 1rem;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  padding-left: 3.5rem;
}

.option-check {
  position: absolute;
  left: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  color: transparent;
  transition: all 0.2s ease;
}

.option-button:hover {
  border-color: #667eea;
  transform: translateX(4px);
}

.option-button.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.option-button.active .option-check {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}

/* Navigation */
.quiz-navigation {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  padding-top: 2rem;
  border-top: 1px solid var(--color-border);
}

.nav-button {
  padding: 0.875rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.nav-button.secondary {
  background: var(--color-background-soft);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.nav-button.secondary:hover:not(:disabled) {
  background: var(--color-background-mute);
  transform: translateX(-2px);
}

.nav-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .onboarding-card {
    padding: 2rem 1.5rem;
  }

  .quiz-header h1 {
    font-size: 1.5rem;
  }

  .question-text {
    font-size: 1.25rem;
  }

  .scale-options {
    flex-wrap: wrap;
    gap: 0.75rem;
  }

  .scale-button {
    width: 45px;
    height: 45px;
  }

  .quiz-navigation {
    flex-direction: column-reverse;
  }

  .nav-button {
    width: 100%;
  }
}
</style>
