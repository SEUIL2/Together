package com.together.projectsimilarity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleComparisonResultDto {
    private Long targetProjectId;

    @JsonProperty("topicSimilarity")
    private SimilarityScoreDto topicSimilarity;

    @JsonProperty("functionSimilarity")
    private SimilarityScoreDto functionSimilarity;

    @JsonProperty("techStackSimilarity")
    private SimilarityScoreDto techStackSimilarity;

    @JsonProperty("keywordSimilarity")
    private SimilarityScoreDto keywordSimilarity;
}