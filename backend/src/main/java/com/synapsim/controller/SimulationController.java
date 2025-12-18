package com.synapsim.controller;

import com.synapsim.dto.ScenarioRequest;
import com.synapsim.dto.SimulationResponse;
import com.synapsim.service.SimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for simulation operations
 * Main API for running brain network simulations
 */
@RestController
@RequestMapping("/simulations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;
    private final com.synapsim.repository.BrainRegionRepository brainRegionRepository;
    private final com.synapsim.repository.NeuralConnectionRepository neuralConnectionRepository;

    /**
     * Create and run a new simulation
     * POST /api/simulations
     *
     * @param request Scenario parameters
     * @return Simulation results
     */
    @PostMapping
    public ResponseEntity<SimulationResponse> createSimulation(
            @Valid @RequestBody ScenarioRequest request
    ) {
        log.info("Received simulation request: compound={}, setting={}",
                request.getCompoundInspiration(), request.getTherapeuticSetting());

        try {
            SimulationResponse response = simulationService.runSimulation(request);

            log.info("Simulation completed successfully: id={}, confidence={}",
                    response.getId(), response.getConfidenceScore());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating simulation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get simulation by ID
     * GET /api/simulations/{id}
     *
     * @param id Simulation ID
     * @return Simulation results
     */
    @GetMapping("/{id}")
    public ResponseEntity<SimulationResponse> getSimulation(@PathVariable Long id) {
        log.info("Retrieving simulation: id={}", id);

        try {
            SimulationResponse response = simulationService.getSimulation(id);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Simulation not found: id={}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error retrieving simulation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get simulation history
     * GET /api/simulations/history
     *
     * @return List of all simulations
     */
    @GetMapping("/history")
    public ResponseEntity<List<SimulationResponse>> getSimulationHistory() {
        log.info("Retrieving simulation history");

        try {
            List<SimulationResponse> history = simulationService.getSimulationHistory();
            log.info("Found {} simulations in history", history.size());

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error retrieving simulation history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/simulations/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "simulation-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    /**
     * Debug endpoint to check database data
     * GET /api/simulations/debug/data
     */
    @GetMapping("/debug/data")
    public ResponseEntity<Map<String, Object>> debugData() {
        long regionCount = brainRegionRepository.count();
        long connectionCount = neuralConnectionRepository.count();

        return ResponseEntity.ok(Map.of(
                "brainRegions", regionCount,
                "neuralConnections", connectionCount,
                "regions", brainRegionRepository.findAll().stream()
                        .map(r -> r.getName() + " (" + r.getCode() + ")")
                        .toList()
        ));
    }
}
