package com.together.project.ProjectDetail;

import com.together.project.ProjectDetail.dto.ProjectDetailResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailSimpleResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailTextResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;

@RestController
@RequestMapping("/project-details")
@RequiredArgsConstructor
public class ProjectDetailController {

    private final ProjectDetailService projectDetailService;

    // ✅ 프로젝트 상세 정보 조회
    // ✅ 간소화된 프로젝트 상세 정보 조회
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponseDto> getProjectDetail(@PathVariable Long projectId) {
        ProjectDetailResponseDto detail = projectDetailService.getProjectDetail(projectId);
        return ResponseEntity.ok(detail);
    }

    // ✅ 프로젝트 이미지 업로드
    @PostMapping("/upload-image")
    public ResponseEntity<ProjectDetailSimpleResponseDto> uploadProjectImage(
            @RequestParam("projectId") Long projectId,
            @RequestParam("imageType") String imageType,
            @RequestParam("file") MultipartFile file) throws IOException {

        ProjectDetailSimpleResponseDto responseDto = projectDetailService.uploadProjectImage(projectId, file, imageType);
        return ResponseEntity.ok(responseDto);
    }

    // ✅ 프로젝트 텍스트 데이터 (동기, 목표, 스토리보드, UI디자인)
    @PostMapping("/create-text")
    public ResponseEntity<ProjectDetailTextResponseDto> createTextDetails(
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "projectMotivation", required = false) String projectMotivation,
            @RequestParam(value = "projectGoal", required = false) String projectGoal,
            @RequestParam(value = "storyboard", required = false) String storyboard,
            @RequestParam(value = "uiDesign", required = false) String uiDesign,
            @RequestParam(value = "systemArchitecture", required = false) String systemArchitecture,
            @RequestParam(value = "devEnvironmentText", required = false) String devEnvironmentText,
            @RequestParam(value = "versionControlStrategy", required = false) String versionControlStrategy,
            @RequestParam(value = "commitMessageRule", required = false) String commitMessageRule,
            @RequestParam(value = "folderNamingRule", required = false) String folderNamingRule,
            @RequestParam(value = "projectDescription", required = false) String projectDescription
    ) {
        ProjectDetailTextResponseDto dto = projectDetailService.updateTextDetails(
                projectId,
                projectMotivation,
                projectGoal,
                storyboard,
                uiDesign,
                systemArchitecture,
                devEnvironmentText,
                versionControlStrategy,
                commitMessageRule,
                folderNamingRule,
                projectDescription
        );
        return ResponseEntity.ok(dto);
    }
    //프로젝트 정보 불러오기
    @GetMapping("/full/{projectId}")
    public ResponseEntity<ProjectDetailResponseDto> getFullProjectDetail(@PathVariable Long projectId) {
        ProjectDetailResponseDto detailDto = projectDetailService.getProjectDetail(projectId);
        return ResponseEntity.ok(detailDto);
    }


    // ✅ 전체 정보 수정 (텍스트 + 이미지)
    @PutMapping(value = "/update-full", consumes = "multipart/form-data")
    public ResponseEntity<ProjectDetailResponseDto> updateFullProjectDetail(
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "projectMotivation", required = false) String projectMotivation,
            @RequestParam(value = "projectGoal", required = false) String projectGoal,
            @RequestParam(value = "storyboard", required = false) String storyboard,
            @RequestParam(value = "uiDesign", required = false) String uiDesign,
            @RequestParam(value = "systemArchitecture", required = false) String systemArchitecture,
            @RequestParam(value = "devEnvironmentText", required = false) String devEnvironmentText,
            @RequestParam(value = "versionControlStrategy", required = false) String versionControlStrategy,
            @RequestParam(value = "commitMessageRule", required = false) String commitMessageRule,
            @RequestParam(value = "folderNamingRule", required = false) String folderNamingRule,
            @RequestParam(value = "projectDescription", required = false) String projectDescription,
            @RequestPart(value = "requirementsImage", required = false) MultipartFile requirementsImage,
            @RequestPart(value = "infoStructure", required = false) MultipartFile infoStructure,
            @RequestPart(value = "useCaseDiagramImage", required = false) MultipartFile useCaseDiagramImage,
            @RequestPart(value = "classDiagramImage", required = false) MultipartFile classDiagramImage,
            @RequestPart(value = "sequenceDiagramImage", required = false) MultipartFile sequenceDiagramImage,
            @RequestPart(value = "erDiagramImage", required = false) MultipartFile erDiagramImage,
            @RequestPart(value = "tableSpecImage", required = false) MultipartFile tableSpecImage,
            @RequestPart(value = "schedulePlanImage", required = false) MultipartFile schedulePlanImage,
            @RequestPart(value = "devEnvironmentImage", required = false) MultipartFile devEnvironmentImage,
            @RequestPart(value = "codingStandardImage", required = false) MultipartFile codingStandardImage,
            @RequestPart(value = "unitTestImage", required = false) MultipartFile unitTestImage,
            @RequestPart(value = "integrationTestImage", required = false) MultipartFile integrationTestImage
    ) throws IOException {

        ProjectDetailResponseDto updatedDetail = projectDetailService.updateFullDetails(
                projectId,
                projectMotivation, projectGoal, storyboard, uiDesign, systemArchitecture,
                devEnvironmentText, versionControlStrategy, commitMessageRule, folderNamingRule, projectDescription,
                requirementsImage, infoStructure, useCaseDiagramImage, classDiagramImage,
                sequenceDiagramImage, erDiagramImage, tableSpecImage, schedulePlanImage,
                devEnvironmentImage, codingStandardImage, unitTestImage, integrationTestImage
        );

        return ResponseEntity.ok(updatedDetail);
    }
}
