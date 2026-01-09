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
const selectedNode = ref(null)  // Currently selected node data

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
        connectedRegions: node.connectedRegions || [],
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

      // Styling matching SynapSim design system
      style: [
        {
          selector: 'node',
          style: {
            'background-color': '#e8eaf6',  // Soft purple-gray background
            'label': 'data(label)',
            'color': '#2c3e50',  // Dark text matching design system
            'text-halign': 'center',
            'text-valign': 'center',
            'font-size': '11px',
            'font-weight': '600',
            'font-family': 'Inter, sans-serif',
            'width': 50,
            'height': 50,
            'border-width': 2,
            'border-color': '#9fa8da',  // Soft purple border
            'text-outline-width': 1,
            'text-outline-color': '#ffffff',
            'transition-property': 'background-color, border-color, width, height',
            'transition-duration': '0.3s'
          }
        },
        {
          // Mentioned regions - highlighted with brand gradient colors
          selector: 'node[isMentioned = true]',
          style: {
            'background-color': '#667eea',  // Brand primary color
            'color': '#ffffff',
            'border-color': '#764ba2',  // Brand secondary color
            'border-width': 3,
            'width': 60,
            'height': 60,
            'font-size': '12px',
            'font-weight': 'bold',
            'text-outline-color': '#2c3e50',
            'text-outline-width': 2
          }
        },
        {
          // Mentioned regions with multiple mentions - extra emphasis
          selector: 'node[mentionCount > 1]',
          style: {
            'background-color': '#764ba2',  // Deeper brand color
            'border-width': 4,
            'width': 70,
            'height': 70,
            'font-size': '13px'
          }
        },
        {
          // Hover state
          selector: 'node:selected',
          style: {
            'border-color': '#667eea',
            'border-width': 4,
            'background-color': '#764ba2',
            'color': '#ffffff'
          }
        },
        {
          selector: 'edge',
          style: {
            'width': 2,
            'line-color': '#c5cae9',  // Soft purple-gray
            'target-arrow-color': '#c5cae9',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            'opacity': 0.6,
            'arrow-scale': 0.8
          }
        },
        {
          // Edges connected to mentioned regions
          selector: 'edge[source][target]',
          style: {
            'line-color': '#9fa8da',  // Slightly darker purple
            'target-arrow-color': '#9fa8da',
            'opacity': 0.7,
            'width': 2.5
          }
        },
        {
          // High weight connections
          selector: 'edge[weight > 0.7]',
          style: {
            'width': 3,
            'line-color': '#7986cb',  // More prominent purple
            'target-arrow-color': '#7986cb',
            'opacity': 0.85
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

    // Add node click event listener
    cyInstance.value.on('tap', 'node', (event) => {
      const node = event.target
      selectedNode.value = {
        code: node.data('code'),
        name: node.data('name'),
        description: node.data('description'),
        connectedRegions: node.data('connectedRegions') || [],
        mentionCount: node.data('mentionCount'),
        isMentioned: node.data('isMentioned')
      }
      console.log('Node selected:', selectedNode.value)
    })

    // Click on background to deselect
    cyInstance.value.on('tap', (event) => {
      if (event.target === cyInstance.value) {
        selectedNode.value = null
      }
    })

    isLoading.value = false

  } catch (err) {
    console.error('Error initializing Cytoscape:', err)
    error.value = 'Failed to initialize brain network visualization'
    isLoading.value = false
  }
}

// Control functions
const resetView = () => {
  if (cyInstance.value) {
    cyInstance.value.fit(undefined, 50)
    cyInstance.value.zoom(1)
    cyInstance.value.center()
  }
}

const zoomIn = () => {
  if (cyInstance.value) {
    const currentZoom = cyInstance.value.zoom()
    cyInstance.value.zoom({
      level: currentZoom * 1.2,
      renderedPosition: {
        x: cyInstance.value.width() / 2,
        y: cyInstance.value.height() / 2
      }
    })
  }
}

const zoomOut = () => {
  if (cyInstance.value) {
    const currentZoom = cyInstance.value.zoom()
    cyInstance.value.zoom({
      level: currentZoom * 0.8,
      renderedPosition: {
        x: cyInstance.value.width() / 2,
        y: cyInstance.value.height() / 2
      }
    })
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

    <!-- Controls Panel (overlay) -->
    <div v-if="!isLoading && !error" class="controls-panel">
      <button class="control-button" title="Reset View" @click="resetView">
        🔄
      </button>
      <button class="control-button" title="Zoom In" @click="zoomIn">
        ➕
      </button>
      <button class="control-button" title="Zoom Out" @click="zoomOut">
        ➖
      </button>
    </div>

    <!-- Legend Panel (bottom-left overlay) -->
    <div v-if="!isLoading && !error" class="legend-panel">
      <div class="legend-item">
        <div class="legend-node mentioned"></div>
        <span class="legend-text">Mentioned in Research</span>
      </div>
      <div class="legend-item">
        <div class="legend-node multiple-mentions"></div>
        <span class="legend-text">Multiple Mentions</span>
      </div>
      <div class="legend-item">
        <div class="legend-node normal"></div>
        <span class="legend-text">Other Regions</span>
      </div>
    </div>

    <!-- Node Info Panel (right side, appears when node selected) -->
    <div v-if="selectedNode" class="node-info-panel">
      <div class="node-info-header">
        <h3>{{ selectedNode.code }}</h3>
        <button class="close-button" @click="selectedNode = null">✕</button>
      </div>
      <div class="node-info-body">
        <div class="info-section">
          <label class="info-label">Name</label>
          <p class="info-value">{{ selectedNode.name }}</p>
        </div>
        <div class="info-section">
          <label class="info-label">Description</label>
          <p class="info-value">{{ selectedNode.description }}</p>
        </div>
        <div v-if="selectedNode.isMentioned" class="info-section">
          <label class="info-label">Research Mentions</label>
          <p class="info-value">{{ selectedNode.mentionCount }} time(s)</p>
        </div>
        <div class="info-section">
          <label class="info-label">Connected to</label>
          <div v-if="selectedNode.connectedRegions.length > 0" class="connected-regions">
            <span
              v-for="region in selectedNode.connectedRegions"
              :key="region"
              class="region-tag"
            >
              {{ region }}
            </span>
          </div>
          <p v-else class="info-value">No connections in this network</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.brain-network-container {
  position: relative;
  width: 100%;
  flex: 1;
  min-height: 400px;
  background: var(--color-background);
  border: 2px solid var(--color-border);
  border-radius: 16px;
  overflow: hidden;
}

/* Cytoscape Container */
.cytoscape-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.02) 0%, rgba(118, 75, 162, 0.02) 100%),
              var(--color-background-soft);
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

/* Legend Panel (bottom-left overlay) */
.legend-panel {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.75rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  z-index: 10;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.legend-node {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-node.normal {
  background-color: #e8eaf6;
  border: 2px solid #9fa8da;
}

.legend-node.mentioned {
  background-color: #667eea;
  border: 2px solid #764ba2;
}

.legend-node.multiple-mentions {
  background-color: #764ba2;
  border: 3px solid #5e35b1;
}

.legend-text {
  font-size: 0.75rem;
  color: var(--color-text);
  font-weight: 500;
  white-space: nowrap;
}

/* Node Info Panel (right side overlay) */
.node-info-panel {
  position: absolute;
  top: 1rem;
  right: 70px;  /* Offset from control buttons */
  width: 300px;
  max-height: calc(100% - 2rem);
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  z-index: 20;
  animation: slideInRight 0.3s ease;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.node-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.node-info-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #667eea;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: var(--color-text);
  cursor: pointer;
  padding: 0.25rem;
  line-height: 1;
  transition: all 0.2s ease;
  border-radius: 4px;
}

.close-button:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #667eea;
}

.node-info-body {
  padding: 1rem;
}

.info-section {
  margin-bottom: 1rem;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--color-text);
  margin-bottom: 0.5rem;
  opacity: 0.7;
}

.info-value {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text);
  line-height: 1.5;
}

.connected-regions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.region-tag {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.3px;
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

  .node-info-panel {
    width: calc(100% - 2rem);
    right: 1rem;
    max-height: 60%;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .metrics-panel,
  .control-button,
  .legend-panel,
  .node-info-panel {
    background: rgba(30, 30, 30, 0.95);
  }

  .cytoscape-container {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%),
                var(--color-background-soft);
  }
}
</style>
