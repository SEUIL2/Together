package com.together.project.ProjectDetail.planning;

import com.together.documentManger.GoogleDriveService;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.project.ProjectDetail.common.FileMeta;
import com.together.project.ProjectDetail.common.FileMetaDto;
import com.together.project.ProjectDetail.planning.dto.PlanningDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningDetailService {

    private final PlanningDetailRepository repository;
    private final ProjectRepository projectRepository;
    private final GoogleDriveService driveService;


    // ✅ 기획 항목 저장 서비스 (텍스트 + 파일 업로드 가능)
    @Transactional
    public PlanningDetailResponseDto savePlanningItem(
            Long userId,
            Long projectId,
            String type,
            String text,
            List<MultipartFile> files
    ) throws IOException {

        // 🔎 프로젝트 조회
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        // 🗂️ 해당 프로젝트의 상세 정보 조회 또는 새로 생성
        PlanningDetailEntity detail = repository.findByProject(project)
                .orElseGet(() -> {
                    PlanningDetailEntity newDetail = new PlanningDetailEntity();
                    newDetail.setProject(project);
                    return repository.save(newDetail);
                });

        detail.setUpdatedAt(LocalDateTime.now());

        // 📁 파일 업로드 처리
        List<FileMeta> metaList = new ArrayList<>();
        List<FileMetaDto> uploadedDtos = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // ✅ Google Drive 업로드
                    String url = driveService.uploadFile(file, userId, projectId).getFileUrl();
                    FileMeta meta = new FileMeta(url, LocalDateTime.now());
                    metaList.add(meta);
                    uploadedDtos.add(new FileMetaDto(meta.getUrl(), meta.getUploadedAt()));
                }
            }
        }

        // ✅ 항목 타입에 따라 저장 위치 분기
        switch (type.toLowerCase()) {
            case "motivation" -> {
                if (text != null) detail.setMotivationText(text);
                detail.getMotivationFiles().addAll(metaList);
            }
            case "goal" -> {
                if (text != null) detail.setGoalText(text);
                detail.getGoalFiles().addAll(metaList);
            }
            case "description" -> {
                if (text != null) detail.setDescriptionText(text);
                detail.getDescriptionFiles().addAll(metaList);
            }
            case "requirement" -> {
                if (text != null) detail.setRequirementText(text);
                detail.getRequirementFiles().addAll(metaList);
            }
            case "infostructure" -> {
                if (text != null) detail.setInfoStructureText(text);
                detail.getInfoStructureFiles().addAll(metaList);
            }
            case "storyboard" -> {
                if (text != null) detail.setStoryboardText(text);
                detail.getStoryboardFiles().addAll(metaList);
            }
            default -> throw new IllegalArgumentException("알 수 없는 항목: " + type);
        }

        // 💾 최종 저장
        repository.save(detail);

        // 📤 클라이언트에게 응답 반환
        return new PlanningDetailResponseDto(type, text, uploadedDtos);
    }
    //수정
    @Transactional
    public PlanningDetailResponseDto updatePlanningItem(
            Long userId,
            Long projectId,
            String type,
            String text,
            List<MultipartFile> files
    ) throws IOException {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        PlanningDetailEntity detail = repository.findByProject(project)
                .orElseThrow(() -> new IllegalArgumentException("기획 상세 없음"));

        detail.setUpdatedAt(LocalDateTime.now());

        List<FileMeta> metaList = new ArrayList<>();
        List<FileMetaDto> uploadedDtos = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = driveService.uploadFile(file, userId, projectId).getFileUrl();
                    FileMeta meta = new FileMeta(url, LocalDateTime.now());
                    metaList.add(meta);
                    uploadedDtos.add(new FileMetaDto(meta.getUrl(), meta.getUploadedAt()));
                }
            }
        }

        // 타입별 텍스트 및 파일 갱신
        switch (type.toLowerCase()) {
            case "motivation" -> {
                if (text != null) detail.setMotivationText(text);
                detail.getMotivationFiles().addAll(metaList);
            }
            case "goal" -> {
                if (text != null) detail.setGoalText(text);
                detail.getGoalFiles().addAll(metaList);
            }
            case "description" -> {
                if (text != null) detail.setDescriptionText(text);
                detail.getDescriptionFiles().addAll(metaList);
            }
            case "requirement" -> {
                if (text != null) detail.setRequirementText(text);
                detail.getRequirementFiles().addAll(metaList);
            }
            case "infostructure" -> {
                if (text != null) detail.setInfoStructureText(text);
                detail.getInfoStructureFiles().addAll(metaList);
            }
            case "storyboard" -> {
                if (text != null) detail.setStoryboardText(text);
                detail.getStoryboardFiles().addAll(metaList);
            }
            default -> throw new IllegalArgumentException("알 수 없는 항목: " + type);
        }

        repository.save(detail);
        return new PlanningDetailResponseDto(type, text, uploadedDtos);
    }

    //삭제
    @Transactional
    public void deletePlanningFile(Long projectId, String type, String fileUrl) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        PlanningDetailEntity detail = repository.findByProject(project)
                .orElseThrow(() -> new IllegalArgumentException("기획 상세 없음"));

        List<FileMeta> targetList;

        switch (type.toLowerCase()) {
            case "motivation" -> targetList = detail.getMotivationFiles();
            case "goal" -> targetList = detail.getGoalFiles();
            case "description" -> targetList = detail.getDescriptionFiles();
            case "requirement" -> targetList = detail.getRequirementFiles();
            case "infostructure" -> targetList = detail.getInfoStructureFiles();
            case "storyboard" -> targetList = detail.getStoryboardFiles();
            default -> throw new IllegalArgumentException("유효하지 않은 항목 타입: " + type);
        }

        targetList.removeIf(meta -> meta.getUrl().equals(fileUrl));

        try {
            String fileId = driveService.extractDriveFileId(fileUrl);
            driveService.deleteFile(fileId);
        } catch (IOException e) {
            throw new RuntimeException("Google Drive 파일 삭제 중 오류 발생", e);
        }

        repository.save(detail);
    }

    // 🔍 Google Drive URL에서 ID 추출
    private String extractDriveFileId(String url) {
        // 예: https://docs.google.com/document/d/FILE_ID/edit 또는 https://drive.google.com/file/d/FILE_ID/view
        String regex = "/d/([a-zA-Z0-9_-]+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);  // 추출된 file ID
        } else {
            throw new IllegalArgumentException("올바른 Google Drive 파일 URL 형식이 아닙니다: " + url);
        }
    }
}
