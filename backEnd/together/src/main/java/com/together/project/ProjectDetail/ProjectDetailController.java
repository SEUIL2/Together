package com.together.project.ProjectDetail;

import com.together.project.ProjectDetail.dto.ProjectDetailResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailSimpleResponseDto;
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

    // ✅ 프로젝트 텍스트 데이터 수정 (동기, 목표, 스토리보드, UI디자인)
    @PutMapping("/update-text")
    public ResponseEntity<ProjectDetailEntity> updateTextDetails(
            @RequestParam("projectId") Long projectId,
            @RequestParam("projectMotivation") String projectMotivation,
            @RequestParam("projectGoal") String projectGoal,
            @RequestParam("storyboard") String storyboard,
            @RequestParam("uiDesign") String uiDesign) {

        ProjectDetailEntity updatedDetail = projectDetailService.updateTextDetails(
                projectId, projectMotivation, projectGoal, storyboard, uiDesign
        );
        return ResponseEntity.ok(updatedDetail);
    }
}
