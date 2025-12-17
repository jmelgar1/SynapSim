package com.synapsim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapsim.dto.BrainNetworkDTO;
import com.synapsim.dto.PubMedArticleDTO;
import com.synapsim.dto.ScenarioRequest;
import com.synapsim.dto.SimulationResponse;
import com.synapsim.model.*;
import com.synapsim.repository.ScenarioRepository;
import com.synapsim.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main simulation service that orchestrates the brain network simulation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {

    private final ScenarioRepository scenarioRepository;
    private final SimulationRepository simulationRepository;
    private final BrainNetworkService brainNetworkService;
    private final PubMedService pubMedService;
    private final ObjectMapper objectMapper;

    /**
     * Run a complete simulation based on scenario parameters
     */
    @Transactional
    public SimulationResponse runSimulation(ScenarioRequest request) {
        log.info("Starting simulation for scenario: compound={}, setting={}",
                request.getCompoundInspiration(), request.getTherapeuticSetting());

        long startTime = System.currentTimeMillis();

        try {
            // 1. Create and save scenario
            Scenario scenario = createScenario(request);
            scenario = scenarioRepository.save(scenario);
            log.debug("Saved scenario with ID: {}", scenario.getId());

            // 2. Initialize simulation record
            Simulation simulation = new Simulation();
            simulation.setScenario(scenario);
            simulation.setStatus(Simulation.SimulationStatus.PROCESSING);
            simulation.setExecutedAt(LocalDateTime.now());
            simulation = simulationRepository.save(simulation);

            // 3. Build brain network graph
            Graph<String, DefaultWeightedEdge> originalGraph = brainNetworkService.buildBrainGraph();
            Graph<String, DefaultWeightedEdge> modifiedGraph = brainNetworkService.buildBrainGraph();

            // 4. Apply neuroplasticity changes
            Map<String, Map<String, Double>> changes = brainNetworkService.applyNeuroplasticityChanges(
                    modifiedGraph, scenario
            );

            // 5. Search for relevant research articles
            List<PubMedArticleDTO> articles = searchRelevantResearch(scenario);

            // 6. Save PubMed references
            savePubMedReferences(simulation, articles);

            // 7. Generate network state JSON
            BrainNetworkDTO networkState = brainNetworkService.convertGraphToDTO(modifiedGraph);
            String networkStateJson = objectMapper.writeValueAsString(networkState);

            // 8. Generate connection changes JSON
            List<SimulationResponse.ConnectionChangeDTO> connectionChanges =
                    brainNetworkService.generateConnectionChanges(changes, originalGraph);
            String connectionChangesJson = objectMapper.writeValueAsString(connectionChanges);

            // 9. Generate prediction summary and confidence score
            String predictionSummary = generatePredictionSummary(scenario, connectionChanges, articles.size());
            double confidenceScore = calculateConfidenceScore(connectionChanges, articles.size());

            // 10. Determine success and badge
            boolean success = determineSuccess(confidenceScore, connectionChanges);
            String badge = determineBadge(scenario, success, connectionChanges);

            // 11. Update simulation with results
            simulation.setStatus(Simulation.SimulationStatus.COMPLETED);
            simulation.setNetworkState(networkStateJson);
            simulation.setConnectionChanges(connectionChangesJson);
            simulation.setPredictionSummary(predictionSummary);
            simulation.setConfidenceScore(confidenceScore);
            simulation.setSuccess(success);
            simulation.setBadgeEarned(badge);

            long processingTime = System.currentTimeMillis() - startTime;
            simulation.setProcessingTimeMs(processingTime);

            simulation = simulationRepository.save(simulation);

            log.info("Simulation completed in {}ms with confidence score: {}", processingTime, confidenceScore);

            // 12. Build and return response
            return buildSimulationResponse(simulation, networkState, connectionChanges, articles);

        } catch (Exception e) {
            log.error("Error running simulation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to run simulation: " + e.getMessage(), e);
        }
    }

    /**
     * Get simulation by ID
     */
    public SimulationResponse getSimulation(Long simulationId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new RuntimeException("Simulation not found: " + simulationId));

        try {
            // Parse stored JSON data
            BrainNetworkDTO networkState = objectMapper.readValue(
                    simulation.getNetworkState(), BrainNetworkDTO.class
            );

            List<SimulationResponse.ConnectionChangeDTO> connectionChanges = objectMapper.readValue(
                    simulation.getConnectionChanges(),
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class, SimulationResponse.ConnectionChangeDTO.class
                    )
            );

            // Get PubMed references
            List<PubMedArticleDTO> articles = simulation.getPubmedReferences().stream()
                    .map(this::convertToPubMedDTO)
                    .collect(Collectors.toList());

            return buildSimulationResponse(simulation, networkState, connectionChanges, articles);

        } catch (Exception e) {
            log.error("Error retrieving simulation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve simulation: " + e.getMessage(), e);
        }
    }

    /**
     * Get simulation history (all completed simulations)
     */
    public List<SimulationResponse> getSimulationHistory() {
        List<Simulation> simulations = simulationRepository.findAll();
        return simulations.stream()
                .map(sim -> {
                    try {
                        BrainNetworkDTO networkState = objectMapper.readValue(
                                sim.getNetworkState(), BrainNetworkDTO.class
                        );
                        List<SimulationResponse.ConnectionChangeDTO> connectionChanges = objectMapper.readValue(
                                sim.getConnectionChanges(),
                                objectMapper.getTypeFactory().constructCollectionType(
                                        List.class, SimulationResponse.ConnectionChangeDTO.class
                                )
                        );
                        List<PubMedArticleDTO> articles = sim.getPubmedReferences().stream()
                                .map(this::convertToPubMedDTO)
                                .collect(Collectors.toList());

                        return buildSimulationResponse(sim, networkState, connectionChanges, articles);
                    } catch (Exception e) {
                        log.error("Error parsing simulation {}: {}", sim.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Create scenario from request
     */
    private Scenario createScenario(ScenarioRequest request) {
        Scenario scenario = new Scenario();
        scenario.setQuestId(request.getQuestId());
        scenario.setCompoundInspiration(request.getCompoundType());
        scenario.setTherapeuticSetting(request.getSettingType());
        scenario.setPrimaryBrainRegion(request.getPrimaryBrainRegion());
        scenario.setSimulationDuration(request.getDurationType());
        scenario.setIntegrationSteps(request.getIntegrationSteps());
        return scenario;
    }

    /**
     * Search for relevant research articles
     */
    private List<PubMedArticleDTO> searchRelevantResearch(Scenario scenario) {
        log.info("Searching PubMed for relevant research");

        List<String> keywords = pubMedService.generateSearchKeywords(
                scenario.getCompoundInspiration().getValue(),
                scenario.getTherapeuticSetting().getValue(),
                scenario.getPrimaryBrainRegion()
        );

        List<PubMedArticleDTO> articles = pubMedService.searchArticles(keywords);
        log.info("Found {} relevant articles", articles.size());

        return articles;
    }

    /**
     * Save PubMed references to database
     */
    private void savePubMedReferences(Simulation simulation, List<PubMedArticleDTO> articles) {
        for (PubMedArticleDTO article : articles) {
            PubMedReference reference = new PubMedReference();
            reference.setSimulation(simulation);
            reference.setPubmedId(article.getPubmedId());
            reference.setTitle(article.getTitle());
            reference.setAuthors(article.getAuthors());
            reference.setPublicationDate(article.getPublicationDate());
            reference.setAbstractText(article.getAbstractText());
            reference.setArticleUrl(article.getArticleUrl());
            reference.setRelevanceScore(article.getRelevanceScore());
            reference.setKeywords(article.getKeywords());

            simulation.getPubmedReferences().add(reference);
        }
    }

    /**
     * Generate prediction summary based on simulation results
     */
    private String generatePredictionSummary(
            Scenario scenario,
            List<SimulationResponse.ConnectionChangeDTO> changes,
            int researchCount
    ) {
        StringBuilder summary = new StringBuilder();

        // Count increases and decreases
        long increases = changes.stream()
                .filter(c -> c.getChangeType().equals("INCREASED"))
                .count();
        long decreases = changes.stream()
                .filter(c -> c.getChangeType().equals("DECREASED"))
                .count();

        // Get top 3 most significant changes
        List<SimulationResponse.ConnectionChangeDTO> topChanges = changes.stream()
                .limit(3)
                .collect(Collectors.toList());

        // Build summary
        summary.append(String.format("Based on %d relevant research studies, this simulation suggests that ",
                researchCount));
        summary.append(scenario.getCompoundInspiration().getValue());
        summary.append(" in a ");
        summary.append(scenario.getTherapeuticSetting().getValue());
        summary.append(" setting could promote neural pathway changes. ");

        summary.append(String.format("The simulation showed %d strengthened and %d weakened connections. ",
                increases, decreases));

        summary.append("Most notable changes include: ");
        for (int i = 0; i < topChanges.size(); i++) {
            SimulationResponse.ConnectionChangeDTO change = topChanges.get(i);
            if (i > 0) summary.append(", ");
            summary.append(String.format("%s-%s (%s%.1f%%)",
                    extractCode(change.getSourceRegion()),
                    extractCode(change.getTargetRegion()),
                    change.getChangePercentage() > 0 ? "+" : "",
                    change.getChangePercentage()));
        }
        summary.append(". ");

        // Add therapeutic implications
        summary.append(generateTherapeuticImplications(scenario, topChanges));

        return summary.toString();
    }

    /**
     * Generate therapeutic implications based on changes
     */
    private String generateTherapeuticImplications(
            Scenario scenario,
            List<SimulationResponse.ConnectionChangeDTO> topChanges
    ) {
        StringBuilder implications = new StringBuilder();

        implications.append("These changes suggest potential benefits for ");

        // Determine therapeutic targets based on compound and changes
        switch (scenario.getCompoundInspiration()) {
            case PSILOCYBIN:
                implications.append("mood regulation, anxiety reduction, and emotional processing");
                break;
            case LSD:
                implications.append("creative thinking, cognitive flexibility, and perceptual enhancement");
                break;
            case KETAMINE:
                implications.append("rapid mood improvement, cognitive flexibility, and stress resilience");
                break;
            case MDMA:
                implications.append("emotional openness, empathy enhancement, and trauma processing");
                break;
        }

        implications.append(". Integration practices like ");
        if (scenario.getIntegrationSteps() != null && !scenario.getIntegrationSteps().isEmpty()) {
            implications.append(scenario.getIntegrationSteps());
        } else {
            implications.append("mindfulness and journaling");
        }
        implications.append(" may help consolidate these neuroplastic changes.");

        return implications.toString();
    }

    /**
     * Calculate confidence score based on simulation quality
     */
    private double calculateConfidenceScore(
            List<SimulationResponse.ConnectionChangeDTO> changes,
            int researchCount
    ) {
        double score = 0.0;

        // Base score from research availability (0-0.4)
        score += Math.min(0.4, researchCount * 0.08);

        // Score from number of significant changes (0-0.3)
        long significantChanges = changes.stream()
                .filter(c -> Math.abs(c.getChangePercentage()) > 10.0)
                .count();
        score += Math.min(0.3, significantChanges * 0.05);

        // Score from magnitude of changes (0-0.3)
        double avgMagnitude = changes.stream()
                .mapToDouble(c -> Math.abs(c.getChangePercentage()))
                .average()
                .orElse(0.0);
        score += Math.min(0.3, avgMagnitude / 100.0);

        return Math.min(1.0, score);
    }

    /**
     * Determine if simulation was successful
     */
    private boolean determineSuccess(
            double confidenceScore,
            List<SimulationResponse.ConnectionChangeDTO> changes
    ) {
        // Success criteria:
        // 1. Confidence score above 0.5
        // 2. At least 3 significant changes
        boolean highConfidence = confidenceScore >= 0.5;
        long significantChanges = changes.stream()
                .filter(c -> Math.abs(c.getChangePercentage()) > 10.0)
                .count();

        return highConfidence && significantChanges >= 3;
    }

    /**
     * Determine badge earned based on scenario and results
     */
    private String determineBadge(
            Scenario scenario,
            boolean success,
            List<SimulationResponse.ConnectionChangeDTO> changes
    ) {
        if (!success) {
            return null;
        }

        // Determine badge based on quest type and changes
        String questId = scenario.getQuestId();
        if (questId != null && !questId.isEmpty()) {
            return switch (questId.toLowerCase()) {
                case "anxiety" -> "Mood Stabilizer";
                case "empathy" -> "Empathy Enhancer";
                case "creativity" -> "Creative Explorer";
                case "depression" -> "Neural Revitalizer";
                default -> "Pathway Pioneer";
            };
        }

        // Default badge based on compound type
        return switch (scenario.getCompoundInspiration()) {
            case PSILOCYBIN -> "Neuroplasticity Navigator";
            case LSD -> "Connectivity Explorer";
            case KETAMINE -> "Resilience Builder";
            case MDMA -> "Empathy Architect";
        };
    }

    /**
     * Build simulation response DTO
     */
    private SimulationResponse buildSimulationResponse(
            Simulation simulation,
            BrainNetworkDTO networkState,
            List<SimulationResponse.ConnectionChangeDTO> connectionChanges,
            List<PubMedArticleDTO> articles
    ) {
        return SimulationResponse.builder()
                .id(simulation.getId())
                .scenarioId(simulation.getScenario().getId())
                .status(simulation.getStatus().name())
                .executedAt(simulation.getExecutedAt())
                .processingTimeMs(simulation.getProcessingTimeMs())
                .networkState(networkState)
                .connectionChanges(connectionChanges)
                .predictionSummary(simulation.getPredictionSummary())
                .confidenceScore(simulation.getConfidenceScore())
                .success(simulation.getSuccess())
                .badgeEarned(simulation.getBadgeEarned())
                .pubmedReferences(articles)
                .build();
    }

    /**
     * Convert PubMedReference entity to DTO
     */
    private PubMedArticleDTO convertToPubMedDTO(PubMedReference reference) {
        return PubMedArticleDTO.builder()
                .pubmedId(reference.getPubmedId())
                .title(reference.getTitle())
                .authors(reference.getAuthors())
                .publicationDate(reference.getPublicationDate())
                .abstractText(reference.getAbstractText())
                .articleUrl(reference.getArticleUrl())
                .relevanceScore(reference.getRelevanceScore())
                .keywords(reference.getKeywords())
                .build();
    }

    /**
     * Extract region code from full name
     */
    private String extractCode(String regionName) {
        Map<String, String> nameToCodes = Map.ofEntries(
                Map.entry("Medial Prefrontal Cortex", "mPFC"),
                Map.entry("Posterior Cingulate Cortex", "PCC"),
                Map.entry("Anterior Hippocampus", "AHP"),
                Map.entry("Amygdala", "AMY"),
                Map.entry("Visual Cortex", "V1"),
                Map.entry("Auditory Cortex", "A1"),
                Map.entry("Thalamus", "THL"),
                Map.entry("Anteromedial Caudate", "AMC"),
                Map.entry("Frontoparietal Regions", "FP"),
                Map.entry("Cerebellum", "CBL")
        );

        return nameToCodes.getOrDefault(regionName, regionName);
    }
}
