package com.synapsim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapsim.dto.BrainNetworkDTO;
import com.synapsim.dto.MentionedRegionDTO;
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

            // 3. Search for relevant research articles FIRST
            List<PubMedArticleDTO> articles = searchRelevantResearch(scenario);

            // 3.1. Check if any research was found - if not, throw error
            if (articles.isEmpty()) {
                log.warn("No relevant research found for compound={}, setting={}",
                        scenario.getCompoundInspiration(), scenario.getTherapeuticSetting());
                throw new NoResearchFoundException(
                        "No relevant research found for the selected parameters. " +
                        "Please try a different combination of compound and therapeutic setting."
                );
            }

            // 4. Extract brain regions mentioned in research abstracts
            Map<String, List<String>> regionAliasMap = brainNetworkService.buildRegionAliasMap();
            Set<String> mentionedRegionCodes = pubMedService.extractMentionedRegions(articles, regionAliasMap);

            log.info("Found {} brain regions mentioned in research articles: {}",
                    mentionedRegionCodes.size(), mentionedRegionCodes);

            // 5. Build filtered brain network graph with only mentioned regions
            Graph<String, DefaultWeightedEdge> originalGraph = brainNetworkService.buildFilteredBrainGraph(mentionedRegionCodes);
            Graph<String, DefaultWeightedEdge> modifiedGraph = brainNetworkService.buildFilteredBrainGraph(mentionedRegionCodes);

            // 6. Apply neuroplasticity changes to filtered graph
            Map<String, Map<String, Double>> changes = brainNetworkService.applyNeuroplasticityChanges(
                    modifiedGraph, scenario
            );

            // 7. Save PubMed references
            savePubMedReferences(simulation, articles);

            // 8. Generate network state JSON
            BrainNetworkDTO networkState = brainNetworkService.convertGraphToDTO(modifiedGraph);
            String networkStateJson = objectMapper.writeValueAsString(networkState);

            // 9. Extract mentioned regions with research context
            List<MentionedRegionDTO> mentionedRegions = extractMentionedRegionsWithContext(articles, mentionedRegionCodes);
            String mentionedRegionsJson = objectMapper.writeValueAsString(mentionedRegions);

            log.info("Extracted {} brain regions with research context", mentionedRegions.size());

            // 10. Generate prediction summary and confidence score
            String predictionSummary = generatePredictionSummary(scenario, mentionedRegions, articles);
            double confidenceScore = calculateConfidenceScore(mentionedRegions, articles.size());

            // 11. Determine success and badge
            boolean success = determineSuccess(confidenceScore, mentionedRegions);
            String badge = determineBadge(scenario, success, mentionedRegions);

            // 12. Update simulation with results
            simulation.setStatus(Simulation.SimulationStatus.COMPLETED);
            simulation.setNetworkState(networkStateJson);
            simulation.setConnectionChanges(mentionedRegionsJson); // Reusing field to store mentioned regions
            simulation.setPredictionSummary(predictionSummary);
            simulation.setConfidenceScore(confidenceScore);
            simulation.setSuccess(success);
            simulation.setBadgeEarned(badge);

            long processingTime = System.currentTimeMillis() - startTime;
            simulation.setProcessingTimeMs(processingTime);

            simulation = simulationRepository.save(simulation);

            log.info("Simulation completed in {}ms with confidence score: {}", processingTime, confidenceScore);

            // 13. Build and return response
            return buildSimulationResponse(simulation, networkState, mentionedRegions, articles);

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

            List<MentionedRegionDTO> mentionedRegions = objectMapper.readValue(
                    simulation.getConnectionChanges(),
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class, MentionedRegionDTO.class
                    )
            );

            // Get PubMed references
            List<PubMedArticleDTO> articles = simulation.getPubmedReferences().stream()
                    .map(this::convertToPubMedDTO)
                    .collect(Collectors.toList());

            return buildSimulationResponse(simulation, networkState, mentionedRegions, articles);

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
                        List<MentionedRegionDTO> mentionedRegions = objectMapper.readValue(
                                sim.getConnectionChanges(),
                                objectMapper.getTypeFactory().constructCollectionType(
                                        List.class, MentionedRegionDTO.class
                                )
                        );
                        List<PubMedArticleDTO> articles = sim.getPubmedReferences().stream()
                                .map(this::convertToPubMedDTO)
                                .collect(Collectors.toList());

                        return buildSimulationResponse(sim, networkState, mentionedRegions, articles);
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
            reference.setFullText(article.getFullText());
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
            List<MentionedRegionDTO> mentionedRegions,
            List<PubMedArticleDTO> articles
    ) {
        StringBuilder summary = new StringBuilder();

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

        // Add brain regions mentioned in research
        summary.append(String.format("Research specifically discusses %d brain regions: ",
                mentionedRegions.size()));

        List<String> topRegions = mentionedRegions.stream()
                .limit(5)
                .map(MentionedRegionDTO::getRegionCode)
                .collect(Collectors.toList());

        summary.append(String.join(", ", topRegions));
        summary.append(". ");

        // Add therapeutic implications based on research
        summary.append(generateResearchBasedImplications(scenario, articles, mentionedRegions));

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
            List<MentionedRegionDTO> mentionedRegions
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

        implications.append(". These findings align with the brain regions identified in our analysis");

        return implications.toString();
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
     * Get full region name from code
     */
    private String getRegionName(String code) {
        Map<String, String> codeToName = Map.ofEntries(
                Map.entry("mPFC", "Medial Prefrontal Cortex"),
                Map.entry("PCC", "Posterior Cingulate Cortex"),
                Map.entry("AHP", "Anterior Hippocampus"),
                Map.entry("AMY", "Amygdala"),
                Map.entry("V1", "Visual Cortex"),
                Map.entry("A1", "Auditory Cortex"),
                Map.entry("THL", "Thalamus"),
                Map.entry("AMC", "Anteromedial Caudate"),
                Map.entry("FP", "Frontoparietal Regions"),
                Map.entry("CBL", "Cerebellum")
        );
        return codeToName.getOrDefault(code, code);
    }

    /**
     * Extract mentioned brain regions from research articles with context
     */
    private List<MentionedRegionDTO> extractMentionedRegionsWithContext(
            List<PubMedArticleDTO> articles,
            Set<String> mentionedRegionCodes
    ) {
        Map<String, List<String>> regionAliases = buildRegionAliasMap();
        Map<String, MentionedRegionDTO> regionMap = new HashMap<>();

        // Initialize DTOs for each mentioned region
        for (String regionCode : mentionedRegionCodes) {
            MentionedRegionDTO dto = MentionedRegionDTO.builder()
                    .regionCode(regionCode)
                    .regionName(getRegionName(regionCode))
                    .mentions(new ArrayList<>())
                    .build();
            regionMap.put(regionCode, dto);
        }

        // Scan articles for mentions
        for (PubMedArticleDTO article : articles) {
            String abstractText = article.getAbstractText() != null ? article.getAbstractText() : "";
            String fullText = article.getFullText() != null ? article.getFullText() : "";
            String title = article.getTitle() != null ? article.getTitle() : "";

            // Combine all text sources
            String combinedText = title + " " + abstractText + " " + fullText;
            String lowerText = combinedText.toLowerCase();

            // Check each mentioned region
            for (String regionCode : mentionedRegionCodes) {
                List<String> aliases = regionAliases.getOrDefault(regionCode, List.of(regionCode.toLowerCase()));

                // Find sentences mentioning this region
                for (String alias : aliases) {
                    if (lowerText.contains(alias.toLowerCase())) {
                        // Extract relevant excerpt containing the region mention
                        String excerpt = extractExcerpt(combinedText, alias);
                        String context = determineContext(excerpt);

                        if (excerpt != null && !excerpt.isEmpty()) {
                            MentionedRegionDTO.ResearchMention mention = MentionedRegionDTO.ResearchMention.builder()
                                    .articleTitle(article.getTitle())
                                    .pubmedId(article.getPubmedId())
                                    .excerpt(excerpt)
                                    .context(context)
                                    .build();

                            regionMap.get(regionCode).addMention(mention);
                            break; // Only one mention per article per region
                        }
                    }
                }
            }
        }

        // Filter out regions with no mentions and return sorted by mention count
        return regionMap.values().stream()
                .filter(dto -> dto.getMentions() != null && !dto.getMentions().isEmpty())
                .sorted((a, b) -> Integer.compare(b.getMentions().size(), a.getMentions().size()))
                .collect(Collectors.toList());
    }

    /**
     * Extract a relevant excerpt around the region mention
     */
    private String extractExcerpt(String text, String keyword) {
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        int index = lowerText.indexOf(lowerKeyword);
        if (index == -1) {
            return null;
        }

        // Find sentence boundaries
        int sentenceStart = text.lastIndexOf('.', index);
        if (sentenceStart == -1) sentenceStart = 0;
        else sentenceStart++; // Skip the period

        int sentenceEnd = text.indexOf('.', index + keyword.length());
        if (sentenceEnd == -1) sentenceEnd = text.length();
        else sentenceEnd++; // Include the period

        // Extract the sentence
        String excerpt = text.substring(sentenceStart, sentenceEnd).trim();

        // If excerpt is too long, try to shorten it
        if (excerpt.length() > 300) {
            // Try to get just the clause containing the keyword
            int start = Math.max(0, index - 150);
            int end = Math.min(text.length(), index + keyword.length() + 150);
            excerpt = "..." + text.substring(start, end).trim() + "...";
        }

        return excerpt;
    }

    /**
     * Determine the context of the region mention
     */
    private String determineContext(String excerpt) {
        String lowerExcerpt = excerpt.toLowerCase();

        if (lowerExcerpt.contains("connectivity") || lowerExcerpt.contains("connection") ||
            lowerExcerpt.contains("network") || lowerExcerpt.contains("coupling")) {
            return "connectivity";
        } else if (lowerExcerpt.contains("activity") || lowerExcerpt.contains("activation") ||
                   lowerExcerpt.contains("active")) {
            return "activity";
        } else if (lowerExcerpt.contains("neuroplasticity") || lowerExcerpt.contains("plasticity") ||
                   lowerExcerpt.contains("synaptic")) {
            return "neuroplasticity";
        } else if (lowerExcerpt.contains("volume") || lowerExcerpt.contains("density") ||
                   lowerExcerpt.contains("structure")) {
            return "structure";
        } else if (lowerExcerpt.contains("function") || lowerExcerpt.contains("functional")) {
            return "function";
        } else {
            return "general";
        }
    }

    /**
     * Calculate confidence score based on simulation quality
     */
    private double calculateConfidenceScore(
            List<MentionedRegionDTO> mentionedRegions,
            int researchCount
    ) {
        double score = 0.0;

        // Base score from research availability (0-0.4)
        score += Math.min(0.4, researchCount * 0.08);

        // Score from number of mentioned regions (0-0.3)
        score += Math.min(0.3, mentionedRegions.size() * 0.05);

        // Score from number of research mentions across regions (0-0.3)
        int totalMentions = mentionedRegions.stream()
                .mapToInt(r -> r.getMentions() != null ? r.getMentions().size() : 0)
                .sum();
        score += Math.min(0.3, totalMentions * 0.03);

        return Math.min(1.0, score);
    }

    /**
     * Determine if simulation was successful
     */
    private boolean determineSuccess(
            double confidenceScore,
            List<MentionedRegionDTO> mentionedRegions
    ) {
        // Success criteria:
        // 1. Confidence score above 0.5
        // 2. At least 3 brain regions mentioned in research
        boolean highConfidence = confidenceScore >= 0.5;
        boolean sufficientRegions = mentionedRegions.size() >= 3;

        return highConfidence && sufficientRegions;
    }

    /**
     * Determine badge earned based on scenario and results
     */
    private String determineBadge(
            Scenario scenario,
            boolean success,
            List<MentionedRegionDTO> mentionedRegions
    ) {
        if (!success) {
            return null;
        }

        // Determine badge based on quest type
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
            List<MentionedRegionDTO> mentionedRegions,
            List<PubMedArticleDTO> articles
    ) {
        return SimulationResponse.builder()
                .id(simulation.getId())
                .scenarioId(simulation.getScenario().getId())
                .status(simulation.getStatus().name())
                .executedAt(simulation.getExecutedAt())
                .processingTimeMs(simulation.getProcessingTimeMs())
                .networkState(networkState)
                .mentionedRegions(mentionedRegions)
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
                .fullText(reference.getFullText())
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
