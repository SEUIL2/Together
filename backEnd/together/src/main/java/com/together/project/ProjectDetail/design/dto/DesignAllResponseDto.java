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
    private DesignItemDto sequence;
    private DesignItemDto ui;
    private DesignItemDto erd;
    private DesignItemDto table;
    private DesignItemDto architecture;
    private DesignItemDto schedule;
}