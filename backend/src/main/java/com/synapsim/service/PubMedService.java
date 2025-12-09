package com.synapsim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapsim.dto.PubMedArticleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for interacting with PubMed API (eUtils)
 */
@Service
@Slf4j
public class PubMedService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${pubmed.api.base-url}")
    private String pubmedBaseUrl;

    @Value("${pubmed.api.max-results}")
    private Integer maxResults;

    @Value("${pubmed.relevance.critical-keyword-weight}")
    private Double criticalKeywordWeight;

    @Value("${pubmed.relevance.important-keyword-weight}")
    private Double importantKeywordWeight;

    @Value("${pubmed.relevance.general-keyword-weight}")
    private Double generalKeywordWeight;

    @Value("${pubmed.relevance.title-match-bonus}")
    private Double titleMatchBonus;

    @Value("${pubmed.relevance.multiple-occurrence-bonus}")
    private Double multipleOccurrenceBonus;

    public PubMedService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Search PubMed for articles based on keywords
     *
     * @param keywords List of search keywords
     * @return List of PubMed article DTOs
     */
    public List<PubMedArticleDTO> searchArticles(List<String> keywords) {
        try {
            log.info("Searching PubMed with keywords: {}", keywords);

            // Step 1: Search for article IDs
            List<String> articleIds = searchArticleIds(keywords);

            if (articleIds.isEmpty()) {
                log.warn("No articles found for keywords: {}", keywords);
                return new ArrayList<>();
            }

            log.info("Found {} article IDs", articleIds.size());

            // Step 2: Build keyword weights for relevance scoring
            Map<String, Double> keywordWeights = buildKeywordWeights(keywords);

            // Step 3: Fetch article details with relevance scoring
            return fetchArticleDetails(articleIds, keywords, keywordWeights);

        } catch (Exception e) {
            log.error("Error searching PubMed: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Search for article IDs based on keywords
     */
    private List<String> searchArticleIds(List<String> keywords) {
        try {
            // Build search query
            String query = String.join("+", keywords);

            String url = String.format("%s/esearch.fcgi?db=pubmed&term=%s&retmode=json&retmax=%d",
                    pubmedBaseUrl, query, maxResults);

            log.debug("PubMed search URL: {}", url);

            // Make API call
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                return new ArrayList<>();
            }

            // Parse JSON response
            JsonNode root = objectMapper.readTree(response);
            JsonNode idList = root.path("esearchresult").path("idlist");

            List<String> ids = new ArrayList<>();
            if (idList.isArray()) {
                idList.forEach(id -> ids.add(id.asText()));
            }

            return ids;

        } catch (Exception e) {
            log.error("Error searching article IDs: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetch article details by IDs
     */
    private List<PubMedArticleDTO> fetchArticleDetails(List<String> articleIds, List<String> searchKeywords, Map<String, Double> keywordWeights) {
        try {
            // Join IDs with commas
            String ids = String.join(",", articleIds);

            String url = String.format("%s/esummary.fcgi?db=pubmed&id=%s&retmode=json",
                    pubmedBaseUrl, ids);

            log.debug("PubMed summary URL: {}", url);

            // Make API call
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                return new ArrayList<>();
            }

            // Parse JSON response with relevance scoring
            return parseArticleSummaries(response, articleIds, searchKeywords, keywordWeights);

        } catch (Exception e) {
            log.error("Error fetching article details: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Parse article summaries from PubMed API response
     */
    private List<PubMedArticleDTO> parseArticleSummaries(String response, List<String> articleIds, List<String> searchKeywords, Map<String, Double> keywordWeights) {
        List<PubMedArticleDTO> articles = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.path("result");

            for (String id : articleIds) {
                JsonNode article = result.path(id);

                if (article.isMissingNode()) {
                    continue;
                }

                // Extract article information
                String title = article.path("title").asText("");
                String pubDate = article.path("pubdate").asText("");

                // Extract authors
                JsonNode authorsNode = article.path("authors");
                String authors = "";
                if (authorsNode.isArray()) {
                    List<String> authorNames = new ArrayList<>();
                    authorsNode.forEach(author -> {
                        authorNames.add(author.path("name").asText());
                    });
                    authors = String.join(", ", authorNames);
                }

                // Build article DTO with calculated relevance score
                PubMedArticleDTO dto = PubMedArticleDTO.builder()
                        .pubmedId(id)
                        .title(title)
                        .authors(authors)
                        .publicationDate(pubDate)
                        .articleUrl("https://pubmed.ncbi.nlm.nih.gov/" + id + "/")
                        .build();

                // Calculate relevance score based on title
                double relevance = calculateRelevanceScore(dto, searchKeywords, keywordWeights);
                dto.setRelevanceScore(relevance);

                articles.add(dto);

                log.debug("Article: {} | Relevance: {}", title, relevance);
            }

        } catch (Exception e) {
            log.error("Error parsing article summaries: {}", e.getMessage(), e);
        }

        return articles;
    }

    /**
     * Generate search keywords based on scenario parameters
     */
    public List<String> generateSearchKeywords(String compoundType, String therapeuticSetting, String brainRegion) {
        List<String> keywords = new ArrayList<>();

        // Add compound-related keywords
        keywords.add(compoundType);
        keywords.add("neuroplasticity");

        // Add brain region if specified
        if (brainRegion != null && !brainRegion.isEmpty()) {
            keywords.add(brainRegion.replace("-", " "));
        }

        // Add setting-related keywords
        if (therapeuticSetting != null && !therapeuticSetting.isEmpty()) {
            if (therapeuticSetting.contains("therapy")) {
                keywords.add("therapy");
            }
            if (therapeuticSetting.contains("meditation")) {
                keywords.add("meditation");
            }
        }

        // Add general neuroscience keywords
        keywords.add("brain");
        keywords.add("connectivity");

        log.debug("Generated keywords: {}", keywords);
        return keywords;
    }

    /**
     * Test method to verify PubMed API connectivity
     */
    public boolean testConnection() {
        try {
            log.info("Testing PubMed API connection...");
            List<String> testKeywords = List.of("psilocybin", "brain");
            List<String> ids = searchArticleIds(testKeywords);

            boolean success = !ids.isEmpty();
            log.info("PubMed API test {}", success ? "PASSED" : "FAILED");
            return success;

        } catch (Exception e) {
            log.error("PubMed API test FAILED: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Build keyword weights map for relevance scoring
     * Keywords are categorized as critical, important, or general based on their role in the search
     *
     * @param keywords List of search keywords
     * @return Map of keyword to weight
     */
    private Map<String, Double> buildKeywordWeights(List<String> keywords) {
        Map<String, Double> weights = new HashMap<>();

        // Define neuroscience-specific terms that should be weighted higher
        Set<String> criticalTerms = Set.of(
                "psilocybin", "lsd", "mdma", "ketamine", "dmt",  // Compounds
                "amygdala", "hippocampus", "prefrontal cortex", "default mode network",
                "anterior cingulate", "insula", "thalamus"  // Brain regions
        );

        Set<String> importantTerms = Set.of(
                "neuroplasticity", "anxiety", "depression", "ptsd", "creativity",
                "empathy", "connectivity", "serotonin", "dopamine", "therapy"  // Mechanisms and conditions
        );

        // Assign weights based on keyword categorization
        for (String keyword : keywords) {
            String normalizedKeyword = keyword.toLowerCase().trim();

            if (criticalTerms.contains(normalizedKeyword)) {
                weights.put(normalizedKeyword, criticalKeywordWeight);
            } else if (importantTerms.contains(normalizedKeyword)) {
                weights.put(normalizedKeyword, importantKeywordWeight);
            } else {
                weights.put(normalizedKeyword, generalKeywordWeight);
            }
        }

        log.debug("Built keyword weights: {}", weights);
        return weights;
    }

    /**
     * Calculate relevance score for an article based on keyword matches in the title
     * Score is based on weighted keyword matching with bonuses for title prominence
     *
     * @param article The article to score
     * @param searchKeywords Original search keywords
     * @param keywordWeights Weight map for each keyword
     * @return Relevance score between 0.0 and 1.0
     */
    private double calculateRelevanceScore(PubMedArticleDTO article, List<String> searchKeywords, Map<String, Double> keywordWeights) {
        try {
            String title = article.getTitle().toLowerCase();
            double baseScore = 0.0;
            int matchedKeywords = 0;
            int totalOccurrences = 0;

            // Calculate base score from keyword matches
            for (String keyword : searchKeywords) {
                String normalizedKeyword = keyword.toLowerCase().trim();

                if (title.contains(normalizedKeyword)) {
                    matchedKeywords++;

                    // Add weight for this keyword
                    double weight = keywordWeights.getOrDefault(normalizedKeyword, generalKeywordWeight);
                    baseScore += weight;

                    // Count occurrences for frequency bonus
                    int occurrences = countOccurrences(title, normalizedKeyword);
                    totalOccurrences += occurrences;
                }
            }

            // Apply bonuses
            double bonus = 0.0;

            // Title match bonus: if keywords appear in title (which we're already searching)
            if (matchedKeywords > 0) {
                bonus += titleMatchBonus;
            }

            // Multiple occurrence bonus: keywords appearing multiple times indicate higher relevance
            if (totalOccurrences > matchedKeywords) {
                int extraOccurrences = totalOccurrences - matchedKeywords;
                bonus += Math.min(extraOccurrences * multipleOccurrenceBonus, 0.15); // Cap bonus at 0.15
            }

            // Calculate final score and cap at 1.0
            double finalScore = Math.min(1.0, baseScore + bonus);

            log.debug("Relevance calculation - Keywords matched: {}/{}, Base score: {}, Bonus: {}, Final: {}",
                    matchedKeywords, searchKeywords.size(), baseScore, bonus, finalScore);

            return finalScore;

        } catch (Exception e) {
            log.error("Error calculating relevance score: {}", e.getMessage(), e);
            return 0.5; // Default to medium relevance on error
        }
    }

    /**
     * Count occurrences of a keyword in text
     */
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }

        return count;
    }
}
