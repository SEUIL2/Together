package com.together.project.ProjectDetail.develop;

import com.together.project.ProjectDetail.develop.dto.DevelopAllResponseDto;
import com.together.project.ProjectDetail.develop.dto.DevelopDetailResponseDto;
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
@RequestMapping("/develop")
public class DevelopDetailController {

    private final DevelopDetailService service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DevelopDetailResponseDto> uploadDevelopItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("type") String type,
            @RequestPart(value = "text", required = false) String text,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        Long userId = userDetails.getUser().getUserId();
        return ResponseEntity.ok(service.saveDevelopItem(userId, projectId, type, text, files));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DevelopDetailResponseDto> updateDevelopItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("type") String type,
            @RequestParam(value = "text", required = false) String text,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        Long userId = userDetails.getUser().getUserId();
        return ResponseEntity.ok(service.updateDevelopItem(userId, projectId, type, text, files));
    }

    @DeleteMapping("/delete-file")
    public ResponseEntity<Map<String, String>> deleteDevelopFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("type") String type,
            @RequestParam("fileUrl") String fileUrl
    ) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        service.deleteDevelopFile(projectId, type, fileUrl);
        return ResponseEntity.ok(Map.of(
                "message", "삭제 성공",
                "deletedUrl", fileUrl
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<DevelopAllResponseDto> getAllDevelopDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        return ResponseEntity.ok(service.getAllDetails(projectId));
    }
}
