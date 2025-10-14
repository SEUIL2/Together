package com.together.projectsimilarity.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectComparisonRequestDto {
    private Long baseProjectId;
    private List<Long> targetProjectIds;
}