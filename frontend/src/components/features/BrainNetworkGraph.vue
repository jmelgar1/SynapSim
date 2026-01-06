<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import cytoscape from 'cytoscape'

// Props - Data from parent component (SimulationResults.vue)
const props = defineProps({
  // Network data from backend: { nodes: [...], edges: [...], metrics: {...} }
  networkState: {
    type: Object,
    required: true,
    default: () => ({ nodes: [], edges: [], metrics: {} })
  },
  // Brain regions mentioned in research
  mentionedRegions: {
    type: Array,
    default: () => []
  }
})

// Refs
const cyContainer = ref(null)  // Reference to the DOM element for Cytoscape
const cyInstance = ref(null)   // Cytoscape instance (will be initialized in Phase 2)
const isLoading = ref(true)
const error = ref(null)

// Lifecycle hooks
onMounted(() => {
  console.log('BrainNetworkGraph mounted')
  console.log('Network State:', props.networkState)
  console.log('Mentioned Regions:', props.mentionedRegions)

  // Phase 2: Initialize Cytoscape here
  initializeCytoscape()
})

onBeforeUnmount(() => {
  console.log('BrainNetworkGraph unmounting')

  // Cleanup: Destroy Cytoscape instance to prevent memory leaks
  if (cyInstance.value) {
    cyInstance.value.destroy()
    cyInstance.value = null
  }
})

// Watch for data changes (if simulation data updates)
watch(() => props.networkState, (newState) => {
  console.log('Network state updated:', newState)
  // Re-render graph when data changes
  if (cyInstance.value && newState?.nodes?.length > 0) {
    renderGraph()
  }
}, { deep: true })

// Helper: Get mention count for a region
const getMentionCount = (regionCode) => {
  const region = props.mentionedRegions.find(r => r.regionCode === regionCode)
  return region?.mentions?.length || 0
}

// Helper: Check if region is mentioned in research
const isMentioned = (regionCode) => {
  return props.mentionedRegions.some(r => r.regionCode === regionCode)
}

// Phase 3: Transform backend nodes to Cytoscape format
const transformNodes = (backendNodes) => {
  if (!backendNodes || backendNodes.length === 0) {
    console.warn('No nodes to transform:', backendNodes)
    return []
  }

  console.log('Transforming nodes:', backendNodes)

  const transformed = backendNodes.map(node => {
    const cyNode = {
      data: {
        id: node.id || node.code,
        label: node.code || node.name,
        name: node.name,
        code: node.code,
        description: node.description,
        activityLevel: node.activityLevel || 0,
        mentionCount: getMentionCount(node.code),
        isMentioned: isMentioned(node.code),
        // Store raw positions for debugging
        rawPositionX: node.positionX,
        rawPositionY: node.positionY
      },
      // Use backend positions - scale DOWN since they're too large
      // Backend gives values like 80-200, we need values that fit in ~1000px container
      position: (node.positionX !== undefined && node.positionY !== undefined)
        ? { x: node.positionX * 5, y: node.positionY * 5 }  // Scale by 5 instead of 100
        : undefined
    }
    console.log('Transformed node:', node.code || node.name, '→', cyNode)
    return cyNode
  })

  return transformed
}

// Phase 3: Transform backend edges to Cytoscape format
const transformEdges = (backendEdges) => {
  if (!backendEdges || backendEdges.length === 0) {
    console.warn('No edges to transform:', backendEdges)
    return []
  }

  console.log('Transforming edges:', backendEdges)

  const transformed = backendEdges.map((edge, index) => {
    const cyEdge = {
      data: {
        id: edge.id || `edge_${index}`,
        source: edge.source,
        target: edge.target,
        weight: edge.weight || 0.5,
        connectionType: edge.connectionType || 'EXCITATORY',
        isBidirectional: edge.isBidirectional || false
      }
    }
    console.log('Transformed edge:', edge.source, '→', edge.target, cyEdge)
    return cyEdge
  })

  return transformed
}

// Phase 3: Render graph with actual data
const renderGraph = () => {
  if (!cyInstance.value) return

  const nodes = transformNodes(props.networkState?.nodes || [])
  const edges = transformEdges(props.networkState?.edges || [])

  console.log('Rendering graph with:', { nodes: nodes.length, edges: edges.length })

  // Update graph elements
  cyInstance.value.elements().remove()
  cyInstance.value.add([...nodes, ...edges])

  // Re-run layout
  const hasPositions = nodes.some(n => n.position !== undefined)
  const layout = cyInstance.value.layout({
    name: hasPositions ? 'preset' : 'circle',  // Use preset if we have positions, otherwise circle
    animate: true,
    animationDuration: 500
  })
  layout.run()
}

// Initialize Cytoscape (Phase 2 + 3)
const initializeCytoscape = async () => {
  try {
    // Note: isLoading starts as true from initial state
    error.value = null

    // Wait for DOM to be ready
    await nextTick()

    // Make sure container exists
    if (!cyContainer.value) {
      throw new Error('Cytoscape container not found')
    }

    console.log('Initializing Cytoscape...')
    console.log('Raw networkState:', props.networkState)
    console.log('Raw nodes array:', props.networkState?.nodes)
    console.log('Raw edges array:', props.networkState?.edges)

    // Phase 3: Transform actual simulation data
    const nodes = transformNodes(props.networkState?.nodes || [])
    const edges = transformEdges(props.networkState?.edges || [])
    const elements = [...nodes, ...edges]

    console.log('Graph elements:', {
      nodes: nodes.length,
      edges: edges.length,
      totalElements: elements.length
    })

    if (elements.length === 0) {
      console.error('No graph elements! Check if networkState has data.')
      // Show error instead of empty graph
      error.value = 'No network data available for this simulation'
      isLoading.value = false
      return
    }

    console.log('Final elements to render:', elements)

    // Initialize Cytoscape instance
    cyInstance.value = cytoscape({
      container: cyContainer.value,
      elements: elements,

      // Basic styling - VERY VISIBLE for debugging
      style: [
        {
          selector: 'node',
          style: {
            'background-color': '#FF0000',  // BRIGHT RED for visibility
            'label': 'data(label)',
            'color': '#000000',  // Black text
            'text-halign': 'center',
            'text-valign': 'center',
            'font-size': '20px',  // Larger font
            'font-weight': 'bold',
            'width': 80,  // Larger nodes
            'height': 80,
            'border-width': 4,
            'border-color': '#000000',  // Black border
            'text-outline-width': 2,
            'text-outline-color': '#FFFFFF'
          }
        },
        {
          selector: 'edge',
          style: {
            'width': 5,  // Thicker edges
            'line-color': '#0000FF',  // BRIGHT BLUE
            'target-arrow-color': '#0000FF',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            'opacity': 1
          }
        }
      ],

      // Layout configuration
      layout: {
        // Temporarily force circle layout while we debug positions
        name: 'circle',  // TODO: Change to 'preset' once positions are fixed
        animate: true,
        animationDuration: 500,
        fit: true,  // Fit graph to container
        padding: 50  // Padding around graph
      },

      // Interaction settings
      minZoom: 0.5,
      maxZoom: 2,
      wheelSensitivity: 0.2
    })

    console.log('Cytoscape initialized successfully!', cyInstance.value)

    // Debug: Check what Cytoscape actually has
    console.log('Cytoscape nodes count:', cyInstance.value.nodes().length)
    console.log('Cytoscape edges count:', cyInstance.value.edges().length)
    const nodePositions = cyInstance.value.nodes().map(n => ({
      id: n.id(),
      label: n.data('label'),
      position: { x: n.position().x, y: n.position().y }
    }))
    console.log('Cytoscape node positions:', nodePositions)

    // Force fit to ensure nodes are visible
    cyInstance.value.fit(undefined, 50)

    // Debug: Check container and canvas
    console.log('Container dimensions:', {
      width: cyContainer.value.offsetWidth,
      height: cyContainer.value.offsetHeight
    })
    console.log('Cytoscape extent:', cyInstance.value.extent())

    // Force a resize and redraw
    setTimeout(() => {
      cyInstance.value.resize()
      cyInstance.value.fit(undefined, 50)
      console.log('Forced resize and refit')
    }, 100)

    isLoading.value = false

  } catch (err) {
    console.error('Error initializing Cytoscape:', err)
    error.value = 'Failed to initialize brain network visualization'
    isLoading.value = false
  }
}
</script>

<template>
  <div class="brain-network-container">
    <!-- Cytoscape Container - Always in DOM -->
    <div
      ref="cyContainer"
      class="cytoscape-container"
      :class="{ hidden: isLoading || error }"
    >
      <!-- Cytoscape will render here -->
    </div>

    <!-- Loading State Overlay -->
    <div v-if="isLoading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading brain network...</p>
    </div>

    <!-- Error State Overlay -->
    <div v-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <p>{{ error }}</p>
    </div>

    <!-- Network Metrics Panel (overlay) -->
    <div v-if="!isLoading && !error" class="metrics-panel">
      <div class="metric-item">
        <span class="metric-label">Nodes</span>
        <span class="metric-value">{{ networkState?.metrics?.totalNodes || 0 }}</span>
      </div>
      <div class="metric-item">
        <span class="metric-label">Connections</span>
        <span class="metric-value">{{ networkState?.metrics?.totalConnections || 0 }}</span>
      </div>
      <div class="metric-item">
        <span class="metric-label">Density</span>
        <span class="metric-value">{{ ((networkState?.metrics?.networkDensity || 0) * 100).toFixed(1) }}%</span>
      </div>
    </div>

    <!-- Controls Panel (overlay) - Phase 5 -->
    <div v-if="!isLoading && !error" class="controls-panel">
      <button class="control-button" title="Reset View">
        🔄
      </button>
      <button class="control-button" title="Zoom In">
        ➕
      </button>
      <button class="control-button" title="Zoom Out">
        ➖
      </button>
    </div>
  </div>
</template>

<style scoped>
.brain-network-container {
  position: relative;
  width: 100%;
  height: 600px;
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  overflow: hidden;
}

/* Cytoscape Container */
.cytoscape-container {
  width: 100%;
  height: 100%;
  background: #f0f0f0;  /* Light gray background for better visibility */
}

.cytoscape-container.hidden {
  visibility: hidden;
}

/* Loading State Overlay */
.loading-state {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--color-background);
  gap: 1rem;
  z-index: 20;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--color-border);
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: var(--color-text);
  font-size: 0.95rem;
}

/* Error State Overlay */
.error-state {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--color-background);
  gap: 0.5rem;
  z-index: 20;
}

.error-icon {
  font-size: 3rem;
}

.error-state p {
  color: var(--color-text);
  font-size: 0.95rem;
}

/* Metrics Panel (top-left overlay) */
.metrics-panel {
  position: absolute;
  top: 1rem;
  left: 1rem;
  display: flex;
  gap: 1rem;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.75rem 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  z-index: 10;
}

.metric-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.metric-label {
  font-size: 0.75rem;
  color: var(--color-text);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}

.metric-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #667eea;
}

/* Controls Panel (top-right overlay) */
.controls-panel {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  z-index: 10;
}

.control-button {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.control-button:hover {
  background: #667eea;
  border-color: #667eea;
  transform: scale(1.05);
}

.control-button:active {
  transform: scale(0.95);
}

/* Responsive */
@media (max-width: 768px) {
  .brain-network-container {
    height: 400px;
  }

  .metrics-panel {
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.5rem;
  }

  .metric-item {
    flex-direction: row;
    gap: 0.5rem;
  }

  .controls-panel {
    flex-direction: row;
    top: auto;
    bottom: 1rem;
    right: 1rem;
  }
}

/* Dark mode support (if using CSS variables) */
@media (prefers-color-scheme: dark) {
  .metrics-panel,
  .control-button {
    background: rgba(30, 30, 30, 0.95);
  }
}
</style>
