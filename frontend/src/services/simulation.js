/**
 * Simulation API service
 * Handles all simulation-related API calls to the Spring Boot backend
 */

import apiClient from './api'

export const simulationService = {
  /**
   * Create and run a new simulation
   * @param {Object} scenarioData - Scenario parameters
   * @returns {Promise<Object>} Simulation results
   */
  createSimulation: async (scenarioData) => {
    const response = await apiClient.post('/simulations', scenarioData)
    return response.data
  },

  /**
   * Get simulation results by ID
   * @param {number} simulationId - Simulation ID
   * @returns {Promise<Object>} Simulation results
   */
  getSimulation: async (simulationId) => {
    const response = await apiClient.get(`/simulations/${simulationId}`)
    return response.data
  },

  /**
   * Get user's simulation history
   * @returns {Promise<Array>} List of simulations
   */
  getSimulationHistory: async () => {
    const response = await apiClient.get('/simulations/history')
    return response.data
  },

  /**
   * Health check endpoint
   * @returns {Promise<Object>} Health status
   */
  healthCheck: async () => {
    const response = await apiClient.get('/simulations/health')
    return response.data
  },
}

export default simulationService
