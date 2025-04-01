package com.together.project.ProjectDetail;

import com.together.project.ProjectDetail.dto.ProjectDetailResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailSimpleResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailTextResponseDto;
import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;



import java.io.IOException;

@RestController
@RequestMapping("/project-details")
@RequiredArgsConstructor
public class ProjectDetailController {

    private final ProjectDetailService projectDetailService;

    // ✅ 자동으로 로그인한 유저의 projectId 기반으로 조회
    @GetMapping("/my-project")
    public ResponseEntity<ProjectDetailResponseDto> getMyProjectDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        ProjectDetailResponseDto detailDto = projectDetailService.getProjectDetail(projectId);
        return ResponseEntity.ok(detailDto);
    }

    // ✅ 이미지 업로드
    @PostMapping("/upload-image")
    public ResponseEntity<ProjectDetailSimpleResponseDto> uploadImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("imageType") String imageType,
            @RequestParam("file") MultipartFile file) throws IOException {

        Long projectId = userDetails.getUser().getProject().getProjectId();
        return ResponseEntity.ok(projectDetailService.uploadProjectImage(projectId, file, imageType));
    }

    // ✅ 텍스트 저장
    @PostMapping("/create-text")
    public ResponseEntity<ProjectDetailTextResponseDto> createTextDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
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
        Long projectId = userDetails.getUser().getProject().getProjectId();
        return ResponseEntity.ok(projectDetailService.updateTextDetails(
                projectId, projectMotivation, projectGoal, storyboard, uiDesign, systemArchitecture,
                devEnvironmentText, versionControlStrategy, commitMessageRule, folderNamingRule, projectDescription
        ));
    }



    // ✅ 전체 수정 (텍스트 + 이미지)
    @PutMapping(value = "/update-my-project", consumes = "multipart/form-data")
    public ResponseEntity<ProjectDetailResponseDto> updateFullProject(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
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

        Long projectId = userDetails.getUser().getProject().getProjectId();

        return ResponseEntity.ok(projectDetailService.updateFullDetails(
                projectId,
                projectMotivation, projectGoal, storyboard, uiDesign, systemArchitecture,
                devEnvironmentText, versionControlStrategy, commitMessageRule, folderNamingRule, projectDescription,
                requirementsImage, infoStructure, useCaseDiagramImage, classDiagramImage,
                sequenceDiagramImage, erDiagramImage, tableSpecImage, schedulePlanImage,
                devEnvironmentImage, codingStandardImage, unitTestImage, integrationTestImage
        ));
    }
}
