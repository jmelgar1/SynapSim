package com.synapsim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapsim.dto.PubMedArticleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
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

            // Step 2: Fetch article details
            return fetchArticleDetails(articleIds);

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
    private List<PubMedArticleDTO> fetchArticleDetails(List<String> articleIds) {
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

            // Parse JSON response
            return parseArticleSummaries(response, articleIds);

        } catch (Exception e) {
            log.error("Error fetching article details: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Parse article summaries from PubMed API response
     */
    private List<PubMedArticleDTO> parseArticleSummaries(String response, List<String> articleIds) {
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

                // Build article DTO
                PubMedArticleDTO dto = PubMedArticleDTO.builder()
                        .pubmedId(id)
                        .title(title)
                        .authors(authors)
                        .publicationDate(pubDate)
                        .articleUrl("https://pubmed.ncbi.nlm.nih.gov/" + id + "/")
                        .relevanceScore(1.0) // Default relevance, can be calculated based on search match
                        .build();

                articles.add(dto);
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
}
