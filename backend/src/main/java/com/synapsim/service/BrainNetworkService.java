package com.synapsim.service;

import com.synapsim.dto.BrainNetworkDTO;
import com.synapsim.dto.SimulationResponse;
import com.synapsim.model.BrainRegion;
import com.synapsim.model.NeuralConnection;
import com.synapsim.model.Scenario;
import com.synapsim.repository.BrainRegionRepository;
import com.synapsim.repository.NeuralConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing brain network graph operations
 * Uses JGraphT for graph representation and manipulation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BrainNetworkService {

    private final BrainRegionRepository brainRegionRepository;
    private final NeuralConnectionRepository neuralConnectionRepository;

    @Value("${simulation.graph.min-connection-weight}")
    private Double minConnectionWeight;

    @Value("${simulation.graph.max-connection-weight}")
    private Double maxConnectionWeight;

    /**
     * Load brain network from database and build graph representation
     */
    public Graph<String, DefaultWeightedEdge> buildBrainGraph() {
        log.info("Building brain network graph from database");

        // Create weighted graph (undirected by default for most neural connections)
        Graph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Load all brain regions and add as vertices
        List<BrainRegion> regions = brainRegionRepository.findAll();
        for (BrainRegion region : regions) {
            graph.addVertex(region.getName());
        }

        log.debug("Added {} vertices to graph", regions.size());

        // Load all connections and add as edges
        List<NeuralConnection> connections = neuralConnectionRepository.findAll();
        for (NeuralConnection connection : connections) {
            String source = connection.getSourceRegion().getName();
            String target = connection.getTargetRegion().getName();

            if (graph.containsVertex(source) && graph.containsVertex(target)) {
                DefaultWeightedEdge edge = graph.addEdge(source, target);
                if (edge != null) {
                    graph.setEdgeWeight(edge, connection.getWeight());
                }
            }
        }

        log.info("Built brain graph with {} vertices and {} edges",
                graph.vertexSet().size(), graph.edgeSet().size());

        return graph;
    }

    /**
     * Apply neuroplasticity changes to the brain graph based on scenario parameters
     * This simulates how psychedelics might affect neural connectivity
     */
    public Map<String, Map<String, Double>> applyNeuroplasticityChanges(
            Graph<String, DefaultWeightedEdge> graph,
            Scenario scenario
    ) {
        log.info("Applying neuroplasticity changes for scenario: compound={}, setting={}",
                scenario.getCompoundInspiration(), scenario.getTherapeuticSetting());

        Map<String, Map<String, Double>> changes = new HashMap<>();

        // Get compound-specific modifiers
        Map<String, Double> compoundModifiers = getCompoundModifiers(scenario.getCompoundInspiration());

        // Get setting-specific modifiers
        Map<String, Double> settingModifiers = getSettingModifiers(scenario.getTherapeuticSetting());

        // Get duration multiplier
        double durationMultiplier = getDurationMultiplier(scenario.getSimulationDuration());

        // Apply changes to each edge in the graph
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            String source = graph.getEdgeSource(edge);
            String target = graph.getEdgeTarget(edge);
            double currentWeight = graph.getEdgeWeight(edge);

            // Calculate weight change based on modifiers
            double weightChange = calculateWeightChange(
                    source, target, currentWeight,
                    compoundModifiers, settingModifiers, durationMultiplier
            );

            double newWeight = Math.max(minConnectionWeight,
                    Math.min(maxConnectionWeight, currentWeight + weightChange));

            // Update graph
            graph.setEdgeWeight(edge, newWeight);

            // Track changes
            if (Math.abs(weightChange) > 0.01) { // Only track significant changes
                changes.computeIfAbsent(source, k -> new HashMap<>())
                        .put(target, newWeight - currentWeight);
            }
        }

        log.info("Applied neuroplasticity changes to {} connections", changes.size());
        return changes;
    }

    /**
     * Calculate weight change for a specific connection based on scenario parameters
     */
    private double calculateWeightChange(
            String source, String target, double currentWeight,
            Map<String, Double> compoundModifiers,
            Map<String, Double> settingModifiers,
            double durationMultiplier
    ) {
        // Base change starts at 0
        double change = 0.0;

        // Get connection-specific key (e.g., "PFC-AMY")
        String connectionKey = getConnectionKey(source, target);

        // Apply compound-specific modifier
        double compoundEffect = compoundModifiers.getOrDefault(connectionKey, 0.0);
        change += compoundEffect;

        // Apply setting-specific modifier
        double settingEffect = settingModifiers.getOrDefault(connectionKey, 0.0);
        change += settingEffect;

        // Apply duration multiplier
        change *= durationMultiplier;

        // Add some variability (±10%) to simulate individual differences
        double variability = (Math.random() - 0.5) * 0.2;
        change *= (1.0 + variability);

        return change;
    }

    /**
     * Get compound-specific modifiers for neural connections
     * Based on research about how different compounds affect brain connectivity
     */
    private Map<String, Double> getCompoundModifiers(Scenario.CompoundType compound) {
        Map<String, Double> modifiers = new HashMap<>();

        switch (compound) {
            case PSILOCYBIN:
                // Psilocybin reduces DMN connectivity, increases emotion regulation
                modifiers.put("PCC-mPFC", -0.15);  // Decrease core DMN connectivity (ego dissolution)
                modifiers.put("AMY-mPFC", 0.20);   // Increase emotional regulation
                modifiers.put("FP-mPFC", 0.15);    // Enhanced cognitive control
                modifiers.put("AHP-AMY", -0.10);   // Reduced fear memory formation
                modifiers.put("AMY-FP", 0.18);     // Enhanced emotion regulation via attention
                modifiers.put("AHP-PCC", -0.12);   // Reduced rumination
                break;

            case LSD:
                // LSD increases global connectivity, especially creative networks
                modifiers.put("PCC-V1", 0.25);     // Enhanced visual-DMN integration (hallucinations)
                modifiers.put("mPFC-V1", 0.20);    // Visual imagery during introspection
                modifiers.put("FP-mPFC", 0.18);    // Creative problem-solving
                modifiers.put("AMC-mPFC", 0.15);   // Motivation and exploration
                modifiers.put("PCC-mPFC", -0.10);  // Reduced self-focused thinking
                modifiers.put("A1-V1", 0.22);      // Cross-modal sensory integration (synesthesia)
                break;

            case KETAMINE:
                // Ketamine has rapid antidepressant effects, reduces DMN hyperconnectivity
                modifiers.put("PCC-mPFC", -0.25);  // Strong DMN reduction
                modifiers.put("AHP-mPFC", 0.22);   // Enhanced neuroplasticity
                modifiers.put("AMY-mPFC", 0.18);   // Improved emotion regulation
                modifiers.put("FP-mPFC", 0.15);    // Cognitive flexibility
                modifiers.put("AHP-AMC", 0.12);    // Reward-memory integration
                break;

            case MDMA:
                // MDMA enhances empathy, social bonding, reduces fear response
                modifiers.put("AMY-FP", 0.30);     // Strong empathy enhancement via emotion-attention
                modifiers.put("AMY-mPFC", 0.25);   // Enhanced emotional regulation
                modifiers.put("AHP-AMY", -0.18);   // Reduced fear conditioning
                modifiers.put("AMC-mPFC", 0.20);   // Prosocial motivation
                modifiers.put("PCC-mPFC", 0.15);   // Enhanced self-awareness (opposite of other psychedelics)
                break;
        }

        return modifiers;
    }

    /**
     * Get setting-specific modifiers
     * Therapeutic setting can enhance or diminish effects
     */
    private Map<String, Double> getSettingModifiers(Scenario.TherapeuticSetting setting) {
        Map<String, Double> modifiers = new HashMap<>();

        switch (setting) {
            case CALM_NATURE:
                // Calm environment enhances stress reduction
                modifiers.put("AMY-mPFC", 0.10);   // Better emotional regulation
                modifiers.put("FP-mPFC", 0.08);    // Mindful awareness
                modifiers.put("PCC-mPFC", -0.05);  // Reduced rumination
                break;

            case GUIDED_THERAPY:
                // Therapeutic guidance enhances healing pathways
                modifiers.put("AMY-mPFC", 0.12);   // Stronger emotion regulation
                modifiers.put("AMY-FP", 0.10);     // Enhanced introspection via attention
                modifiers.put("AHP-mPFC", 0.08);   // Memory reprocessing
                break;

            case MEDITATION_SPACE:
                // Meditation enhances introspective and awareness pathways
                modifiers.put("AMY-FP", 0.15);     // Strong interoceptive awareness via emotion-attention
                modifiers.put("FP-mPFC", 0.12);    // Mindful attention
                modifiers.put("PCC-mPFC", -0.08);  // Present-moment focus
                break;

            case CREATIVE_STUDIO:
                // Creative environment enhances divergent thinking
                modifiers.put("mPFC-V1", 0.12);    // Visual creativity
                modifiers.put("AMC-mPFC", 0.10);   // Creative motivation
                modifiers.put("PCC-mPFC", 0.08);   // Imaginative thinking
                break;

            case SOCIAL_GATHERING:
                // Social setting enhances empathy and bonding
                modifiers.put("AMY-FP", 0.12);     // Social empathy via emotion-attention
                modifiers.put("AMC-FP", 0.10);     // Social reward
                modifiers.put("AMY-FP", 0.08);     // Emotional resonance
                break;
        }

        return modifiers;
    }

    /**
     * Get duration multiplier
     */
    private double getDurationMultiplier(Scenario.SimulationDuration duration) {
        return switch (duration) {
            case SHORT -> 0.7;
            case MEDIUM -> 1.0;
            case EXTENDED -> 1.3;
        };
    }

    /**
     * Generate connection key from region names (normalized)
     */
    private String getConnectionKey(String region1, String region2) {
        // Extract codes from region names for standardized keys
        String code1 = extractRegionCode(region1);
        String code2 = extractRegionCode(region2);

        // Always use alphabetical order for consistency
        if (code1.compareTo(code2) < 0) {
            return code1 + "-" + code2;
        } else {
            return code2 + "-" + code1;
        }
    }

    /**
     * Extract region code from full name
     */
    private String extractRegionCode(String regionName) {
        // Map full names to codes
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

    /**
     * Build comprehensive region alias map for matching brain regions in research articles
     * Maps region codes to common variations and alternate names found in literature
     */
    public Map<String, List<String>> buildRegionAliasMap() {
        Map<String, List<String>> aliases = new HashMap<>();

        // Original 10 core regions
        aliases.put("mPFC", List.of("medial prefrontal cortex", "mpfc", "prefrontal", "pfc", "medial prefrontal"));
        aliases.put("PCC", List.of("posterior cingulate cortex", "pcc", "posterior cingulate"));
        aliases.put("AHP", List.of("anterior hippocampus", "hippocampus", "hippocampal"));
        aliases.put("AMY", List.of("amygdala", "amygdalae"));
        aliases.put("V1", List.of("visual cortex", "primary visual", "v1", "striate cortex", "occipital"));
        aliases.put("A1", List.of("auditory cortex", "primary auditory", "a1", "temporal"));
        aliases.put("THL", List.of("thalamus", "thalamic"));
        aliases.put("AMC", List.of("anteromedial caudate", "caudate", "striatum"));
        aliases.put("FP", List.of("frontoparietal", "frontoparietal regions", "fronto-parietal"));
        aliases.put("CBL", List.of("cerebellum", "cerebellar"));

        // Brainstem regions
        aliases.put("MED", List.of("medulla oblongata", "medulla", "brainstem"));
        aliases.put("LC", List.of("locus coeruleus", "locus ceruleus", "noradrenergic", "norepinephrine", "lc"));
        aliases.put("DRN", List.of("dorsal raphe nucleus", "dorsal raphe", "raphe nucleus", "raphe nuclei", "serotonergic", "5-ht", "drn"));
        aliases.put("PON", List.of("pons", "pontine"));
        aliases.put("CV", List.of("cerebellar vermis", "vermis"));
        aliases.put("DN", List.of("dentate nucleus", "cerebellar dentate"));

        // Midbrain regions
        aliases.put("PAG", List.of("periaqueductal gray", "periaqueductal grey", "pag", "central gray"));
        aliases.put("VTA", List.of("ventral tegmental area", "ventral tegmental", "vta", "dopamine", "dopaminergic"));
        aliases.put("SN", List.of("substantia nigra", "nigra", "sn"));
        aliases.put("SC", List.of("superior colliculus", "superior colliculi", "colliculus"));
        aliases.put("IC", List.of("inferior colliculus", "inferior colliculi"));

        // Diencephalon - Epithalamus
        aliases.put("PIN", List.of("pineal gland", "pineal", "epiphysis"));
        aliases.put("HB", List.of("habenular nuclei", "habenula", "habenular"));

        // Thalamus expanded
        aliases.put("MD", List.of("medial dorsal nucleus", "mediodorsal", "md thalamus", "medial dorsal thalamus"));
        aliases.put("PUL", List.of("pulvinar", "pulvinar nucleus"));
        aliases.put("LGN", List.of("lateral geniculate nucleus", "lateral geniculate", "lgn"));
        aliases.put("MGN", List.of("medial geniculate nucleus", "medial geniculate", "mgn"));
        aliases.put("AN", List.of("anterior thalamic nuclei", "anterior thalamus"));
        aliases.put("ILN", List.of("intralaminar nuclei", "intralaminar thalamus"));

        // Hypothalamus expanded
        aliases.put("SCN", List.of("suprachiasmatic nucleus", "suprachiasmatic", "scn", "circadian"));
        aliases.put("PVN", List.of("paraventricular nucleus", "paraventricular", "pvn", "hpa axis"));
        aliases.put("VMH", List.of("ventromedial hypothalamus", "ventromedial", "vmh"));
        aliases.put("LH", List.of("lateral hypothalamus", "lateral", "lh"));
        aliases.put("MB", List.of("mammillary bodies", "mammillary", "mammillary body"));

        // Basal Ganglia
        aliases.put("PUT", List.of("putamen"));
        aliases.put("CD", List.of("caudate nucleus", "caudate"));
        aliases.put("NAcc", List.of("nucleus accumbens", "accumbens", "ventral striatum", "nacc", "reward", "nac"));
        aliases.put("GP", List.of("globus pallidus", "pallidum"));
        aliases.put("STN", List.of("subthalamic nucleus", "subthalamic", "stn"));

        // Basal Forebrain
        aliases.put("NBM", List.of("nucleus basalis", "nucleus basalis of meynert", "nbm", "basal nucleus", "acetylcholine"));
        aliases.put("SN_BF", List.of("septal nuclei", "septum", "septal"));

        // Hippocampal Formation
        aliases.put("DG", List.of("dentate gyrus", "dentate"));
        aliases.put("CA1", List.of("ca1", "cornu ammonis 1", "hippocampus ca1", "hippocampal ca1"));
        aliases.put("CA3", List.of("ca3", "cornu ammonis 3", "hippocampus ca3", "hippocampal ca3"));
        aliases.put("EC", List.of("entorhinal cortex", "entorhinal", "ec"));
        aliases.put("PHC", List.of("parahippocampal cortex", "parahippocampal", "phc"));

        // Amygdala Complex
        aliases.put("BLA", List.of("basolateral amygdala", "basolateral", "bla"));
        aliases.put("CEA", List.of("central amygdala", "central nucleus amygdala", "cea"));
        aliases.put("BNST", List.of("bed nucleus of stria terminalis", "bed nucleus", "bnst", "extended amygdala"));

        // Insula
        aliases.put("AI", List.of("anterior insula", "insular cortex anterior", "insula", "ai"));
        aliases.put("PI", List.of("posterior insula", "insular cortex posterior", "pi"));

        // Cingulate Cortex
        aliases.put("ACC", List.of("anterior cingulate cortex", "anterior cingulate", "acc"));
        aliases.put("sgACC", List.of("subgenual anterior cingulate", "subgenual", "subgenual acc", "area 25", "ba25", "brodmann area 25", "sgacc"));
        aliases.put("RSC", List.of("retrosplenial cortex", "retrosplenial", "rsc"));
        aliases.put("PCUN", List.of("precuneus", "medial parietal", "precuneal"));

        // Prefrontal Cortex expanded
        aliases.put("dlPFC", List.of("dorsolateral prefrontal cortex", "dorsolateral prefrontal", "dlpfc", "lateral prefrontal"));
        aliases.put("vmPFC", List.of("ventromedial prefrontal cortex", "ventromedial prefrontal", "vmpfc", "medial prefrontal ventral"));
        aliases.put("OFC", List.of("orbitofrontal cortex", "orbitofrontal", "orbital frontal", "ofc"));
        aliases.put("vlPFC", List.of("ventrolateral prefrontal cortex", "ventrolateral prefrontal", "vlpfc"));
        aliases.put("dmPFC", List.of("dorsomedial prefrontal cortex", "dorsomedial prefrontal", "dmpfc"));

        // Motor and Somatosensory
        aliases.put("M1", List.of("primary motor cortex", "motor cortex", "m1", "precentral gyrus"));
        aliases.put("PMC", List.of("premotor cortex", "premotor", "pmc"));
        aliases.put("SMA", List.of("supplementary motor area", "sma", "supplementary motor"));
        aliases.put("S1", List.of("primary somatosensory cortex", "somatosensory cortex", "s1", "postcentral gyrus"));
        aliases.put("S2", List.of("secondary somatosensory cortex", "s2"));

        // Parietal Cortex
        aliases.put("PPC", List.of("posterior parietal cortex", "posterior parietal", "ppc"));
        aliases.put("IPL", List.of("inferior parietal lobule", "inferior parietal", "ipl"));
        aliases.put("SPL", List.of("superior parietal lobule", "superior parietal", "spl"));

        // Temporal Cortex
        aliases.put("STG", List.of("superior temporal gyrus", "superior temporal", "stg"));
        aliases.put("MTG", List.of("middle temporal gyrus", "middle temporal", "mtg"));
        aliases.put("ITG", List.of("inferior temporal gyrus", "inferior temporal", "itg"));
        aliases.put("FG", List.of("fusiform gyrus", "fusiform", "fg", "face area"));
        aliases.put("TP", List.of("temporal pole", "anterior temporal"));

        // Occipital/Visual Cortex expanded
        aliases.put("V2", List.of("visual area 2", "v2", "secondary visual"));
        aliases.put("V3", List.of("visual area 3", "v3"));
        aliases.put("V4", List.of("visual area 4", "v4", "color area"));
        aliases.put("V5", List.of("visual area 5", "v5", "mt", "motion area"));
        aliases.put("CUN", List.of("cuneus", "cuneal"));

        // Other regions
        aliases.put("CLA", List.of("claustrum", "claustral", "consciousness"));
        aliases.put("PIR", List.of("piriform cortex", "piriform", "primary olfactory", "olfactory"));

        return aliases;
    }

    /**
     * Build filtered brain graph containing only regions mentioned in research abstracts
     * This creates a focused network based on what the research actually discusses
     *
     * @param mentionedRegionCodes Set of region codes (e.g., "DRN", "mPFC") found in abstracts
     * @return Graph containing only the mentioned regions and their connections
     */
    public Graph<String, DefaultWeightedEdge> buildFilteredBrainGraph(Set<String> mentionedRegionCodes) {
        log.info("Building filtered brain graph with {} mentioned regions", mentionedRegionCodes.size());

        Graph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Load all brain regions
        List<BrainRegion> allRegions = brainRegionRepository.findAll();
        Map<String, BrainRegion> codeToRegion = allRegions.stream()
                .collect(Collectors.toMap(BrainRegion::getCode, r -> r));

        // Add only mentioned regions as vertices
        for (String code : mentionedRegionCodes) {
            BrainRegion region = codeToRegion.get(code);
            if (region != null) {
                graph.addVertex(region.getName());
            } else {
                log.warn("Region code {} mentioned in research but not found in database", code);
            }
        }

        log.debug("Added {} vertices to filtered graph", graph.vertexSet().size());

        // Add connections between mentioned regions only
        List<NeuralConnection> connections = neuralConnectionRepository.findAll();
        for (NeuralConnection connection : connections) {
            String sourceCode = connection.getSourceRegion().getCode();
            String targetCode = connection.getTargetRegion().getCode();

            // Only add edge if both regions are mentioned in research
            if (mentionedRegionCodes.contains(sourceCode) && mentionedRegionCodes.contains(targetCode)) {
                String source = connection.getSourceRegion().getName();
                String target = connection.getTargetRegion().getName();

                if (graph.containsVertex(source) && graph.containsVertex(target)) {
                    DefaultWeightedEdge edge = graph.addEdge(source, target);
                    if (edge != null) {
                        graph.setEdgeWeight(edge, connection.getWeight());
                    }
                }
            }
        }

        log.info("Built filtered brain graph with {} vertices and {} edges",
                graph.vertexSet().size(), graph.edgeSet().size());

        return graph;
    }

    /**
     * Convert graph to DTO for frontend visualization
     */
    public BrainNetworkDTO convertGraphToDTO(Graph<String, DefaultWeightedEdge> graph) {
        List<BrainNetworkDTO.NodeDTO> nodes = new ArrayList<>();
        List<BrainNetworkDTO.EdgeDTO> edges = new ArrayList<>();

        // Convert vertices to nodes
        List<BrainRegion> regions = brainRegionRepository.findAll();
        Map<String, BrainRegion> regionMap = regions.stream()
                .collect(Collectors.toMap(BrainRegion::getName, r -> r));

        for (String vertex : graph.vertexSet()) {
            BrainRegion region = regionMap.get(vertex);
            if (region != null) {
                nodes.add(BrainNetworkDTO.NodeDTO.builder()
                        .id(region.getId().toString())
                        .name(region.getName())
                        .code(region.getCode())
                        .description(region.getDescription())
                        .positionX(region.getPositionX())
                        .positionY(region.getPositionY())
                        .activityLevel(region.getBaselineActivity())
                        .build());
            }
        }

        // Convert edges to connections
        // Load all neural connections to get metadata
        List<NeuralConnection> connections = neuralConnectionRepository.findAll();
        Map<String, NeuralConnection> connectionMap = new HashMap<>();
        for (NeuralConnection conn : connections) {
            String key = conn.getSourceRegion().getName() + "-" + conn.getTargetRegion().getName();
            connectionMap.put(key, conn);
        }

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            String source = graph.getEdgeSource(edge);
            String target = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);

            BrainRegion sourceRegion = regionMap.get(source);
            BrainRegion targetRegion = regionMap.get(target);

            if (sourceRegion != null && targetRegion != null) {
                // Look up the connection metadata
                String connectionKey = source + "-" + target;
                NeuralConnection connection = connectionMap.get(connectionKey);

                edges.add(BrainNetworkDTO.EdgeDTO.builder()
                        .id(connection != null ? connection.getId().toString() : null)
                        .source(sourceRegion.getId().toString())
                        .target(targetRegion.getId().toString())
                        .weight(weight)
                        .connectionType(connection != null ? connection.getConnectionType().name() : null)
                        .isBidirectional(connection != null ? connection.getIsBidirectional() : null)
                        .build());
            }
        }

        return BrainNetworkDTO.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    // Method removed - no longer using connection changes, using mentioned regions instead
}
