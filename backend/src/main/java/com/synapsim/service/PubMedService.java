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
    private final BrainRegionContextValidator contextValidator;

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

    public PubMedService(WebClient webClient, BrainRegionContextValidator contextValidator) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
        this.contextValidator = contextValidator;
    }

    /**
     * Search PubMed and PMC for articles based on keywords
     *
     * @param keywords List of search keywords
     * @return List of PubMed article DTOs (combined from both sources)
     */
    public List<PubMedArticleDTO> searchArticles(List<String> keywords) {
        try {
            log.info("Searching PubMed and PMC with keywords: {}", keywords);

            // Build keyword weights for relevance scoring
            Map<String, Double> keywordWeights = buildKeywordWeights(keywords);

            // Step 1: Search PubMed for article IDs
            List<String> pubmedIds = searchArticleIds(keywords);
            log.info("Found {} article IDs from PubMed", pubmedIds.size());

            // Step 2: Search PMC for open access article IDs
            List<String> pmcIds = searchPMCArticleIds(keywords);
            log.info("Found {} article IDs from PMC", pmcIds.size());

            // Step 3: Fetch detailed information from PubMed
            List<PubMedArticleDTO> pubmedArticles = new ArrayList<>();
            if (!pubmedIds.isEmpty()) {
                pubmedArticles = fetchArticleDetails(pubmedIds, keywords, keywordWeights);
                log.info("Fetched {} articles from PubMed with details", pubmedArticles.size());
            }

            // Step 4: Fetch detailed information from PMC (with full text)
            List<PubMedArticleDTO> pmcArticles = new ArrayList<>();
            if (!pmcIds.isEmpty()) {
                pmcArticles = fetchPMCArticleDetails(pmcIds, keywords, keywordWeights);
                log.info("Fetched {} articles from PMC with full text", pmcArticles.size());
            }

            // Step 5: Combine results, avoiding duplicates
            List<PubMedArticleDTO> combinedArticles = combineArticles(pubmedArticles, pmcArticles);
            log.info("Successfully combined {} unique articles", combinedArticles.size());

            return combinedArticles;

        } catch (Exception e) {
            log.error("Error searching PubMed/PMC: {}", e.getMessage(), e);
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
            List<PubMedArticleDTO> articles = parseArticleXml(response, searchKeywords, keywordWeights);

            // Attempt to fetch full text from PMC for each article
            for (PubMedArticleDTO article : articles) {
                String fullText = fetchFullTextFromPMC(article.getPubmedId());
                article.setFullText(fullText);
            }

            return articles;

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
     * Search PMC for open access article IDs
     */
    private List<String> searchPMCArticleIds(List<String> keywords) {
        try {
            // Build search query
            String query = String.join("+", keywords);

            // Search PMC database for open access articles
            String url = String.format("%s/esearch.fcgi?db=pmc&term=%s&retmode=json&retmax=%d",
                    pubmedBaseUrl, query, maxResults);

            log.debug("PMC search URL: {}", url);

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

            List<String> pmcIds = new ArrayList<>();
            if (idList.isArray()) {
                for (JsonNode idNode : idList) {
                    pmcIds.add(idNode.asText());
                }
            }

            log.info("Found {} PMC article IDs", pmcIds.size());
            return pmcIds;

        } catch (Exception e) {
            log.error("Error searching PMC: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetch article details from PMC with full text
     */
    private List<PubMedArticleDTO> fetchPMCArticleDetails(
            List<String> pmcIds,
            List<String> searchKeywords,
            Map<String, Double> keywordWeights
    ) {
        try {
            List<PubMedArticleDTO> articles = new ArrayList<>();

            // Fetch each PMC article individually to get full text
            for (String pmcId : pmcIds) {
                PubMedArticleDTO article = fetchSinglePMCArticle(pmcId, searchKeywords, keywordWeights);
                if (article != null) {
                    articles.add(article);
                }
            }

            return articles;

        } catch (Exception e) {
            log.error("Error fetching PMC article details: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetch a single PMC article with full text
     */
    private PubMedArticleDTO fetchSinglePMCArticle(
            String pmcId,
            List<String> searchKeywords,
            Map<String, Double> keywordWeights
    ) {
        try {
            // Fetch full XML from PMC
            String url = String.format("%s/efetch.fcgi?db=pmc&id=%s&retmode=xml",
                    pubmedBaseUrl, pmcId);

            log.debug("Fetching PMC article: {}", pmcId);

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isEmpty()) {
                log.debug("No response for PMC ID: {}", pmcId);
                return null;
            }

            // Parse PMC XML to extract article details and full text
            return parsePMCArticleXml(response, pmcId, searchKeywords, keywordWeights);

        } catch (Exception e) {
            log.warn("Error fetching PMC article {}: {}", pmcId, e.getMessage());
            return null;
        }
    }

    /**
     * Parse PMC XML response to extract article details including full text
     */
    private PubMedArticleDTO parsePMCArticleXml(
            String xmlResponse,
            String pmcId,
            List<String> searchKeywords,
            Map<String, Double> keywordWeights
    ) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));
            doc.getDocumentElement().normalize();

            // Extract PubMed ID if available
            String pubmedId = pmcId; // Default to PMC ID
            NodeList pmidNodes = doc.getElementsByTagName("article-id");
            for (int i = 0; i < pmidNodes.getLength(); i++) {
                Element pmidElement = (Element) pmidNodes.item(i);
                if ("pmid".equals(pmidElement.getAttribute("pub-id-type"))) {
                    pubmedId = pmidElement.getTextContent().trim();
                    break;
                }
            }

            // Extract title
            String title = "";
            NodeList titleNodes = doc.getElementsByTagName("article-title");
            if (titleNodes.getLength() > 0) {
                title = titleNodes.item(0).getTextContent().trim();
            }

            // Extract authors
            StringBuilder authors = new StringBuilder();
            NodeList contribNodes = doc.getElementsByTagName("contrib");
            int authorCount = 0;
            for (int i = 0; i < contribNodes.getLength() && authorCount < 3; i++) {
                Element contrib = (Element) contribNodes.item(i);
                if ("author".equals(contrib.getAttribute("contrib-type"))) {
                    NodeList surnameNodes = contrib.getElementsByTagName("surname");
                    NodeList givenNamesNodes = contrib.getElementsByTagName("given-names");

                    if (surnameNodes.getLength() > 0) {
                        if (authors.length() > 0) authors.append(", ");
                        if (givenNamesNodes.getLength() > 0) {
                            authors.append(givenNamesNodes.item(0).getTextContent().trim()).append(" ");
                        }
                        authors.append(surnameNodes.item(0).getTextContent().trim());
                        authorCount++;
                    }
                }
            }
            if (authorCount < contribNodes.getLength()) {
                authors.append(" et al.");
            }

            // Extract publication date
            String publicationDate = "";
            NodeList pubDateNodes = doc.getElementsByTagName("pub-date");
            if (pubDateNodes.getLength() > 0) {
                Element pubDate = (Element) pubDateNodes.item(0);
                NodeList monthNodes = pubDate.getElementsByTagName("month");
                NodeList yearNodes = pubDate.getElementsByTagName("year");

                if (monthNodes.getLength() > 0 && yearNodes.getLength() > 0) {
                    String monthNum = monthNodes.item(0).getTextContent().trim();
                    String year = yearNodes.item(0).getTextContent().trim();
                    // Convert month number to name
                    String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    String month = monthNum;
                    try {
                        int monthIndex = Integer.parseInt(monthNum) - 1;
                        if (monthIndex >= 0 && monthIndex < 12) {
                            month = monthNames[monthIndex];
                        }
                    } catch (NumberFormatException e) {
                        // Keep month as is if it's not a number
                    }
                    publicationDate = month + " " + year;
                } else if (yearNodes.getLength() > 0) {
                    publicationDate = yearNodes.item(0).getTextContent().trim();
                }
            }

            // Extract abstract
            String abstractText = "";
            NodeList abstractNodes = doc.getElementsByTagName("abstract");
            if (abstractNodes.getLength() > 0) {
                abstractText = abstractNodes.item(0).getTextContent().trim();
            }

            // Extract full text body
            String fullText = parseFullTextXml(xmlResponse);

            // Build article URL (use PubMed URL if we have PMID, otherwise PMC URL)
            String articleUrl = pubmedId.equals(pmcId)
                    ? String.format("https://www.ncbi.nlm.nih.gov/pmc/articles/PMC%s/", pmcId)
                    : String.format("https://pubmed.ncbi.nlm.nih.gov/%s/", pubmedId);

            // Build temporary article to calculate relevance score
            PubMedArticleDTO tempArticle = PubMedArticleDTO.builder()
                    .pubmedId(pubmedId)
                    .title(title)
                    .authors(authors.toString())
                    .publicationDate(publicationDate)
                    .abstractText(abstractText)
                    .fullText(fullText)
                    .articleUrl(articleUrl)
                    .keywords(String.join(", ", searchKeywords))
                    .build();

            // Calculate relevance score
            double relevanceScore = calculateRelevanceScore(tempArticle, searchKeywords, keywordWeights);

            return PubMedArticleDTO.builder()
                    .pubmedId(pubmedId)
                    .title(title)
                    .authors(authors.toString())
                    .publicationDate(publicationDate)
                    .abstractText(abstractText)
                    .fullText(fullText)
                    .articleUrl(articleUrl)
                    .relevanceScore(relevanceScore)
                    .keywords(String.join(", ", searchKeywords))
                    .build();

        } catch (Exception e) {
            log.error("Error parsing PMC XML for article {}: {}", pmcId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Combine articles from PubMed and PMC, avoiding duplicates
     * Prioritize PMC articles (with full text) over PubMed articles
     */
    private List<PubMedArticleDTO> combineArticles(
            List<PubMedArticleDTO> pubmedArticles,
            List<PubMedArticleDTO> pmcArticles
    ) {
        // Create a map of PMC articles by PubMed ID
        Map<String, PubMedArticleDTO> pmcMap = pmcArticles.stream()
                .collect(Collectors.toMap(
                        PubMedArticleDTO::getPubmedId,
                        article -> article,
                        (existing, replacement) -> existing // Keep first if duplicate
                ));

        // Merge articles, prioritizing PMC versions (with full text)
        Map<String, PubMedArticleDTO> combinedMap = new HashMap<>(pmcMap);

        for (PubMedArticleDTO pubmedArticle : pubmedArticles) {
            if (!combinedMap.containsKey(pubmedArticle.getPubmedId())) {
                combinedMap.put(pubmedArticle.getPubmedId(), pubmedArticle);
            }
        }

        // Convert to list and sort by relevance score
        List<PubMedArticleDTO> combined = new ArrayList<>(combinedMap.values());
        combined.sort((a, b) -> Double.compare(
                b.getRelevanceScore() != null ? b.getRelevanceScore() : 0.0,
                a.getRelevanceScore() != null ? a.getRelevanceScore() : 0.0
        ));

        // Limit to maxResults
        if (combined.size() > maxResults) {
            combined = combined.subList(0, maxResults);
        }

        return combined;
    }

    /**
     * Fetch full text from PubMed Central if available
     *
     * @param pubmedId PubMed ID to fetch full text for
     * @return Full text content if available in PMC, null otherwise
     */
    private String fetchFullTextFromPMC(String pubmedId) {
        try {
            log.debug("Attempting to fetch full text from PMC for PMID: {}", pubmedId);

            // First, check if the article is available in PMC by looking for PMC ID
            String pmcId = getPMCId(pubmedId);

            if (pmcId == null) {
                log.debug("No PMC ID found for PMID: {}", pubmedId);
                return null;
            }

            log.debug("Found PMC ID {} for PMID: {}", pmcId, pubmedId);

            // Fetch full text from PMC using efetch
            String url = String.format("%s/efetch.fcgi?db=pmc&id=%s&retmode=xml",
                    pubmedBaseUrl, pmcId);

            log.debug("PMC fetch URL: {}", url);

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isEmpty()) {
                log.debug("No response from PMC for ID: {}", pmcId);
                return null;
            }

            // Parse the XML to extract body text
            String fullText = parseFullTextXml(response);

            if (fullText != null && !fullText.isEmpty()) {
                log.info("Successfully fetched full text from PMC for PMID: {} ({} characters)",
                        pubmedId, fullText.length());
                return fullText;
            }

            return null;

        } catch (Exception e) {
            log.warn("Could not fetch full text from PMC for PMID {}: {}", pubmedId, e.getMessage());
            return null;
        }
    }

    /**
     * Get PMC ID from PubMed ID using elink
     *
     * @param pubmedId PubMed ID
     * @return PMC ID if article is in PMC, null otherwise
     */
    private String getPMCId(String pubmedId) {
        try {
            // Use elink to find PMC ID from PubMed ID
            String url = String.format("%s/elink.fcgi?dbfrom=pubmed&db=pmc&id=%s&retmode=json",
                    pubmedBaseUrl, pubmedId);

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                return null;
            }

            // Parse JSON response to extract PMC ID
            JsonNode root = objectMapper.readTree(response);
            JsonNode linksets = root.path("linksets");

            if (linksets.isArray() && linksets.size() > 0) {
                JsonNode linksetdb = linksets.get(0).path("linksetdbs");

                if (linksetdb.isArray() && linksetdb.size() > 0) {
                    JsonNode links = linksetdb.get(0).path("links");

                    if (links.isArray() && links.size() > 0) {
                        return links.get(0).asText();
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.debug("Error getting PMC ID for PMID {}: {}", pubmedId, e.getMessage());
            return null;
        }
    }

    /**
     * Parse full text XML from PMC to extract readable text
     *
     * @param xmlResponse XML response from PMC efetch
     * @return Extracted full text content
     */
    private String parseFullTextXml(String xmlResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));
            doc.getDocumentElement().normalize();

            StringBuilder fullText = new StringBuilder();

            // Extract article body sections
            NodeList bodyNodes = doc.getElementsByTagName("body");

            if (bodyNodes.getLength() > 0) {
                Element bodyElement = (Element) bodyNodes.item(0);

                // Get all section elements
                NodeList sections = bodyElement.getElementsByTagName("sec");

                for (int i = 0; i < sections.getLength(); i++) {
                    Element section = (Element) sections.item(i);

                    // Extract section title
                    NodeList titleNodes = section.getElementsByTagName("title");
                    if (titleNodes.getLength() > 0) {
                        String sectionTitle = titleNodes.item(0).getTextContent().trim();
                        fullText.append("\n\n## ").append(sectionTitle).append("\n");
                    }

                    // Extract paragraphs in this section
                    NodeList paragraphs = section.getElementsByTagName("p");
                    for (int j = 0; j < paragraphs.getLength(); j++) {
                        String paragraph = paragraphs.item(j).getTextContent().trim();
                        if (!paragraph.isEmpty()) {
                            fullText.append("\n").append(paragraph);
                        }
                    }
                }
            }

            String result = fullText.toString().trim();
            return result.isEmpty() ? null : result;

        } catch (Exception e) {
            log.error("Error parsing full text XML: {}", e.getMessage(), e);
            return null;
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

    /**
     * Extract brain region codes mentioned in research article abstracts
     * Uses BrainNetworkService's alias mapping to match various ways regions are mentioned
     *
     * @param articles List of PubMed articles with abstracts
     * @param regionAliasMap Map of region codes to their aliases (from BrainNetworkService)
     * @return Set of region codes that were mentioned in the abstracts
     */
    public Set<String> extractMentionedRegions(List<PubMedArticleDTO> articles, Map<String, List<String>> regionAliasMap) {
        Set<String> mentionedRegions = new HashSet<>();

        if (articles == null || articles.isEmpty()) {
            log.warn("No articles provided for region extraction");
            return mentionedRegions;
        }

        log.info("Extracting mentioned regions from {} articles", articles.size());

        for (PubMedArticleDTO article : articles) {
            String title = article.getTitle() != null ? article.getTitle() : "";
            String abstractText = article.getAbstractText() != null ? article.getAbstractText() : "";

            // Keep original case for context validation
            String fullTextOriginal = title + " " + abstractText;
            String fullTextLower = fullTextOriginal.toLowerCase();

            // Search for each region's aliases in the text
            for (Map.Entry<String, List<String>> entry : regionAliasMap.entrySet()) {
                String regionCode = entry.getKey();
                List<String> aliases = entry.getValue();

                // Check if any alias is mentioned in the text
                for (String alias : aliases) {
                    String normalizedAlias = alias.toLowerCase().trim();

                    // Use word boundary matching to avoid partial matches
                    // e.g., "hip" shouldn't match "ship"
                    String regex = "\\b" + java.util.regex.Pattern.quote(normalizedAlias) + "\\b";
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(fullTextLower);

                    while (matcher.find()) {
                        int matchPosition = matcher.start();

                        // Validate with context analysis to filter out false positives
                        if (contextValidator.isValidBrainRegionMention(
                                fullTextOriginal,
                                matchPosition,
                                normalizedAlias,
                                alias.length())) {

                            mentionedRegions.add(regionCode);
                            log.debug("Found region {} via alias '{}' in article: {}",
                                    regionCode, alias, article.getTitle());
                            break; // No need to check other aliases for this region
                        }
                    }

                    if (mentionedRegions.contains(regionCode)) {
                        break; // Already found this region, move to next region
                    }
                }
            }
        }

        log.info("Extracted {} mentioned regions from abstracts: {}",
                mentionedRegions.size(), mentionedRegions);

        return mentionedRegions;
    }
}
