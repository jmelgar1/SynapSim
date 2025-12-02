<script setup>
import { ref } from 'vue'
import DisclaimerModal from '../components/DisclaimerModal.vue'
import PrimaryButton from '../components/PrimaryButton.vue'
import ThreeNeuralBackground from '../components/ThreeNeuralBackground.vue'

const disclaimerDismissed = ref(false)

const handleDisclaimerDismissed = () => {
  disclaimerDismissed.value = true
}

const handleBeginForging = () => {
  // TODO: Navigate to onboarding or dashboard
  console.log('Begin Forging clicked')
}
</script>

<template>
  <DisclaimerModal v-if="!disclaimerDismissed" @dismissed="handleDisclaimerDismissed" />

  <main class="landing-page">
    <div class="hero-section">
      <ThreeNeuralBackground class="neural-background" />

      <div class="hero-content">
        <h1 class="hero-title">SynapSim</h1>
        <p class="hero-subtitle">
          Explore simulated neural pathways inspired by psychedelic research for educational purposes.
        </p>
        <p class="hero-description">
          Gamified learning meets neuroscience. Design personalized scenarios, visualize neural changes, and discover how
          brain connectivity might evolve under different conditions.
        </p>

        <PrimaryButton @click="handleBeginForging" class="begin-button">
          Start Simulation
        </PrimaryButton>
      </div>
    </div>

    <section class="features-section">
      <div class="feature-card">
        <div class="feature-icon">🧠</div>
        <h3>Interactive Simulations</h3>
        <p>Build custom scenarios and visualize dynamic changes in neural connectivity in real-time.</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon">📊</div>
        <h3>Evidence-Based</h3>
        <p>Simulations are grounded in public neuroscience research from PubMed and academic sources.</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon">🎮</div>
        <h3>Gamified Learning</h3>
        <p>Earn badges, complete quests, and progress through levels while exploring neuroscience concepts.</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon">📤</div>
        <h3>Export & Share</h3>
        <p>Generate detailed PDF reports with graphs and source citations to share your discoveries.</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.landing-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-background);
  padding: 0;
}

/* Hero Section */
.hero-section {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.neural-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 2rem;
  max-width: 700px;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  margin: 0 0 1rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: fadeInDown 0.8s ease;
}

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

.hero-subtitle {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 1rem 0;
  animation: fadeIn 0.8s ease 0.2s both;
}

.hero-description {
  font-size: 1.1rem;
  color: var(--color-text);
  margin: 0 0 2rem 0;
  line-height: 1.8;
  animation: fadeIn 0.8s ease 0.4s both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.begin-button {
  animation: fadeIn 0.8s ease 0.6s both;
}

/* Features Section */
.features-section {
  padding: 5rem 2rem;
  background: var(--color-background-soft);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.feature-card {
  padding: 2rem;
  border-radius: 12px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-8px);
  border-color: #667eea;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.3rem;
  margin: 0 0 0.5rem 0;
  color: var(--color-heading);
}

.feature-card p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.6;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1.2rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .features-section {
    padding: 3rem 1rem;
    grid-template-columns: 1fr;
  }
}
</style>
