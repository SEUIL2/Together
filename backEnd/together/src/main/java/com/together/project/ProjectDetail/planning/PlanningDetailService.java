package com.together.project.ProjectDetail.planning;

import com.together.documentManger.GoogleDriveService;
import com.together.project.ProjectDetail.planning.dto.PlanningAllResponseDto;
import com.together.project.ProjectDetail.planning.dto.PlanningItemDto;
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
            String json,
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
                    FileMeta meta = new FileMeta(url, LocalDateTime.now(),file.getContentType());
                    metaList.add(meta);
                    uploadedDtos.add(new FileMetaDto(meta.getUrl(), meta.getUploadedAt(), meta.getFileType()));
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
                if (json != null) detail.setInfoStructureJson(json); // ⭐️ JSON 저장 추가
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

        // ⭐️ JSON 포함해서 응답 (infostructure일 때만 json 값 할당)
        return new PlanningDetailResponseDto(type, text, json, uploadedDtos);
    }
    //수정
    @Transactional
    public PlanningDetailResponseDto updatePlanningItem(
            Long userId,
            Long projectId,
            String type,
            String text,
            String json, // ⭐️ 추가
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
                    FileMeta meta = new FileMeta(url, LocalDateTime.now(),file.getContentType());
                    metaList.add(meta);
                    uploadedDtos.add(new FileMetaDto(meta.getUrl(), meta.getUploadedAt(), meta.getFileType()));
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
                if (json != null) detail.setInfoStructureJson(json); // ⭐️ JSON 수정 추가
                detail.getInfoStructureFiles().addAll(metaList);
            }
            case "storyboard" -> {
                if (text != null) detail.setStoryboardText(text);
                detail.getStoryboardFiles().addAll(metaList);
            }
            default -> throw new IllegalArgumentException("알 수 없는 항목: " + type);
        }

        repository.save(detail);
        return new PlanningDetailResponseDto(type, text, json,uploadedDtos);
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

    /**
     * 전체 기획 항목들을 한 번에 불러오는 서비스 로직
     * @param projectId 해당 프로젝트 ID
     * @return PlanningAllResponseDto 형태로 전체 반환
     */
    @Transactional
    public PlanningAllResponseDto getAllDetails(Long projectId) {
        PlanningDetailEntity detail = getOrCreateDetail(projectId);

        return PlanningAllResponseDto.builder()
                .motivation(toItem(detail.getMotivationText(), detail.getMotivationFiles()))
                .goal(toItem(detail.getGoalText(), detail.getGoalFiles()))
                .requirement(toItem(detail.getRequirementText(), detail.getRequirementFiles()))
                .infostructure(toItem(
                        detail.getInfoStructureText(),
                        detail.getInfoStructureJson(), // ⭐️ JSON 추가
                        detail.getInfoStructureFiles()))
                .storyboard(toItem(detail.getStoryboardText(), detail.getStoryboardFiles()))
                .description(toItem(detail.getDescriptionText(), detail.getDescriptionFiles()))
                .build();
    }

    /**
     * 개별 항목의 텍스트 + 파일 리스트를 DTO로 변환
     */
    // json까지 모두 받을 수 있는 오버로딩 메서드
    private PlanningItemDto toItem(String text, String json, List<FileMeta> files) {
        List<FileMetaDto> fileDtos = files.stream()
                .map(f -> new FileMetaDto(f.getUrl(), f.getUploadedAt(),f.getFileType()))
                .toList();
        return PlanningItemDto.builder()
                .text(text)
                .json(json) // json 필드 포함
                .files(fileDtos)
                .build();
    }

    // text + files만 있는 경우 (json=null로 전달)
    private PlanningItemDto toItem(String text, List<FileMeta> files) {
        return toItem(text, null, files);
    }


    private PlanningDetailEntity getOrCreateDetail(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다."));

        return repository.findByProject(project)
                .orElseGet(() -> {
                    PlanningDetailEntity newDetail = new PlanningDetailEntity();
                    newDetail.setProject(project);
                    return repository.save(newDetail);
                });
    }
}
