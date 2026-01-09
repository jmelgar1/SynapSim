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
    label: 'Psilocybin',
    description: 'Enhanced neural connectivity and neuroplasticity.',
  },
  {
    value: 'lsd',
    label: 'LSD',
    description: 'Increased brain network integration.',
  },
  {
    value: 'ketamine',
    label: 'Ketamine',
    description: 'Rapid synaptogenesis and mood effects.',
  },
  {
    value: 'mdma',
    label: 'MDMA',
    description: 'Social bonding and emotional processing.',
  },
]

export const THERAPEUTIC_SETTINGS = [
  {
    value: 'calm-nature',
    label: 'Calm Nature',
    description: 'Peaceful outdoor relaxation and introspection.',
    icon: '🌿',
  },
  {
    value: 'guided-therapy',
    label: 'Guided Therapy',
    description: 'Professional therapeutic setting with support.',
    icon: '🏥',
  },
  {
    value: 'meditation-space',
    label: 'Meditation Space',
    description: 'Quiet environment designed for mindfulness.',
    icon: '🧘',
  },
  {
    value: 'creative-studio',
    label: 'Creative Studio',
    description: 'Artistic environment for creative expression.',
    icon: '🎨',
  },
  {
    value: 'social-gathering',
    label: 'Social Gathering',
    description: 'Communal setting fostering connection and empathy.',
    icon: '👥',
  },
]

// Research Focus Options (optional therapeutic goals)
export const RESEARCH_FOCUS = [
  {
    value: 'anxiety-fear',
    label: 'Anxiety & Fear',
    description: 'Anxiety reduction and fear processing research.',
    icon: '😰',
  },
  {
    value: 'depression-mood',
    label: 'Depression & Mood',
    description: 'Mood enhancement and depression studies.',
    icon: '🌧️',
  },
  {
    value: 'trauma-ptsd',
    label: 'Trauma & PTSD',
    description: 'Trauma processing and PTSD treatment research.',
    icon: '🩹',
  },
  {
    value: 'addiction-craving',
    label: 'Addiction & Craving',
    description: 'Addiction and craving reduction studies.',
    icon: '🔗',
  },
  {
    value: 'social-empathy',
    label: 'Social & Empathy',
    description: 'Social bonding and empathy enhancement research.',
    icon: '🤝',
  },
  {
    value: 'mindfulness-awareness',
    label: 'Mindfulness',
    description: 'Meditation and consciousness studies.',
    icon: '🧘',
  },
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
