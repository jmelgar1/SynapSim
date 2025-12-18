/**
 * Application constants
 */

// Local storage keys
export const STORAGE_KEYS = {
  ONBOARDING: 'synapSimOnboarding',
  USER_PREFERENCES: 'synapSimPreferences',
  SIMULATION_HISTORY: 'synapSimHistory',
}

// Focus areas for personalization
export const FOCUS_AREAS = {
  ANXIETY: 'anxiety',
  CREATIVITY: 'creativity',
  DEPRESSION: 'depression',
  EMPATHY: 'empathy',
  GENERAL: 'general',
}

// Experience levels
export const EXPERIENCE_LEVELS = {
  NONE: 'none',
  BASIC: 'basic',
  INTERMEDIATE: 'intermediate',
  ADVANCED: 'advanced',
}

// Learning styles
export const LEARNING_STYLES = {
  VISUAL: 'visual',
  READING: 'reading',
  INTERACTIVE: 'interactive',
  MIXED: 'mixed',
}

// Simulation parameters (example - adjust based on backend)
export const SIMULATION_TYPES = {
  PSILOCYBIN: 'psilocybin',
  LSD: 'lsd',
  KETAMINE: 'ketamine',
  MDMA: 'mdma',
}

// API endpoints (when backend is ready)
export const API_ENDPOINTS = {
  SIMULATIONS: '/simulations',
  QUESTS: '/quests',
  USER_PROGRESS: '/progress',
}

// Scenario Builder Options
export const COMPOUND_INSPIRATIONS = [
  {
    value: 'psilocybin',
    label: 'Psilocybin-like (research-based)',
    description: 'Inspired by psilocybin studies showing enhanced neural connectivity and neuroplasticity.',
  },
  {
    value: 'lsd',
    label: 'LSD-inspired (therapeutic)',
    description: 'Based on LSD research demonstrating increased brain network integration.',
  },
  {
    value: 'ketamine',
    label: 'Ketamine-inspired (rapid-acting)',
    description: 'Modeled after ketamine studies showing rapid synaptogenesis and mood effects.',
  },
  {
    value: 'mdma',
    label: 'MDMA-inspired (empathogenic)',
    description: 'Inspired by MDMA research on social bonding and emotional processing.',
  },
]

export const THERAPEUTIC_SETTINGS = [
  {
    value: 'calm-nature',
    label: 'Calm Nature',
    description: 'Peaceful outdoor environment promoting relaxation and introspection.',
    icon: '🌿',
  },
  {
    value: 'guided-therapy',
    label: 'Guided Therapy Room',
    description: 'Professional therapeutic setting with expert support.',
    icon: '🏥',
  },
  {
    value: 'meditation-space',
    label: 'Meditation Space',
    description: 'Quiet, minimalist environment designed for mindfulness.',
    icon: '🧘',
  },
  {
    value: 'creative-studio',
    label: 'Creative Studio',
    description: 'Inspiring artistic environment encouraging creative expression.',
    icon: '🎨',
  },
  {
    value: 'social-gathering',
    label: 'Social Gathering',
    description: 'Warm, communal setting fostering connection and empathy.',
    icon: '👥',
  },
]

export const BRAIN_REGIONS = [
  {
    value: 'medial-prefrontal-cortex',
    label: 'Medial Prefrontal Cortex',
    description: 'Hub for self-awareness; shows decreased activity and decoupling, promoting flexible thinking.',
  },
  {
    value: 'posterior-cingulate-cortex',
    label: 'Posterior Cingulate Cortex',
    description: 'Integrates personal experiences; connectivity weakens, linked to ego dissolution.',
  },
  {
    value: 'anterior-hippocampus',
    label: 'Anterior Hippocampus',
    description: 'Handles memory; reduced functional connectivity with DMN during and post-trip, aiding insights.',
  },
  {
    value: 'amygdala',
    label: 'Amygdala',
    description: 'Processes emotions; decreased reactivity and altered connectivity, reducing fear.',
  },
  {
    value: 'visual-cortex',
    label: 'Visual Cortex',
    description: 'Increased activity and connections, contributing to hallucinations.',
  },
  {
    value: 'auditory-cortex',
    label: 'Auditory Cortex',
    description: 'Heightened integration with other areas, enhancing perceptual changes.',
  },
  {
    value: 'thalamus',
    label: 'Thalamus',
    description: 'Relays signals; desynchronizes, allowing novel information flow.',
  },
  {
    value: 'anteromedial-caudate',
    label: 'Anteromedial Caudate',
    description: 'Involved in motivation; shows connectivity changes for behavioral flexibility.',
  },
  {
    value: 'frontoparietal-regions',
    label: 'Frontoparietal Regions',
    description: 'Attention control; increased integration with sensory and limbic systems.',
  },
  {
    value: 'cerebellum',
    label: 'Cerebellum',
    description: 'Motor and cognitive roles; exhibits functional connectivity shifts.',
  },
]

export const SIMULATION_DURATIONS = [
  { value: 'short', label: '15-30 minutes', description: 'Quick exploration' },
  { value: 'medium', label: '1-2 hours', description: 'Standard session' },
  { value: 'extended', label: '3-4 hours', description: 'Deep dive experience' },
]

// Quest definitions
export const QUESTS = [
  {
    id: 'quest-1',
    title: 'Rewire Anxiety Pathways',
    description: 'Simulate how calming environments might strengthen connections between the prefrontal cortex and amygdala, potentially reducing anxiety responses.',
    icon: '🧘',
    difficulty: 'beginner',
    focusAreas: ['anxiety', 'general'],
    badge: 'Anxiety Pathway Explorer',
  },
  {
    id: 'quest-2',
    title: 'Boost Empathy Circuits',
    description: 'Explore how enhanced neural connectivity in the default mode network could promote empathy and social bonding.',
    icon: '💙',
    difficulty: 'beginner',
    focusAreas: ['empathy', 'general'],
    badge: 'Empathy Circuit Builder',
  },
  {
    id: 'quest-3',
    title: 'Enhance Creative Pathways',
    description: 'Visualize increased connectivity between brain regions associated with divergent thinking and creative problem-solving.',
    icon: '🎨',
    difficulty: 'intermediate',
    focusAreas: ['creativity', 'general'],
    badge: 'Creative Pathway Architect',
  },
  {
    id: 'quest-4',
    title: 'Stabilize Mood Pathways',
    description: 'Simulate neuroplastic changes that could support mood regulation and emotional stability through enhanced serotonin pathway connectivity.',
    icon: '🌈',
    difficulty: 'intermediate',
    focusAreas: ['depression', 'anxiety', 'general'],
    badge: 'Mood Stabilizer',
  },
  {
    id: 'quest-5',
    title: 'Optimize Neural Networks',
    description: 'Advanced exploration of global brain connectivity patterns and how they might be influenced by neuroplastic changes.',
    icon: '🧠',
    difficulty: 'advanced',
    focusAreas: ['general'],
    badge: 'Master Forger',
  },
]
