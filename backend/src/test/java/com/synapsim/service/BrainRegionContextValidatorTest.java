package com.synapsim.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BrainRegionContextValidator
 * Demonstrates how the context validation prevents false positives from gene names
 * while correctly identifying genuine brain region mentions
 */
class BrainRegionContextValidatorTest {

    private BrainRegionContextValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BrainRegionContextValidator();
    }

    @Test
    void testRejectsGeneNameFalsePositive() {
        // This is the exact example the user provided
        String text = "Several of these genes, including Cebpb (CCAAT enhancer binding protein beta); " +
                "Iκβ-α (NFKBIA), a key regulator of NF-κB signaling; and Nr4a1 (Nur77) are all " +
                "involved in vascular inflammation and endothelial cell function";

        // The "a1" in "Nr4a1" should be rejected as it's part of a gene name
        int matchPosition = text.toLowerCase().indexOf("a1"); // Position of "a1" in "Nr4a1"

        boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, "a1", 2);

        assertFalse(isValid, "Should reject 'a1' when it appears as part of gene name 'Nr4a1'");
    }

    @Test
    void testAcceptsValidBrainRegionInNeuroscienceContext() {
        String text = "Increased activity in the primary auditory cortex (A1) was observed during " +
                "tone presentation, with significant BOLD signal changes in the superior temporal gyrus.";

        int matchPosition = text.toLowerCase().indexOf("a1");

        boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, "a1", 2);

        assertTrue(isValid, "Should accept 'A1' when discussing auditory cortex with neuroanatomical context");
    }

    @Test
    void testAcceptsLongDescriptiveAlias() {
        String text = "The amygdala showed increased connectivity with prefrontal regions.";

        int matchPosition = text.indexOf("amygdala");

        boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, "amygdala", 8);

        assertTrue(isValid, "Should accept long descriptive terms like 'amygdala'");
    }

    @Test
    void testRejectsShortAliasInMolecularContext() {
        String text = "The v1 variant of the receptor showed increased expression in mutant mice.";

        int matchPosition = text.indexOf("v1");

        boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, "v1", 2);

        assertFalse(isValid, "Should reject 'v1' when in molecular biology context (variant, receptor, expression)");
    }

    @Test
    void testAcceptsV1InVisualCortexContext() {
        String text = "V1 visual cortex activation during visual stimuli processing showed robust response.";

        int matchPosition = text.toLowerCase().indexOf("v1");

        boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, "v1", 2);

        assertTrue(isValid, "Should accept 'V1' when discussing visual cortex");
    }

    @Test
    void testRejectsMultipleGeneExamples() {
        String[] geneTexts = {
                "Expression of Slc6a4 gene in serotonergic neurons",
                "The MAP2K1 kinase pathway was activated",
                "Bdnf protein levels were measured"
        };

        String[] aliases = {"a4", "k1", "nf"};
        int index = 0;

        for (String text : geneTexts) {
            String alias = aliases[index];
            int matchPosition = text.toLowerCase().indexOf(alias);

            if (matchPosition != -1) {
                boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, alias, alias.length());
                assertFalse(isValid, "Should reject '" + alias + "' in gene context: " + text);
            }
            index++;
        }
    }

    @Test
    void testAcceptsHippocampalSubregions() {
        String text = "Hippocampal CA1 region showed enhanced synaptic plasticity and connectivity.";

        int matchPosition = text.toLowerCase().indexOf("ca1");

        boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, "ca1", 3);

        assertTrue(isValid, "Should accept 'CA1' when discussing hippocampal regions with neuroplasticity context");
    }

    @Test
    void testRejectsShortAliasWithoutContext() {
        String text = "The a1 section of the document describes the methodology.";

        int matchPosition = text.indexOf("a1");

        boolean isValid = validator.isValidBrainRegionMention(text, matchPosition, "a1", 2);

        assertFalse(isValid, "Should reject 'a1' when used as a list marker without neuroanatomical context");
    }
}
