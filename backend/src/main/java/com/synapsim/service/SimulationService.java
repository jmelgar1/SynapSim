package com.synapsim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapsim.dto.BrainNetworkDTO;
import com.synapsim.dto.PubMedArticleDTO;
import com.synapsim.dto.ScenarioRequest;
import com.synapsim.dto.SimulationResponse;
import com.synapsim.exception.NoResearchFoundException;
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

            // 5.1. Check if any research was found - if not, throw error
            if (articles.isEmpty()) {
                log.warn("No relevant research found for compound={}, setting={}",
                        scenario.getCompoundInspiration(), scenario.getTherapeuticSetting());
                throw new NoResearchFoundException(
                        "No relevant research found for the selected parameters. " +
                        "Please try a different combination of compound and therapeutic setting."
                );
            }

            // 6. Save PubMed references
            savePubMedReferences(simulation, articles);

            // 7. Generate network state JSON
            BrainNetworkDTO networkState = brainNetworkService.convertGraphToDTO(modifiedGraph);
            String networkStateJson = objectMapper.writeValueAsString(networkState);

            // 8. Generate connection changes JSON
            List<SimulationResponse.ConnectionChangeDTO> connectionChanges =
                    brainNetworkService.generateConnectionChanges(changes, originalGraph);

            // 8.1. Annotate connection changes with research findings
            annotateConnectionChangesWithResearch(connectionChanges, articles);

            String connectionChangesJson = objectMapper.writeValueAsString(connectionChanges);

            // 9. Generate prediction summary and confidence score
            String predictionSummary = generatePredictionSummary(scenario, connectionChanges, articles);
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
     * Generate prediction summary based on simulation results and actual research findings
     */
    private String generatePredictionSummary(
            Scenario scenario,
            List<SimulationResponse.ConnectionChangeDTO> changes,
            List<PubMedArticleDTO> articles
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

        // Get highest relevance article for insights
        PubMedArticleDTO topArticle = articles.stream()
                .max(Comparator.comparing(PubMedArticleDTO::getRelevanceScore))
                .orElse(articles.get(0));

        // Extract research insights from top articles
        String researchInsights = extractResearchInsights(articles, scenario);

        // Build summary starting with research context
        summary.append(String.format("Analysis of %d peer-reviewed studies reveals that ",
                articles.size()));
        summary.append(scenario.getCompoundInspiration().getValue());
        summary.append(" in a ");
        summary.append(scenario.getTherapeuticSetting().getValue());
        summary.append(" setting ");
        summary.append(researchInsights);
        summary.append(". ");

        // Add simulation-specific findings
        summary.append(String.format("Our simulation demonstrates these effects through %d strengthened and %d weakened neural connections. ",
                increases, decreases));

        summary.append("Key connectivity changes: ");
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

        // Add therapeutic implications based on research
        summary.append(generateResearchBasedImplications(scenario, articles, topChanges));

        return summary.toString();
    }

    /**
     * Extract insights from research articles to inform the prediction summary
     */
    private String extractResearchInsights(List<PubMedArticleDTO> articles, Scenario scenario) {
        // Analyze abstracts for common themes and findings
        Map<String, Integer> keyThemes = new HashMap<>();

        // Key terms to look for in abstracts
        String[] positiveTerms = {"increases", "enhances", "promotes", "improves", "strengthens",
                                  "facilitates", "induces", "augments", "elevates"};
        String[] mechanismTerms = {"connectivity", "neuroplasticity", "network", "communication",
                                   "integration", "synchrony", "coupling"};
        String[] outcomeTerms = {"depression", "anxiety", "mood", "wellbeing", "cognition",
                                "emotional", "therapeutic", "treatment"};

        // Count theme occurrences across top articles
        for (PubMedArticleDTO article : articles.stream().limit(5).toList()) {
            String abstractLower = article.getAbstractText() != null ?
                                  article.getAbstractText().toLowerCase() : "";

            for (String term : positiveTerms) {
                if (abstractLower.contains(term)) {
                    keyThemes.merge(term, 1, Integer::sum);
                }
            }
            for (String term : mechanismTerms) {
                if (abstractLower.contains(term)) {
                    keyThemes.merge(term, 1, Integer::sum);
                }
            }
            for (String term : outcomeTerms) {
                if (abstractLower.contains(term)) {
                    keyThemes.merge(term, 1, Integer::sum);
                }
            }
        }

        // Find most common mechanism and outcome terms
        String topMechanism = keyThemes.entrySet().stream()
                .filter(e -> Arrays.asList(mechanismTerms).contains(e.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("connectivity");

        String topOutcome = keyThemes.entrySet().stream()
                .filter(e -> Arrays.asList(outcomeTerms).contains(e.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("therapeutic");

        // Generate insight based on research themes
        return String.format("modulates brain %s with implications for %s outcomes",
                topMechanism, topOutcome);
    }

    /**
     * Generate therapeutic implications based on actual research findings
     */
    private String generateResearchBasedImplications(
            Scenario scenario,
            List<PubMedArticleDTO> articles,
            List<SimulationResponse.ConnectionChangeDTO> topChanges
    ) {
        StringBuilder implications = new StringBuilder();

        // Extract clinical contexts from research abstracts
        Set<String> clinicalContexts = new HashSet<>();
        for (PubMedArticleDTO article : articles.stream().limit(3).toList()) {
            String abstractLower = article.getAbstractText() != null ?
                                  article.getAbstractText().toLowerCase() : "";

            if (abstractLower.contains("depression") || abstractLower.contains("depressive")) {
                clinicalContexts.add("treatment-resistant depression");
            }
            if (abstractLower.contains("anxiety") || abstractLower.contains("anxious")) {
                clinicalContexts.add("anxiety disorders");
            }
            if (abstractLower.contains("ptsd") || abstractLower.contains("trauma")) {
                clinicalContexts.add("PTSD and trauma processing");
            }
            if (abstractLower.contains("creativity") || abstractLower.contains("creative")) {
                clinicalContexts.add("creative enhancement");
            }
            if (abstractLower.contains("empathy") || abstractLower.contains("social")) {
                clinicalContexts.add("social-emotional functioning");
            }
        }

        implications.append("Research suggests therapeutic potential for ");

        if (!clinicalContexts.isEmpty()) {
            implications.append(String.join(", ", clinicalContexts));
        } else {
            // Fallback to compound-specific defaults
            switch (scenario.getCompoundInspiration()) {
                case PSILOCYBIN:
                    implications.append("mood regulation and emotional processing");
                    break;
                case LSD:
                    implications.append("cognitive flexibility and perceptual changes");
                    break;
                case KETAMINE:
                    implications.append("rapid antidepressant effects and mood improvement");
                    break;
                case MDMA:
                    implications.append("empathy enhancement and trauma processing");
                    break;
            }
        }

        implications.append(". These findings align with the observed network reorganization in our simulation");

        return implications.toString();
    }

    /**
     * Annotate connection changes with relevant research findings
     * Scans research abstracts for mentions of brain regions
     */
    private void annotateConnectionChangesWithResearch(
            List<SimulationResponse.ConnectionChangeDTO> connectionChanges,
            List<PubMedArticleDTO> articles
    ) {
        // Map region codes to their full names and common variations
        Map<String, List<String>> regionAliases = buildRegionAliasMap();

        // Process top 5 most relevant articles
        for (PubMedArticleDTO article : articles.stream().limit(5).toList()) {
            String abstractLower = article.getAbstractText() != null ?
                    article.getAbstractText().toLowerCase() : "";
            String titleLower = article.getTitle().toLowerCase();
            String combinedText = titleLower + " " + abstractLower;

            // Check each connection change
            for (SimulationResponse.ConnectionChangeDTO change : connectionChanges) {
                // Skip if already has a research note
                if (change.getResearchNote() != null) {
                    continue;
                }

                String sourceCode = extractCode(change.getSourceRegion());
                String targetCode = extractCode(change.getTargetRegion());

                // Get possible names for these regions
                List<String> sourceNames = regionAliases.getOrDefault(sourceCode, List.of(sourceCode.toLowerCase()));
                List<String> targetNames = regionAliases.getOrDefault(targetCode, List.of(targetCode.toLowerCase()));

                // Check if either region is mentioned (more lenient than requiring both)
                boolean mentionsSourceRegion = false;
                boolean mentionsTargetRegion = false;

                for (String sourceName : sourceNames) {
                    if (combinedText.contains(sourceName)) {
                        mentionsSourceRegion = true;
                        break;
                    }
                }

                for (String targetName : targetNames) {
                    if (combinedText.contains(targetName)) {
                        mentionsTargetRegion = true;
                        break;
                    }
                }

                // Generate note if at least one region is mentioned
                if (mentionsSourceRegion || mentionsTargetRegion) {
                    String note = generateRegionBasedNote(
                            mentionsSourceRegion, mentionsTargetRegion,
                            sourceCode, targetCode, combinedText, change.getChangeType()
                    );
                    if (note != null) {
                        change.setResearchNote(note);
                    }
                }
            }
        }

        log.info("Annotated {} connection changes with research findings",
                connectionChanges.stream().filter(c -> c.getResearchNote() != null).count());
    }

    /**
     * Build a map of region codes to their common aliases in research literature
     */
    private Map<String, List<String>> buildRegionAliasMap() {
        Map<String, List<String>> aliases = new HashMap<>();

        aliases.put("mPFC", List.of("medial prefrontal cortex", "mpfc", "prefrontal cortex", "pfc"));
        aliases.put("PCC", List.of("posterior cingulate cortex", "pcc", "posterior cingulate", "default mode network", "dmn"));
        aliases.put("AHP", List.of("hippocampus", "anterior hippocampus", "hippocampal"));
        aliases.put("AMY", List.of("amygdala", "amygdalar"));
        aliases.put("V1", List.of("visual cortex", "v1", "occipital cortex", "visual"));
        aliases.put("A1", List.of("auditory cortex", "a1", "temporal cortex", "auditory"));
        aliases.put("THL", List.of("thalamus", "thalamic"));
        aliases.put("AMC", List.of("caudate", "anteromedial caudate", "striatum", "striatal"));
        aliases.put("FP", List.of("frontoparietal", "frontoparietal network", "attention network", "parietal"));
        aliases.put("CBL", List.of("cerebellum", "cerebellar"));

        return aliases;
    }

    /**
     * Generate a research note based on which brain regions are mentioned
     */
    private String generateRegionBasedNote(
            boolean mentionsSource, boolean mentionsTarget,
            String sourceCode, String targetCode,
            String text, String changeType
    ) {
        // Connectivity-related keywords to look for
        String[] connectivityKeywords = {"connectivity", "connection", "coupling", "communication",
                                        "network", "integration", "correlation", "functional connectivity"};

        boolean mentionsConnectivity = false;
        for (String keyword : connectivityKeywords) {
            if (text.contains(keyword)) {
                mentionsConnectivity = true;
                break;
            }
        }

        // Case 1: Both regions mentioned
        if (mentionsSource && mentionsTarget) {
            if (mentionsConnectivity) {
                return String.format("Research discusses %s-%s connectivity", sourceCode, targetCode);
            } else {
                return String.format("Research mentions both %s and %s regions", sourceCode, targetCode);
            }
        }

        // Case 2: Only one region mentioned
        String mentionedRegion = mentionsSource ? sourceCode : targetCode;

        // Look for common neuroscience terms
        boolean mentionsActivity = text.contains("activity") || text.contains("activation");
        boolean mentionsPlasticity = text.contains("plasticity") || text.contains("neuroplasticity");
        boolean mentionsFunctional = text.contains("functional") || text.contains("function");

        if (mentionsConnectivity) {
            return String.format("Research discusses %s connectivity patterns", mentionedRegion);
        } else if (mentionsPlasticity) {
            return String.format("Research reports neuroplasticity in %s", mentionedRegion);
        } else if (mentionsActivity) {
            return String.format("Research examines %s activity", mentionedRegion);
        } else if (mentionsFunctional) {
            return String.format("Research explores %s function", mentionedRegion);
        } else {
            return String.format("Research discusses %s region", mentionedRegion);
        }
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
