package com.together.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class KeywordsRequestDto {
    private List<String> keywords; //사용자가 선택한 키워드
}
