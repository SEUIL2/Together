package com.together.documentManger;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private final Drive googleDrive;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    // 파일 업로드 후 공개 설정
    public void makeFilePublic(String fileId) throws IOException {

        Permission permission = new Permission();
        permission.setType("anyone");  // 모든 사용자 허용
        permission.setRole("reader");  // 읽기 전용 권한 부여

        googleDrive.permissions()
                .create(fileId, permission)
                .execute();

    }

    // ✅ 파일 업로드 (Google Drive + DB 저장)
    public FileEntity uploadFile(MultipartFile file, Long userId, Long projectId) throws IOException {
        // 🔍 사용자와 프로젝트 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        // 📁 Google Drive에 업로드할 메타데이터 설정
        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setParents(List.of("root")); // 루트 폴더에 저장

        // 🧠 실제 파일 생성
        java.io.File tempFile = java.io.File.createTempFile("upload_", null);
        try (OutputStream os = new FileOutputStream(tempFile)) {
            os.write(file.getBytes());
        }

        // 📌 MIME 타입 자동 감지 (ex. image/jpeg, application/pdf ...)
        String mimeType = file.getContentType();
        AbstractInputStreamContent fileContent = new FileContent(mimeType, tempFile);

        // ☁️ Google Drive로 파일 업로드
        File uploadedFile = googleDrive.files().create(fileMetadata, fileContent)
                .setFields("id, webViewLink, mimeType, size")
                .execute();

        // 🌍 업로드한 파일을 공개로 설정
        makeFilePublic(uploadedFile.getId());




        // Google Drive "다운로드 전용" URL 직접 생성해서 fileUrl에 저장!
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + uploadedFile.getId();

        // 🗃️ DB에 파일 메타데이터 저장 (fileUrl에 반드시 downloadUrl 저장)
        FileEntity fileEntity = FileEntity.builder()
                .googleDriveFileId(uploadedFile.getId())
                .fileName(file.getOriginalFilename())
                .fileType(uploadedFile.getMimeType())   // 예: image/jpeg
                .fileSize(String.valueOf(uploadedFile.getSize()))
                .fileUrl(downloadUrl)  // ⭐️⭐️⭐️ 반드시 이 부분을 downloadUrl로!
                .user(user)
                .project(project)
                .build();

        return fileRepository.save(fileEntity);
    }

    // **특정 프로젝트의 모든 파일 조회**
    public List<FileEntity> getFilesByProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        return fileRepository.findByProject(project);
    }

    // **파일 삭제 (Google Drive + DB에서 삭제)**
    public void deleteFile(String googleDriveFileId) throws IOException {
        try {
            googleDrive.files().delete(googleDriveFileId).execute();
        } catch (Exception e) {
            throw new RuntimeException("Google Drive에서 파일 삭제 실패", e);
        }

        // 🔁 FileEntity 사용하는 경우만 삭제 (Optional)
        fileRepository.findByGoogleDriveFileId(googleDriveFileId)
                .ifPresentOrElse(
                        fileRepository::delete,
                        () -> System.out.println("[INFO] FileEntity DB 기록 없음. 무시하고 진행.")
                );
    }

    // 파일 다운로드
    public ByteArrayResource downloadFile(String fileId) throws IOException {
        // Google Drive에서 파일 가져오기
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        googleDrive.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream);

        // 바이트 배열을 리소스로 변환
        return new ByteArrayResource(outputStream.toByteArray());
    }

    // Google Drive에 이미지만 업로드하고 URL 반환
    public String uploadImageToGoogleDrive(MultipartFile file) throws IOException {
        // **Google Drive에 파일 업로드**
        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setParents(List.of("root")); // 루트 폴더에 저장

        java.io.File tempFile = java.io.File.createTempFile("upload_", null);
        try (OutputStream os = new FileOutputStream(tempFile)) {
            os.write(file.getBytes());
        }

        AbstractInputStreamContent fileContent = new FileContent("application/octet-stream", tempFile);
        File uploadedFile = googleDrive.files().create(fileMetadata, fileContent)
                .setFields("id, webViewLink")
                .execute();

        // 🔹 파일을 공개로 설정
        makeFilePublic(uploadedFile.getId());

        // PDFBox, 브라우저 등 어디서든 바로 이미지를 다운받을 수 있는 URL 반환!
        return "https://drive.google.com/uc?export=download&id=" + uploadedFile.getId();
    }

    public String extractDriveFileId(String fileUrl) {
        // 📌 Google Drive 파일 ID를 추출하는 다양한 URL 패턴 지원
        String regex = "/(?:file|document|presentation|spreadsheets)/d/([a-zA-Z0-9_-]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // 📎 대체 포맷: https://drive.google.com/open?id=FILE_ID
        regex = "[?&]id=([a-zA-Z0-9_-]+)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(fileUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // ❌ 매칭 실패
        throw new IllegalArgumentException("유효한 Google Drive 링크가 아닙니다: " + fileUrl);


}}

