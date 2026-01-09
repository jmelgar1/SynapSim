package com.synapsim.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates brain region mentions by analyzing surrounding context
 * to distinguish genuine neuroanatomical references from false positives
 * (e.g., gene names, chemical compounds, list markers)
 */
@Slf4j
@Component
public class BrainRegionContextValidator {

    private static final int CONTEXT_WINDOW_SIZE = 100; // characters before/after match

    // Words that indicate genuine brain region discussion
    private static final Set<String> NEUROANATOMICAL_INDICATORS = Set.of(
            // Anatomical terms
            "cortex", "cortical", "region", "regions", "area", "areas",
            "brain", "cerebral", "neural", "neuronal", "lobe", "lobes",
            "gyrus", "sulcus", "nucleus", "nuclei", "pathway", "pathways",
            "matter", "tissue", "structure", "structures",
            "hippocampus", "hippocampal", "amygdala", "thalamus", "thalamic",

            // Functional terms
            "activation", "activity", "activated", "deactivation",
            "connectivity", "connection", "connected", "network", "networks",
            "functional", "processing", "response", "responses",
            "signal", "signaling", "firing", "discharge",

            // Neuroplasticity and synaptic terms
            "synaptic", "synapse", "plasticity", "neuroplasticity",
            "potentiation", "depression", "pruning", "sprouting",

            // Measurement/imaging terms
            "volume", "density", "thickness", "fmri", "pet", "mri",
            "imaging", "scan", "voxel", "bold", "hemodynamic",

            // Neuroscience context
            "cognitive", "sensory", "motor", "attention", "memory",
            "emotional", "affective", "reward", "learning", "perception"
    );

    // Words that indicate gene/protein context (false positives)
    private static final Set<String> MOLECULAR_INDICATORS = Set.of(
            // Gene terminology
            "gene", "genes", "genetic", "allele", "alleles", "locus", "loci",
            "expression", "expressed", "transcript", "transcription",
            "mutation", "mutant", "polymorphism", "variant", "variants",
            "promoter", "enhancer", "coding", "encode", "encodes",

            // Protein terminology
            "protein", "proteins", "receptor", "receptors", "kinase",
            "enzyme", "enzymes", "antibody", "binding", "mrna",
            "peptide", "ligand", "substrate", "catalytic",

            // Molecular biology methods
            "pcr", "western", "blot", "immunohistochemistry",
            "sequencing", "genotype", "phenotype", "knockout"
    );

    // Patterns that indicate gene names
    // More specific to avoid catching brain regions like CA1, V1
    private static final Pattern GENE_NAME_PATTERN = Pattern.compile(
            // Matches patterns like: Nr4a1, Slc6a4, MAP2K1, Bdnf, etc.
            "\\b[A-Z][a-z]{2,}[0-9]+[a-z]+[0-9]*\\b|" +  // Nr4a1, Slc6a4 (mixed case with letters after number)
            "\\b[A-Z]{3,}[0-9]+[a-z]*[0-9]*\\b|" +       // MAP2K1, NFKBIA (3+ uppercase letters)
            "\\([A-Z][a-z]+[0-9]+[a-z]*\\)|" +            // (Nr4a1)
            "\\b[A-Z][a-z]{2,}[0-9]+\\b"                  // Bdnf, Syn1 (3+ total chars before number)
    );

    /**
     * Validates whether a matched alias is a genuine brain region mention
     *
     * @param fullText The complete article text (not lowercased)
     * @param matchPosition Position where the alias was found
     * @param matchedAlias The alias that was matched (lowercase)
     * @param aliasLength Original length of the alias
     * @return true if this appears to be a valid brain region mention
     */
    public boolean isValidBrainRegionMention(
            String fullText,
            int matchPosition,
            String matchedAlias,
            int aliasLength) {

        // Extract context window around the match
        int contextStart = Math.max(0, matchPosition - CONTEXT_WINDOW_SIZE);
        int contextEnd = Math.min(fullText.length(), matchPosition + aliasLength + CONTEXT_WINDOW_SIZE);
        String context = fullText.substring(contextStart, contextEnd);
        String lowerContext = context.toLowerCase();

        // FILTER 1: Check for gene name patterns in immediate vicinity
        int localStart = Math.max(0, matchPosition - 15);
        int localEnd = Math.min(fullText.length(), matchPosition + aliasLength + 15);
        String localWindow = fullText.substring(localStart, localEnd);

        if (GENE_NAME_PATTERN.matcher(localWindow).find()) {
            log.debug("Rejected '{}' - appears in gene name pattern: {}", matchedAlias, localWindow);
            return false;
        }

        // FILTER 2: Short aliases (<=2 chars) require stronger validation
        if (matchedAlias.length() <= 2) {
            // Check if it's part of a larger alphanumeric string (gene name)
            if (isPartOfGeneIdentifier(fullText, matchPosition, aliasLength)) {
                log.debug("Rejected '{}' - part of gene identifier", matchedAlias);
                return false;
            }

            // Short aliases need at least one strong neuroanatomical indicator
            boolean hasStrongIndicator = false;
            for (String indicator : NEUROANATOMICAL_INDICATORS) {
                if (lowerContext.contains(indicator)) {
                    hasStrongIndicator = true;
                    break;
                }
            }

            if (!hasStrongIndicator) {
                log.debug("Rejected '{}' - short alias without neuroanatomical context", matchedAlias);
                return false;
            }
        }

        // FILTER 3: Score the context
        int neuroScore = 0;
        int molecularScore = 0;

        // Count neuroanatomical indicators
        for (String indicator : NEUROANATOMICAL_INDICATORS) {
            if (lowerContext.contains(indicator)) {
                neuroScore++;
            }
        }

        // Count molecular biology indicators
        for (String indicator : MOLECULAR_INDICATORS) {
            if (lowerContext.contains(indicator)) {
                molecularScore++;
            }
        }

        // FILTER 4: Apply scoring logic
        if (molecularScore > neuroScore) {
            log.debug("Rejected '{}' - molecular context dominates (neuro={}, molecular={})",
                    matchedAlias, neuroScore, molecularScore);
            return false;
        }

        // Accept if neuroanatomical context is present
        if (neuroScore > 0) {
            log.debug("Accepted '{}' - valid neuroanatomical context (neuro={}, molecular={})",
                    matchedAlias, neuroScore, molecularScore);
            return true;
        }

        // For longer aliases (>5 chars), accept even without strong context
        // "amygdala", "hippocampus" are unlikely to be false positives
        if (matchedAlias.length() > 5) {
            log.debug("Accepted '{}' - long descriptive alias", matchedAlias);
            return true;
        }

        // Default: reject ambiguous cases
        log.debug("Rejected '{}' - ambiguous context", matchedAlias);
        return false;
    }

    /**
     * Checks if the match is part of a gene identifier like "Nr4a1"
     */
    private boolean isPartOfGeneIdentifier(String text, int position, int length) {
        // Check characters immediately before and after
        char before = (position > 0) ? text.charAt(position - 1) : ' ';
        char after = (position + length < text.length()) ? text.charAt(position + length) : ' ';

        // If surrounded by alphanumeric characters, likely part of gene name
        return Character.isLetterOrDigit(before) || Character.isLetterOrDigit(after);
    }
}
