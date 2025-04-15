package com.together.project.ProjectDetail.design.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignAllResponseDto {

    private DesignItemDto usecase;
    private DesignItemDto classDiagram;
    private DesignItemDto sequenceDiagram;
    private DesignItemDto uiDesign;
    private DesignItemDto erDiagram;
    private DesignItemDto tableSchema;
    private DesignItemDto systemArchitecture;
    private DesignItemDto schedulePlan;
}