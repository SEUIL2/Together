package com.together.project.ProjectDetail.develop;

import com.together.documentManger.GoogleDriveService;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.project.ProjectDetail.common.FileMeta;
import com.together.project.ProjectDetail.common.FileMetaDto;
import com.together.project.ProjectDetail.develop.dto.DevelopAllResponseDto;
import com.together.project.ProjectDetail.develop.dto.DevelopDetailResponseDto;
import com.together.project.ProjectDetail.develop.dto.DevelopItemDto;
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
public class DevelopDetailService {

    private final DevelopDetailRepository repository;
    private final ProjectRepository projectRepository;
    private final GoogleDriveService driveService;

    @Transactional
    public DevelopDetailResponseDto saveDevelopItem(Long userId, Long projectId, String type, String text, List<MultipartFile> files) throws IOException {
        DevelopDetailEntity detail = getOrCreateDetail(projectId);
        detail.setUpdatedAt(LocalDateTime.now());

        List<FileMeta> metaList = uploadFiles(files, userId, projectId);
        List<FileMetaDto> uploadedDtos = toDtoList(metaList);

        switch (type.toLowerCase()) {
            case "environment" -> {
                if (text != null) detail.setEnvironmentText(text);
                detail.getEnvironmentFiles().addAll(metaList);
            }
            case "versioning" -> {
                if (text != null) detail.setVersioningText(text);
                detail.getVersioningFiles().addAll(metaList);
            }
            case "commitrule" -> {
                if (text != null) detail.setCommitRuleText(text);
                detail.getCommitRuleFiles().addAll(metaList);
            }
            case "folder" -> {
                if (text != null) detail.setFolderText(text);
                detail.getFolderFiles().addAll(metaList);
            }
            case "codingstandard" -> {
                if (text != null) detail.setCodingStandardText(text);
                detail.getCodingStandardFiles().addAll(metaList);
            }
            case "unittest" -> {
                if (text != null) detail.setUnitTestText(text);
                detail.getUnitTestFiles().addAll(metaList);
            }
            case "integrationtest" -> {
                if (text != null) detail.setIntegrationTestText(text);
                detail.getIntegrationTestFiles().addAll(metaList);
            }
            default -> throw new IllegalArgumentException("유효하지 않은 타입: " + type);
        }

        repository.save(detail);
        return new DevelopDetailResponseDto(type, text, uploadedDtos);
    }

    @Transactional
    public DevelopDetailResponseDto updateDevelopItem(Long userId, Long projectId, String type, String text, List<MultipartFile> files) throws IOException {
        DevelopDetailEntity detail = getOrCreateDetail(projectId);
        detail.setUpdatedAt(LocalDateTime.now());

        List<FileMeta> metaList = uploadFiles(files, userId, projectId);
        List<FileMetaDto> uploadedDtos = toDtoList(metaList);

        switch (type.toLowerCase()) {
            case "environment" -> {
                if (text != null) detail.setEnvironmentText(text);
                detail.getEnvironmentFiles().addAll(metaList);
            }
            case "versioning" -> {
                if (text != null) detail.setVersioningText(text);
                detail.getVersioningFiles().addAll(metaList);
            }
            case "commitrule" -> {
                if (text != null) detail.setCommitRuleText(text);
                detail.getCommitRuleFiles().addAll(metaList);
            }
            case "folder" -> {
                if (text != null) detail.setFolderText(text);
                detail.getFolderFiles().addAll(metaList);
            }
            case "codingstandard" -> {
                if (text != null) detail.setCodingStandardText(text);
                detail.getCodingStandardFiles().addAll(metaList);
            }
            case "unittest" -> {
                if (text != null) detail.setUnitTestText(text);
                detail.getUnitTestFiles().addAll(metaList);
            }
            case "integrationtest" -> {
                if (text != null) detail.setIntegrationTestText(text);
                detail.getIntegrationTestFiles().addAll(metaList);
            }
            default -> throw new IllegalArgumentException("유효하지 않은 타입: " + type);
        }

        repository.save(detail);
        return new DevelopDetailResponseDto(type, text, uploadedDtos);
    }

    @Transactional
    public void deleteDevelopFile(Long projectId, String type, String fileUrl) {
        DevelopDetailEntity detail = getOrCreateDetail(projectId);
        List<FileMeta> targetList;

        switch (type.toLowerCase()) {
            case "environment" -> targetList = detail.getEnvironmentFiles();
            case "versioning" -> targetList = detail.getVersioningFiles();
            case "commitrule" -> targetList = detail.getCommitRuleFiles();
            case "folder" -> targetList = detail.getFolderFiles();
            case "codingstandard" -> targetList = detail.getCodingStandardFiles();
            case "unittest" -> targetList = detail.getUnitTestFiles();
            case "integrationtest" -> targetList = detail.getIntegrationTestFiles();
            default -> throw new IllegalArgumentException("유효하지 않은 타입: " + type);
        }

        targetList.removeIf(meta -> meta.getUrl().equals(fileUrl));

        try {
            String fileId = driveService.extractDriveFileId(fileUrl);
            driveService.deleteFile(fileId);
        } catch (IOException e) {
            throw new RuntimeException("Google Drive 파일 삭제 실패", e);
        }

        repository.save(detail);
    }

    @Transactional
    public DevelopAllResponseDto getAllDetails(Long projectId) {
        DevelopDetailEntity detail = getOrCreateDetail(projectId);

        return DevelopAllResponseDto.builder()
                .environment(toItem(detail.getEnvironmentText(), detail.getEnvironmentFiles()))
                .versioning(toItem(detail.getVersioningText(), detail.getVersioningFiles()))
                .commitRule(toItem(detail.getCommitRuleText(), detail.getCommitRuleFiles()))
                .folder(toItem(detail.getFolderText(), detail.getFolderFiles()))
                .codingStandard(toItem(detail.getCodingStandardText(), detail.getCodingStandardFiles()))
                .unitTest(toItem(detail.getUnitTestText(), detail.getUnitTestFiles()))
                .integrationTest(toItem(detail.getIntegrationTestText(), detail.getIntegrationTestFiles()))
                .build();
    }

    private DevelopDetailEntity getOrCreateDetail(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        return repository.findByProject(project)
                .orElseGet(() -> {
                    DevelopDetailEntity detail = new DevelopDetailEntity();
                    detail.setProject(project);
                    return repository.save(detail);
                });
    }

    private List<FileMeta> uploadFiles(List<MultipartFile> files, Long userId, Long projectId) throws IOException {
        List<FileMeta> metaList = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = driveService.uploadFile(file, userId, projectId).getFileUrl();
                    metaList.add(new FileMeta(url, LocalDateTime.now(),file.getContentType()));
                }
            }
        }
        return metaList;
    }

    private List<FileMetaDto> toDtoList(List<FileMeta> metas) {
        return metas.stream()
                .map(meta -> new FileMetaDto(meta.getUrl(), meta.getUploadedAt(), meta.getFileType()))
                .toList();
    }

    private DevelopItemDto toItem(String text, List<FileMeta> files) {
        List<FileMetaDto> fileDtos = files.stream()
                .map(f -> new FileMetaDto(f.getUrl(), f.getUploadedAt(), f.getFileType()))
                .toList();
        return new DevelopItemDto(text, fileDtos);
    }
}