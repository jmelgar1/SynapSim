<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { simulationService } from '@/services/simulation'
import PrimaryButton from '@/components/common/PrimaryButton.vue'

const route = useRoute()
const router = useRouter()

const simulationId = ref(route.params.id)
const simulation = ref(null)
const loading = ref(true)
const error = ref(null)

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

        <!-- Network State (Simple Display) -->
        <div class="section network-section">
          <h2>Brain Network State</h2>
          <div class="network-stats">
            <div class="stat-card">
              <span class="stat-label">Total Nodes</span>
              <span class="stat-value">{{ simulation.networkState?.metrics?.totalNodes || 0 }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Total Connections</span>
              <span class="stat-value">{{ simulation.networkState?.metrics?.totalConnections || 0 }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Network Density</span>
              <span class="stat-value">{{ (simulation.networkState?.metrics?.networkDensity * 100 || 0).toFixed(1) }}%</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Avg Connection Strength</span>
              <span class="stat-value">{{ (simulation.networkState?.metrics?.averageConnectionStrength || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- Connection Changes -->
        <div class="section changes-section">
          <h2>Connection Changes ({{ simulation.connectionChanges?.length || 0 }})</h2>
          <div v-if="simulation.connectionChanges && simulation.connectionChanges.length > 0" class="changes-list">
            <div
              v-for="(change, index) in simulation.connectionChanges.slice(0, 10)"
              :key="index"
              class="change-item"
            >
              <div class="change-header">
                <span class="region-name">{{ change.sourceRegion }}</span>
                <span class="arrow">→</span>
                <span class="region-name">{{ change.targetRegion }}</span>
              </div>
              <div class="change-details">
                <span class="change-type" :class="change.changeType">{{ change.changeType }}</span>
                <span class="change-value">
                  {{ change.beforeWeight.toFixed(2) }} → {{ change.afterWeight.toFixed(2) }}
                  ({{ change.changePercentage > 0 ? '+' : '' }}{{ change.changePercentage.toFixed(1) }}%)
                </span>
              </div>
            </div>
          </div>
          <p v-else class="no-data">No connection changes detected</p>
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
              <p class="reference-abstract">{{ ref.abstractText?.substring(0, 200) }}...</p>
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

/* Network Stats */
.network-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--color-text);
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #667eea;
}

/* Changes List */
.changes-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.change-item {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1rem;
}

.change-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.region-name {
  font-weight: 600;
  color: var(--color-heading);
  font-size: 0.95rem;
}

.arrow {
  color: var(--color-text);
}

.change-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.change-type {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.change-type.STRENGTHENED {
  background: rgba(46, 213, 115, 0.2);
  color: #27ae60;
}

.change-type.WEAKENED {
  background: rgba(255, 107, 107, 0.2);
  color: #e74c3c;
}

.change-type.MINIMAL_CHANGE {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

.change-value {
  color: var(--color-text);
  font-size: 0.9rem;
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

.reference-abstract {
  color: var(--color-text);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0;
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

  .network-stats {
    grid-template-columns: 1fr;
  }

  .reference-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
