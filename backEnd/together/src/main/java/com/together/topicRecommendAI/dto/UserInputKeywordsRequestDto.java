package com.together.topicRecommendAI.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserInputKeywordsRequestDto {
    private List<String> keywords;
}