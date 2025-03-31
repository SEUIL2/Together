package com.together.project.ProjectDetail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponseDto {
    private String projectMotivation;
    private String projectGoal;
    private String requirementsImage;
    private String infoStructure;
    private String storyboard;
    private String useCaseDiagramImage;
    private String classDiagramImage;
    private String sequenceDiagramImage;
    private String uiDesign;
    private String erDiagramImage;
    private String tableSpecImage;
    private String schedulePlanImage;
    private String devEnvironmentImage;
    private String codingStandardImage;
    private String unitTestImage;
    private String integrationTestImage;
    private String projectDescription;
}