package com.synapsim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapsim.dto.PubMedArticleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
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
     * Fetch article details by IDs including full abstracts
     */
    private List<PubMedArticleDTO> fetchArticleDetails(List<String> articleIds, List<String> searchKeywords, Map<String, Double> keywordWeights) {
        try {
            // Join IDs with commas
            String ids = String.join(",", articleIds);

            // Use efetch instead of esummary to get full abstracts
            String url = String.format("%s/efetch.fcgi?db=pubmed&id=%s&retmode=xml",
                    pubmedBaseUrl, ids);

            log.debug("PubMed fetch URL: {}", url);

            // Make API call
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                return new ArrayList<>();
            }

            // Parse XML response with relevance scoring
            return parseArticleXml(response, searchKeywords, keywordWeights);

        } catch (Exception e) {
            log.error("Error fetching article details: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Parse article details from PubMed XML response including abstracts
     */
    private List<PubMedArticleDTO> parseArticleXml(String xmlResponse, List<String> searchKeywords, Map<String, Double> keywordWeights) {
        List<PubMedArticleDTO> articles = new ArrayList<>();

        try {
            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));
            doc.getDocumentElement().normalize();

            // Get all PubmedArticle elements
            NodeList articleNodes = doc.getElementsByTagName("PubmedArticle");

            for (int i = 0; i < articleNodes.getLength(); i++) {
                Element articleElement = (Element) articleNodes.item(i);

                // Extract PubMed ID
                String pubmedId = getTextContent(articleElement, "PMID");

                // Extract title
                String title = getTextContent(articleElement, "ArticleTitle");

                // Extract abstract - combine all AbstractText elements
                String abstractText = extractAbstract(articleElement);

                // Extract publication date
                String pubDate = extractPublicationDate(articleElement);

                // Extract authors
                String authors = extractAuthors(articleElement);

                // Build article DTO
                PubMedArticleDTO dto = PubMedArticleDTO.builder()
                        .pubmedId(pubmedId)
                        .title(title)
                        .authors(authors)
                        .publicationDate(pubDate)
                        .abstractText(abstractText)
                        .articleUrl("https://pubmed.ncbi.nlm.nih.gov/" + pubmedId + "/")
                        .keywords(String.join(", ", searchKeywords))
                        .build();

                // Calculate relevance score based on abstract content
                double relevance = calculateRelevanceScore(dto, searchKeywords, keywordWeights);
                dto.setRelevanceScore(relevance);

                articles.add(dto);

                log.debug("Article: {} | Abstract length: {} | Relevance: {}",
                        title, abstractText != null ? abstractText.length() : 0, relevance);
            }

        } catch (Exception e) {
            log.error("Error parsing article XML: {}", e.getMessage(), e);
        }

        return articles;
    }

    /**
     * Extract text content from XML element by tag name
     */
    private String getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent();
        }
        return "";
    }

    /**
     * Extract abstract text from article element
     * Abstracts can have multiple AbstractText elements (structured abstracts)
     */
    private String extractAbstract(Element articleElement) {
        NodeList abstractNodes = articleElement.getElementsByTagName("AbstractText");
        if (abstractNodes.getLength() == 0) {
            return "";
        }

        StringBuilder abstractBuilder = new StringBuilder();
        for (int i = 0; i < abstractNodes.getLength(); i++) {
            Element abstractTextElement = (Element) abstractNodes.item(i);

            // Some abstracts have labels (BACKGROUND, METHODS, etc.)
            String label = abstractTextElement.getAttribute("Label");
            if (label != null && !label.isEmpty()) {
                abstractBuilder.append(label).append(": ");
            }

            abstractBuilder.append(abstractTextElement.getTextContent());

            if (i < abstractNodes.getLength() - 1) {
                abstractBuilder.append(" ");
            }
        }

        return abstractBuilder.toString();
    }

    /**
     * Extract publication date from article element
     */
    private String extractPublicationDate(Element articleElement) {
        NodeList pubDateNodes = articleElement.getElementsByTagName("PubDate");
        if (pubDateNodes.getLength() > 0) {
            Element pubDateElement = (Element) pubDateNodes.item(0);

            String year = getTextContent(pubDateElement, "Year");
            String month = getTextContent(pubDateElement, "Month");

            if (!year.isEmpty()) {
                return month.isEmpty() ? year : month + " " + year;
            }
        }
        return "";
    }

    /**
     * Extract authors from article element
     */
    private String extractAuthors(Element articleElement) {
        NodeList authorNodes = articleElement.getElementsByTagName("Author");
        List<String> authorNames = new ArrayList<>();

        for (int i = 0; i < Math.min(authorNodes.getLength(), 3); i++) { // Limit to first 3 authors
            Element authorElement = (Element) authorNodes.item(i);

            String lastName = getTextContent(authorElement, "LastName");
            String foreName = getTextContent(authorElement, "ForeName");

            if (!lastName.isEmpty()) {
                String fullName = foreName.isEmpty() ? lastName : lastName + " " + foreName.charAt(0);
                authorNames.add(fullName);
            }
        }

        if (authorNodes.getLength() > 3) {
            authorNames.add("et al");
        }

        return String.join(", ", authorNames);
    }

    /**
     * Generate search keywords based on scenario parameters
     */
    public List<String> generateSearchKeywords(String compoundType, String therapeuticSetting, String brainRegion) {
        List<String> keywords = new ArrayList<>();

        // Add compound-related keywords
        keywords.add(compoundType);
        keywords.add("neuroplasticity");

        // NOTE: Brain region excluded from keywords to broaden search results
        // The specific brain region is still used in the simulation logic,
        // but not in PubMed searches to avoid overly narrow results

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

        log.debug("Generated keywords (brain region excluded): {}", keywords);
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
     * Calculate relevance score for an article based on keyword matches in title AND abstract
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
            String abstractText = article.getAbstractText() != null ? article.getAbstractText().toLowerCase() : "";

            double baseScore = 0.0;
            int titleMatches = 0;
            int abstractMatches = 0;
            int totalOccurrences = 0;

            // Calculate base score from keyword matches in both title and abstract
            for (String keyword : searchKeywords) {
                String normalizedKeyword = keyword.toLowerCase().trim();

                // Check title matches (weighted higher)
                if (title.contains(normalizedKeyword)) {
                    titleMatches++;

                    // Title matches get full weight
                    double weight = keywordWeights.getOrDefault(normalizedKeyword, generalKeywordWeight);
                    baseScore += weight;

                    // Count occurrences in title
                    int titleOccurrences = countOccurrences(title, normalizedKeyword);
                    totalOccurrences += titleOccurrences;
                }

                // Check abstract matches (weighted lower than title but still important)
                if (!abstractText.isEmpty() && abstractText.contains(normalizedKeyword)) {
                    abstractMatches++;

                    // Abstract matches get 70% of full weight
                    double weight = keywordWeights.getOrDefault(normalizedKeyword, generalKeywordWeight);
                    baseScore += weight * 0.7;

                    // Count occurrences in abstract
                    int abstractOccurrences = countOccurrences(abstractText, normalizedKeyword);
                    totalOccurrences += abstractOccurrences;
                }
            }

            // Apply bonuses
            double bonus = 0.0;

            // Title match bonus: if keywords appear in title (high prominence)
            if (titleMatches > 0) {
                bonus += titleMatchBonus;
            }

            // Abstract coverage bonus: reward articles where keywords appear in abstract
            if (abstractMatches > 0) {
                double abstractCoverage = (double) abstractMatches / searchKeywords.size();
                bonus += abstractCoverage * 0.10; // Up to 0.10 bonus for full abstract coverage
            }

            // Multiple occurrence bonus: keywords appearing multiple times indicate higher relevance
            if (totalOccurrences > (titleMatches + abstractMatches)) {
                int extraOccurrences = totalOccurrences - (titleMatches + abstractMatches);
                bonus += Math.min(extraOccurrences * multipleOccurrenceBonus, 0.15); // Cap bonus at 0.15
            }

            // Calculate final score and cap at 1.0
            double finalScore = Math.min(1.0, baseScore + bonus);

            log.debug("Relevance calculation - Title matches: {}, Abstract matches: {}, Base: {}, Bonus: {}, Final: {}",
                    titleMatches, abstractMatches, baseScore, bonus, finalScore);

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
