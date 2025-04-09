package com.together.documentManger;

import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class GoogleDriveController {

    private final GoogleDriveService googleDriveService;

    // **파일 업로드 (Google Drive + DB 저장)**
    @PostMapping("/upload")
    public ResponseEntity<FileEntity> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        //자동추출
        Long userId = userDetails.getUser().getUserId();
        Long projectId = userDetails.getUser().getProject().getProjectId();

        FileEntity savedFile = googleDriveService.uploadFile(file, userId, projectId);
        return ResponseEntity.ok(savedFile);
    }

    // **특정 프로젝트의 파일 조회**
    @GetMapping("/project")
    public ResponseEntity<List<FileEntity>> getFilesByProject(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        List<FileEntity> files = googleDriveService.getFilesByProject(projectId);
        return ResponseEntity.ok(files);
    }

    // **파일 삭제**
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId) throws IOException {
        googleDriveService.deleteFile(fileId);
        return ResponseEntity.ok("파일 삭제 완료");
    }

    // 파일 다운로드 API
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) throws IOException {
        ByteArrayResource resource = googleDriveService.downloadFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                .body(resource);
    }

}
