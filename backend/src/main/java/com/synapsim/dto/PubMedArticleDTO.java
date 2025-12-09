package com.synapsim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PubMed article information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PubMedArticleDTO {

    private String pubmedId;
    private String title;
    private String authors;
    private String publicationDate;
    private String abstractText;
    private String articleUrl;
    private Double relevanceScore;
    private String keywords;
}
