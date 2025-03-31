package com.together.project.ProjectDetail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailTextResponseDto {
    private String projectMotivation;
    private String projectGoal;
    private String storyboard;
    private String uiDesign;
    private String systemArchitecture;
    private String projectDescription;

    private String devEnvironmentText;
    private String versionControlStrategy;
    private String commitMessageRule;
    private String folderNamingRule;
}
