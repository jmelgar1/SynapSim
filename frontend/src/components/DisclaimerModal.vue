<script setup>
import { ref } from 'vue'
import PrimaryButton from './PrimaryButton.vue'

const isOpen = ref(true)

const closeDisclaimer = () => {
  isOpen.value = false
  emit('dismissed')
}

const emit = defineEmits(['dismissed'])
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Important Disclaimer</h2>
        </div>
        <div class="modal-body">
          <p>
            <strong>SynapSim is an educational tool only.</strong> This simulator explores simulated neural pathways inspired by
            psychedelic research for educational purposes.
          </p>
          <ul>
            <li>Not a substitute for medical advice or professional treatment</li>
            <li>Simulations are based on public research and are speculative</li>
            <li>Results should not be used for self-diagnosis or self-treatment</li>
            <li>Always consult a healthcare provider for mental health concerns</li>
          </ul>
          <p class="disclaimer-footer">
            By proceeding, you acknowledge that you understand this is an educational simulation, not medical guidance.
          </p>
        </div>
        <div class="modal-footer">
          <PrimaryButton @click="closeDisclaimer"> I Understand </PrimaryButton>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: var(--color-background);
  border-radius: 12px;
  padding: 0;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 24px;
  border-bottom: 2px solid var(--color-border);
}

.modal-header h2 {
  color: var(--color-heading);
  margin: 0;
  font-size: 1.5rem;
}

.modal-body {
  padding: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.modal-body p {
  margin: 0 0 16px 0;
  line-height: 1.6;
  color: var(--color-text);
}

.modal-body ul {
  margin: 16px 0 16px 20px;
  padding: 0;
  list-style-position: inside;
}

.modal-body li {
  margin-bottom: 10px;
  color: var(--color-text);
}

.disclaimer-footer {
  margin-top: 20px !important;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
  font-size: 0.95rem;
  color: var(--color-text);
  font-style: italic;
}

.modal-footer {
  padding: 24px;
  border-top: 2px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
}
</style>
