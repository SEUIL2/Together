package com.together.project.ProjectDetail.planning;

import com.together.project.ProjectDetail.planning.dto.PlanningAllResponseDto;
import com.together.project.ProjectDetail.planning.dto.PlanningDetailResponseDto;
import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/planning")
public class PlanningDetailController {

    private final PlanningDetailService service;

    // ✅ 기획 항목 저장 API
    // 지원: text만 / files만 / 둘 다 전송 가능
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlanningDetailResponseDto> uploadPlanningItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("type") String type,  // 어떤 항목에 저장할지 지정 (ex: motivation, goal...)
            @RequestPart(value = "text", required = false) String text,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {

        // 🔑 로그인한 유저의 프로젝트 ID 및 유저 ID 가져오기
        Long projectId = userDetails.getUser().getProject().getProjectId();
        Long userId = userDetails.getUser().getUserId();

        // 💾 서비스에 저장 위임
        PlanningDetailResponseDto response = service.savePlanningItem(userId, projectId, type, text, files);

        // 📤 클라이언트에 응답 반환
        return ResponseEntity.ok(response);
    }

    // ✅ 기획 항목 수정 API (text + 새 파일 추가)
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlanningDetailResponseDto> updatePlanning(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("type") String type,
            @RequestParam(value = "text", required = false) String text,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        Long userId = userDetails.getUser().getUserId();
        return ResponseEntity.ok(service.updatePlanningItem(userId, projectId, type, text, files));
    }

    // ✅ 특정 기획 항목에 첨부된 파일 삭제 API
    @DeleteMapping("/delete-file")
    public ResponseEntity<Map<String, String>> deleteFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("type") String type,
            @RequestParam("fileUrl") String fileUrl
    ) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        service.deletePlanningFile(projectId, type, fileUrl);

        return ResponseEntity.ok(Map.of(
                "message", "파일 삭제 완료",
                "deletedUrl", fileUrl
        ));
    }

    /**
     * 전체 기획 항목 조회 API
     * - 프로젝트 동기, 목표, 설명 등 항목별 내용과 첨부 파일을 한 번에 반환
     * - GET /planning/all
     */
    @GetMapping("/all")
    public ResponseEntity<PlanningAllResponseDto> getAllPlanningDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        return ResponseEntity.ok(service.getAllDetails(projectId));
    }

}