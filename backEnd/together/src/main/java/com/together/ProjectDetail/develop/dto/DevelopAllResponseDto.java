package com.together.ProjectDetail.develop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DevelopAllResponseDto {
    private DevelopItemDto environment;
    private DevelopItemDto versioning;
    private DevelopItemDto commitRule;
    private DevelopItemDto folder;
    private DevelopItemDto codingStandard;
    private DevelopItemDto unitTest;
    private DevelopItemDto integrationTest;
}