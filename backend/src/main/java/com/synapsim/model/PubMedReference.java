package com.synapsim.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores PubMed article references linked to simulations
 */
@Entity
@Table(name = "pubmed_references")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PubMedReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "pubmed_id", nullable = false)
    private String pubmedId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 1000)
    private String authors;

    @Column(name = "publication_date")
    private String publicationDate;

    @Column(columnDefinition = "TEXT")
    private String abstractText;

    @Column(columnDefinition = "TEXT")
    private String fullText;

    @Column(name = "article_url")
    private String articleUrl;

    @Column(name = "relevance_score")
    private Double relevanceScore; // How relevant to the simulation (0.0 - 1.0)

    @Column(length = 500)
    private String keywords;
}
