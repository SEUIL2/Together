package com.together.project.ProjectDetail.design;

import com.together.project.ProjectDetail.design.dto.DesignAllResponseDto;
import com.together.project.ProjectDetail.design.dto.DesignDetailResponseDto;
import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;
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
@RequestMapping("/design")
public class DesignDetailController {

    private final DesignDetailService service;

    // ✅ 설계 항목 저장 API (텍스트 + 파일 또는 JSON 포함)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DesignDetailResponseDto> uploadDesignItem(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("type") String type,  // 어떤 항목인지 지정 (ex: usecase, class-diagram, ...)
            @RequestPart(value = "text", required = false) String text,
            @RequestPart(value = "json", required = false) String json,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {

        Long userId = userDetails.getUser().getUserId();

        DesignDetailResponseDto response = service.saveDesignItem(userId, projectId, type, text, json, files);
        return ResponseEntity.ok(response);
    }

    // ✅ 설계 항목 수정 API
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DesignDetailResponseDto> updateDesignItem(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("type") String type,
            @RequestPart(value = "text", required = false) String text,
            @RequestPart(value = "json", required = false) String json,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        Long userId = userDetails.getUser().getUserId();
        DesignDetailResponseDto response = service.updateDesignItem(userId, projectId, type, text, json, files);
        return ResponseEntity.ok(response);
    }

    // ✅ 설계 파일 삭제 API
    @DeleteMapping("/delete-file")
    public ResponseEntity<Map<String, String>> deleteDesignFile(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("type") String type,
            @RequestParam("fileUrl") String fileUrl
    ) {
        service.deleteDesignFile(projectId, type, fileUrl);

        return ResponseEntity.ok(Map.of(
                "message", "파일 삭제 완료",
                "deletedUrl", fileUrl
        ));
    }

    // ✅ 전체 설계 항목 조회 API
    @GetMapping("/all")
    public ResponseEntity<DesignAllResponseDto> getAllDesignDetails(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(service.getAllDesignDetails(projectId));
    }
}
