package com.synapsim.controller;

import com.synapsim.dto.PubMedArticleDTO;
import com.synapsim.service.PubMedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Test controller for PubMed API integration
 */
@RestController
@RequestMapping("/test/pubmed")
@RequiredArgsConstructor
@Slf4j
public class PubMedTestController {

    private final PubMedService pubMedService;

    /**
     * Test PubMed API connection
     * GET /api/test/pubmed/connection
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        log.info("Testing PubMed API connection");

        boolean success = pubMedService.testConnection();

        return ResponseEntity.ok(Map.of(
                "status", success ? "success" : "failed",
                "message", success ? "PubMed API is accessible" : "Failed to connect to PubMed API",
                "baseUrl", "https://eutils.ncbi.nlm.nih.gov/entrez/eutils"
        ));
    }

    /**
     * Search PubMed with custom keywords
     * GET /api/test/pubmed/search?keywords=psilocybin,brain,neuroplasticity
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchArticles(
            @RequestParam List<String> keywords
    ) {
        log.info("Searching PubMed with keywords: {}", keywords);

        List<PubMedArticleDTO> articles = pubMedService.searchArticles(keywords);

        return ResponseEntity.ok(Map.of(
                "keywords", keywords,
                "totalResults", articles.size(),
                "articles", articles
        ));
    }

    /**
     * Test search with scenario parameters
     * GET /api/test/pubmed/scenario?compound=psilocybin&setting=meditation-space&region=prefrontal-cortex
     */
    @GetMapping("/scenario")
    public ResponseEntity<Map<String, Object>> searchByScenario(
            @RequestParam(required = false, defaultValue = "psilocybin") String compound,
            @RequestParam(required = false) String setting,
            @RequestParam(required = false) String region
    ) {
        log.info("Searching PubMed with scenario: compound={}, setting={}, region={}",
                compound, setting, region);

        // Generate keywords from scenario (null for researchFocus in test endpoint)
        List<String> keywords = pubMedService.generateSearchKeywords(compound, setting, region, null);

        // Search articles
        List<PubMedArticleDTO> articles = pubMedService.searchArticles(keywords);

        return ResponseEntity.ok(Map.of(
                "scenario", Map.of(
                        "compound", compound,
                        "setting", setting != null ? setting : "none",
                        "region", region != null ? region : "none"
                ),
                "generatedKeywords", keywords,
                "totalResults", articles.size(),
                "articles", articles
        ));
    }

    /**
     * Test with predefined quest scenarios
     * GET /api/test/pubmed/quest/anxiety
     */
    @GetMapping("/quest/{questType}")
    public ResponseEntity<Map<String, Object>> searchByQuest(
            @PathVariable String questType
    ) {
        log.info("Searching PubMed for quest type: {}", questType);

        List<String> keywords;

        // Predefined keywords based on quest type
        switch (questType.toLowerCase()) {
            case "anxiety":
                keywords = List.of("psilocybin", "anxiety", "amygdala", "prefrontal cortex", "neuroplasticity");
                break;
            case "empathy":
                keywords = List.of("mdma", "empathy", "default mode network", "social connection");
                break;
            case "creativity":
                keywords = List.of("lsd", "creativity", "divergent thinking", "brain connectivity");
                break;
            case "depression":
                keywords = List.of("psilocybin", "depression", "serotonin", "mood regulation");
                break;
            case "general":
                keywords = List.of("psychedelics", "neuroplasticity", "brain", "connectivity");
                break;
            default:
                keywords = List.of("psychedelics", "brain", "neuroplasticity");
        }

        List<PubMedArticleDTO> articles = pubMedService.searchArticles(keywords);

        return ResponseEntity.ok(Map.of(
                "questType", questType,
                "keywords", keywords,
                "totalResults", articles.size(),
                "articles", articles
        ));
    }

    /**
     * Simple ping endpoint
     * GET /api/test/pubmed/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "PubMed test controller is running",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
