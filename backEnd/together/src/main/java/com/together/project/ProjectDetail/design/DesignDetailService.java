package com.together.project.ProjectDetail.design;

import com.together.documentManger.GoogleDriveService;
import com.together.project.ProjectDetail.common.FileMeta;
import com.together.project.ProjectDetail.common.FileMetaDto;
import com.together.project.ProjectDetail.design.dto.DesignAllResponseDto;
import com.together.project.ProjectDetail.design.dto.DesignDetailResponseDto;
import com.together.project.ProjectDetail.design.dto.DesignItemDto;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
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
public class DesignDetailService {

    private final DesignDetailRepository repository;
    private final ProjectRepository projectRepository;
    private final GoogleDriveService driveService;

    // ✅ 설계 항목 저장
    @Transactional
    public DesignDetailResponseDto saveDesignItem(Long userId, Long projectId, String type, String text, String json, List<MultipartFile> files) throws IOException {
        DesignDetailEntity detail = getOrCreateDesignDetail(projectId);
        detail.setUpdatedAt(LocalDateTime.now());

        List<FileMeta> metaList = uploadFiles(files, userId, projectId);
        List<FileMetaDto> uploadedDtos = toDtoList(metaList);

        switch (type.toLowerCase()) {
            case "usecase" -> {
                if (text != null) detail.setUsecaseText(text);
                detail.getUsecaseFiles().addAll(metaList);
            }
            case "class-diagram" -> {
                if (json != null) detail.setClassDiagramText(json);
                detail.getClassDiagramFiles().addAll(metaList);
            }
            case "sequence" -> {
                if (text != null) detail.setSequenceText(text);
                detail.getSequenceFiles().addAll(metaList);
            }
            case "ui" -> {
                if (text != null) detail.setUiDesignText(text);
                detail.getUiDesignFiles().addAll(metaList);
            }
            case "erd" -> {
                if (text != null) detail.setErdText(text);
                detail.getErdFiles().addAll(metaList);
            }
            case "table" -> {
                if (text != null) detail.setTableSpecText(text);
                detail.getTableSpecFiles().addAll(metaList);
            }
            case "architecture" -> {
                if (text != null) detail.setArchitectureText(text);
                detail.getArchitectureFiles().addAll(metaList);
            }
            case "schedule" -> {
                if (text != null) detail.setScheduleText(text);
                detail.getScheduleFiles().addAll(metaList);
            }
            default -> throw new IllegalArgumentException("유효하지 않은 type: " + type);
        }

        repository.save(detail);
        return new DesignDetailResponseDto(type, text, json, uploadedDtos);
    }

    // ✅ 설계 항목 수정
    @Transactional
    public DesignDetailResponseDto updateDesignItem(Long userId, Long projectId, String type, String text, String json, List<MultipartFile> files) throws IOException {
        DesignDetailEntity detail = getOrCreateDesignDetail(projectId);
        detail.setUpdatedAt(LocalDateTime.now());

        List<FileMeta> metaList = uploadFiles(files, userId, projectId);
        List<FileMetaDto> uploadedDtos = toDtoList(metaList);

        switch (type.toLowerCase()) {
            case "usecase" -> {
                if (text != null) detail.setUsecaseText(text);
                detail.getUsecaseFiles().addAll(metaList);
            }
            case "class-diagram" -> {
                if (json != null) detail.setClassDiagramText(json);
                detail.getClassDiagramFiles().addAll(metaList);
            }
            case "sequence" -> {
                if (text != null) detail.setSequenceText(text);
                detail.getSequenceFiles().addAll(metaList);
            }
            case "ui" -> {
                if (text != null) detail.setUiDesignText(text);
                detail.getUiDesignFiles().addAll(metaList);
            }
            case "erd" -> {
                if (text != null) detail.setErdText(text);
                detail.getErdFiles().addAll(metaList);
            }
            case "table" -> {
                if (text != null) detail.setTableSpecText(text);
                detail.getTableSpecFiles().addAll(metaList);
            }
            case "architecture" -> {
                if (text != null) detail.setArchitectureText(text);
                detail.getArchitectureFiles().addAll(metaList);
            }
            case "schedule" -> {
                if (text != null) detail.setScheduleText(text);
                detail.getScheduleFiles().addAll(metaList);
            }
            default -> throw new IllegalArgumentException("유효하지 않은 type: " + type);
        }

        repository.save(detail);
        return new DesignDetailResponseDto(type, text != null ? text : json, uploadedDtos);
    }

    // ✅ 설계 항목 파일 삭제
    @Transactional
    public void deleteDesignFile(Long projectId, String type, String fileUrl) {
        DesignDetailEntity detail = getOrCreateDesignDetail(projectId);

        List<FileMeta> targetList;

        switch (type.toLowerCase()) {
            case "usecase" -> targetList = detail.getUsecaseFiles();
            case "class-diagram" -> targetList = detail.getClassDiagramFiles();
            case "sequence" -> targetList = detail.getSequenceFiles();
            case "ui" -> targetList = detail.getUiDesignFiles();
            case "erd" -> targetList = detail.getErdFiles();
            case "table" -> targetList = detail.getTableSpecFiles();
            case "architecture" -> targetList = detail.getArchitectureFiles();
            case "schedule" -> targetList = detail.getScheduleFiles();
            default -> throw new IllegalArgumentException("유효하지 않은 type: " + type);
        }

        targetList.removeIf(meta -> meta.getUrl().equals(fileUrl));
        repository.save(detail);

        try {
            String fileId = driveService.extractDriveFileId(fileUrl);
            driveService.deleteFile(fileId);
        } catch (IOException e) {
            throw new RuntimeException("Google Drive 파일 삭제 실패", e);
        }
    }

    // ✅ 설계 전체 조회
    @Transactional(readOnly = true)
    public DesignAllResponseDto getAllDesignDetails(Long projectId) {
        DesignDetailEntity detail = getOrCreateDesignDetail(projectId);

        return DesignAllResponseDto.builder()
                .usecase(toItem(detail.getUsecaseText(), detail.getUsecaseFiles()))
                .classDiagram(toItem(detail.getClassDiagramText(), detail.getClassDiagramFiles()))
                .sequenceDiagram(toItem(detail.getSequenceText(), detail.getSequenceFiles()))
                .uiDesign(toItem(detail.getUiDesignText(), detail.getUiDesignFiles()))
                .erDiagram(toItem(detail.getErdText(), detail.getErdFiles()))
                .tableSchema(toItem(detail.getTableSpecText(), detail.getTableSpecFiles()))
                .systemArchitecture(toItem(detail.getArchitectureText(), detail.getArchitectureFiles()))
                .schedulePlan(toItem(detail.getScheduleText(), detail.getScheduleFiles()))
                .build();
    }

    // 🔧 내부 공통 메서드들
    private List<FileMeta> uploadFiles(List<MultipartFile> files, Long userId, Long projectId) throws IOException {
        List<FileMeta> metaList = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = driveService.uploadFile(file, userId, projectId).getFileUrl();
                    metaList.add(new FileMeta(url, LocalDateTime.now()));
                }
            }
        }
        return metaList;
    }

    private List<FileMetaDto> toDtoList(List<FileMeta> metas) {
        return metas.stream()
                .map(meta -> new FileMetaDto(meta.getUrl(), meta.getUploadedAt()))
                .toList();
    }

    private DesignItemDto toItem(String text, List<FileMeta> files) {
        List<FileMetaDto> fileDtos = files.stream()
                .map(f -> new FileMetaDto(f.getUrl(), f.getUploadedAt()))
                .toList();
        return new DesignItemDto(text, fileDtos);
    }

    private DesignDetailEntity getOrCreateDesignDetail(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        return repository.findByProject(project)
                .orElseGet(() -> {
                    DesignDetailEntity detail = new DesignDetailEntity();
                    detail.setProject(project);
                    return repository.save(detail);
                });
    }
}
