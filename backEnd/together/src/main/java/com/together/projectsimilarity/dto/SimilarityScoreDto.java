package com.together.projectsimilarity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityScoreDto {
    @JsonProperty("score")
    private int score;

    @JsonProperty("reason")
    private String reason;
}