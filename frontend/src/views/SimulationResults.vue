<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { simulationService } from '@/services/simulation'
import PrimaryButton from '@/components/common/PrimaryButton.vue'
import BrainNetworkGraph from '@/components/features/BrainNetworkGraph.vue'

const route = useRoute()
const router = useRouter()

const simulationId = ref(route.params.id)
const simulation = ref(null)
const loading = ref(true)
const error = ref(null)
const expandedAbstracts = ref(new Set())
const expandedFullTexts = ref(new Set())

onMounted(async () => {
  await loadSimulation()
})

const loadSimulation = async () => {
  try {
    loading.value = true
    error.value = null

    const data = await simulationService.getSimulation(simulationId.value)
    simulation.value = data

    console.log('Simulation loaded:', data)
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to load simulation'
    console.error('Error loading simulation:', err)
  } finally {
    loading.value = false
  }
}

const goToDashboard = () => {
  router.push('/dashboard')
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString()
}

const toggleAbstract = (index) => {
  if (expandedAbstracts.value.has(index)) {
    expandedAbstracts.value.delete(index)
  } else {
    expandedAbstracts.value.add(index)
  }
  // Force reactivity update
  expandedAbstracts.value = new Set(expandedAbstracts.value)
}

const isExpanded = (index) => {
  return expandedAbstracts.value.has(index)
}

const getAbstractPreview = (abstractText, index) => {
  if (!abstractText) return ''
  if (isExpanded(index)) return abstractText
  return abstractText.length > 200 ? abstractText.substring(0, 200) + '...' : abstractText
}

const shouldShowExpander = (abstractText) => {
  return abstractText && abstractText.length > 200
}

const toggleFullText = (index) => {
  if (expandedFullTexts.value.has(index)) {
    expandedFullTexts.value.delete(index)
  } else {
    expandedFullTexts.value.add(index)
  }
  // Force reactivity update
  expandedFullTexts.value = new Set(expandedFullTexts.value)
}

const isFullTextExpanded = (index) => {
  return expandedFullTexts.value.has(index)
}

const getFullTextPreview = (fullText, index) => {
  if (!fullText) return ''
  if (isFullTextExpanded(index)) return fullText
  return fullText.length > 500 ? fullText.substring(0, 500) + '...' : fullText
}

const shouldShowFullTextExpander = (fullText) => {
  return fullText && fullText.length > 500
}
</script>

<template>
  <div class="results-container">
    <div class="results-content">
      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <h2>Processing your simulation...</h2>
        <p>Analyzing neural pathways and fetching research articles</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <h2>Error Loading Simulation</h2>
        <p>{{ error }}</p>
        <PrimaryButton @click="loadSimulation">Retry</PrimaryButton>
        <button class="secondary-button" @click="goToDashboard">Back to Dashboard</button>
      </div>

      <!-- Results -->
      <div v-else-if="simulation" class="results">
        <!-- Header -->
        <header class="results-header">
          <button class="back-button" @click="goToDashboard">← Back to Dashboard</button>
          <div class="header-content">
            <h1 class="results-title">Simulation Results</h1>
            <div class="header-meta">
              <span class="meta-item">ID: #{{ simulation.id }}</span>
              <span class="meta-item">{{ formatDate(simulation.executedAt) }}</span>
              <span class="meta-item">⚡ {{ simulation.processingTimeMs }}ms</span>
            </div>
          </div>
        </header>

        <!-- Success/Badge Section -->
        <div v-if="simulation.success" class="success-banner">
          <div class="badge-display">
            <span class="badge-icon">🏆</span>
            <div>
              <h3>Simulation Successful!</h3>
              <p v-if="simulation.badgeEarned">Badge Earned: {{ simulation.badgeEarned }}</p>
            </div>
          </div>
          <div class="confidence-score">
            <span class="score-label">Confidence Score</span>
            <span class="score-value">{{ (simulation.confidenceScore * 100).toFixed(1) }}%</span>
          </div>
        </div>

        <!-- Prediction Summary -->
        <div class="section prediction-section">
          <h2>Prediction Summary</h2>
          <p class="prediction-text">{{ simulation.predictionSummary }}</p>
        </div>

        <!-- Brain Network Visualization -->
        <div class="section network-section">
          <h2>Brain Network State</h2>
          <BrainNetworkGraph
            :networkState="simulation.networkState || { nodes: [], edges: [], metrics: {} }"
            :mentionedRegions="simulation.mentionedRegions || []"
          />
        </div>

        <!-- Brain Regions Mentioned in Research -->
        <div class="section regions-section">
          <h2>Brain Regions Mentioned in Research ({{ simulation.mentionedRegions?.length || 0 }})</h2>
          <div v-if="simulation.mentionedRegions && simulation.mentionedRegions.length > 0" class="regions-list">
            <div
              v-for="(region, index) in simulation.mentionedRegions"
              :key="index"
              class="region-item"
            >
              <div class="region-header">
                <h3 class="region-title">
                  <span class="region-code">{{ region.regionCode }}</span>
                  <span class="region-name-text">{{ region.regionName }}</span>
                </h3>
                <span class="mention-count">{{ region.mentions.length }} mention{{ region.mentions.length !== 1 ? 's' : '' }}</span>
              </div>

              <div class="region-mentions">
                <div
                  v-for="(mention, mIndex) in region.mentions"
                  :key="mIndex"
                  class="mention-item"
                >
                  <div class="mention-header">
                    <span class="context-badge" :class="`context-${mention.context}`">{{ mention.context }}</span>
                    <a :href="`https://pubmed.ncbi.nlm.nih.gov/${mention.pubmedId}/`" target="_blank" class="mention-article">
                      Study {{ mIndex + 1 }}
                    </a>
                  </div>
                  <p class="mention-excerpt">{{ mention.excerpt }}</p>
                </div>
              </div>
            </div>
          </div>
          <p v-else class="no-data">No brain regions mentioned in research</p>
        </div>

        <!-- PubMed References -->
        <div class="section references-section">
          <h2>Research References ({{ simulation.pubmedReferences?.length || 0 }})</h2>
          <div v-if="simulation.pubmedReferences && simulation.pubmedReferences.length > 0" class="references-list">
            <div
              v-for="(ref, index) in simulation.pubmedReferences"
              :key="index"
              class="reference-item"
            >
              <div class="reference-header">
                <a :href="ref.articleUrl" target="_blank" class="reference-title">
                  {{ ref.title }}
                </a>
                <span class="relevance-badge">{{ (ref.relevanceScore * 100).toFixed(0) }}% relevant</span>
              </div>
              <p class="reference-authors">{{ ref.authors }}</p>
              <div class="abstract-container">
                <h4 class="section-label">Abstract</h4>
                <p class="reference-abstract" :class="{ expanded: isExpanded(index) }">
                  {{ getAbstractPreview(ref.abstractText, index) }}
                </p>
                <button
                  v-if="shouldShowExpander(ref.abstractText)"
                  @click="toggleAbstract(index)"
                  class="expand-button"
                >
                  {{ isExpanded(index) ? 'Show less' : 'Show more' }}
                </button>
              </div>
              <div v-if="ref.fullText" class="full-text-container">
                <h4 class="section-label">Full Text</h4>
                <p class="reference-full-text" :class="{ expanded: isFullTextExpanded(index) }">
                  {{ getFullTextPreview(ref.fullText, index) }}
                </p>
                <button
                  v-if="shouldShowFullTextExpander(ref.fullText)"
                  @click="toggleFullText(index)"
                  class="expand-button"
                >
                  {{ isFullTextExpanded(index) ? 'Show less' : 'Show more' }}
                </button>
              </div>
            </div>
          </div>
          <p v-else class="no-data">No research references found</p>
        </div>

        <!-- Actions -->
        <div class="actions">
          <PrimaryButton @click="goToDashboard">Return to Dashboard</PrimaryButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.results-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  padding: 2rem;
}

.results-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--color-border);
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1.5rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state h2 {
  color: var(--color-heading);
  margin-bottom: 0.5rem;
}

.loading-state p {
  color: var(--color-text);
}

/* Error State */
.error-state {
  text-align: center;
  padding: 4rem 2rem;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.error-state h2 {
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.error-state p {
  color: var(--color-text);
  margin-bottom: 2rem;
}

.secondary-button {
  padding: 0.75rem 1.5rem;
  margin-top: 1rem;
  background: transparent;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text);
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.secondary-button:hover {
  border-color: #667eea;
  color: #667eea;
}

/* Header */
.results-header {
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

.header-content {
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.results-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 1rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-meta {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.meta-item {
  color: var(--color-text);
  font-size: 0.9rem;
}

/* Success Banner */
.success-banner {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border: 2px solid #667eea;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.badge-display {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.badge-icon {
  font-size: 3rem;
}

.badge-display h3 {
  margin: 0 0 0.25rem 0;
  color: var(--color-heading);
  font-size: 1.25rem;
}

.badge-display p {
  margin: 0;
  color: var(--color-text);
}

.confidence-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.score-label {
  font-size: 0.85rem;
  color: var(--color-text);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.score-value {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Sections */
.section {
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.section h2 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 1.5rem 0;
  color: var(--color-heading);
}

.prediction-text {
  line-height: 1.8;
  color: var(--color-text);
  font-size: 1rem;
}

/* Network Section - now contains BrainNetworkGraph component */
.network-section {
  padding: 1.5rem;
}

/* Brain Regions List */
.regions-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.region-item {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.5rem;
}

.region-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border);
}

.region-title {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  margin: 0;
  font-size: 1.1rem;
}

.region-code {
  font-weight: 700;
  color: #667eea;
  font-size: 1.2rem;
}

.region-name-text {
  font-weight: 500;
  color: var(--color-heading);
  font-size: 1rem;
}

.mention-count {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.region-mentions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.mention-item {
  background: var(--color-background);
  border-left: 3px solid #667eea;
  padding: 0.75rem;
  border-radius: 4px;
}

.mention-header {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.context-badge {
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.context-badge.context-connectivity {
  background: rgba(46, 213, 115, 0.2);
  color: #27ae60;
}

.context-badge.context-activity {
  background: rgba(255, 193, 7, 0.2);
  color: #f39c12;
}

.context-badge.context-neuroplasticity {
  background: rgba(156, 39, 176, 0.2);
  color: #9c27b0;
}

.context-badge.context-structure {
  background: rgba(33, 150, 243, 0.2);
  color: #2196f3;
}

.context-badge.context-function {
  background: rgba(255, 152, 0, 0.2);
  color: #ff9800;
}

.context-badge.context-general {
  background: rgba(158, 158, 158, 0.2);
  color: #757575;
}

.mention-article {
  color: #667eea;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 600;
}

.mention-article:hover {
  text-decoration: underline;
}

.mention-excerpt {
  color: var(--color-text);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0;
  font-style: italic;
}

/* References */
.references-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.reference-item {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.5rem;
}

.reference-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.reference-title {
  color: #667eea;
  font-weight: 600;
  font-size: 1rem;
  text-decoration: none;
  flex: 1;
}

.reference-title:hover {
  text-decoration: underline;
}

.relevance-badge {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.reference-authors {
  color: var(--color-text);
  font-size: 0.9rem;
  margin: 0 0 0.5rem 0;
  font-style: italic;
}

.section-label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--color-heading);
  margin: 1rem 0 0.5rem 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.abstract-container {
  position: relative;
}

.reference-abstract {
  color: var(--color-text);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 0.5rem 0;
  transition: max-height 0.3s ease;
}

.reference-abstract.expanded {
  white-space: pre-line;
}

.full-text-container {
  position: relative;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
}

.reference-full-text {
  color: var(--color-text);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 0.5rem 0;
  transition: max-height 0.3s ease;
  white-space: pre-line;
}

.reference-full-text.expanded {
  white-space: pre-line;
}

.expand-button {
  background: none;
  border: none;
  color: #667eea;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  padding: 0.25rem 0;
  margin-top: 0.25rem;
  text-decoration: none;
  transition: all 0.2s;
}

.expand-button:hover {
  color: #764ba2;
  text-decoration: underline;
}

.expand-button:focus {
  outline: 2px solid #667eea;
  outline-offset: 2px;
  border-radius: 2px;
}

.no-data {
  color: var(--color-text);
  font-style: italic;
  text-align: center;
  padding: 2rem;
}

/* Actions */
.actions {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

@media (max-width: 768px) {
  .results-container {
    padding: 1rem;
  }

  .section {
    padding: 1.5rem;
  }

  .success-banner {
    flex-direction: column;
    align-items: flex-start;
  }


  .reference-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
