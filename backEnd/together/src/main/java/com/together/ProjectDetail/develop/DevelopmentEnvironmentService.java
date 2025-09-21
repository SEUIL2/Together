package com.together.ProjectDetail.develop;

import com.together.ProjectDetail.develop.dto.DevelopmentEnvironmentRequestDto;
import com.together.ProjectDetail.develop.dto.DevelopmentEnvironmentResponseDto;
import com.together.project.ProjectEntity; // ProjectEntity 경로
import com.together.project.ProjectRepository; // ProjectRepository 경로
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DevelopmentEnvironmentService {

    private final DevelopmentEnvironmentRepository developmentEnvironmentRepository;
    private final ProjectRepository projectRepository;

    /**
     * 1. 저장 (Create)
     */
    @Transactional
    public Long save(Long projectId, DevelopmentEnvironmentRequestDto requestDto) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다. id=" + projectId));

        DevelopmentEnvironmentEntity devEnvEntity = requestDto.toEntity();
        project.addDevelopmentEnvironmentEntity(devEnvEntity); // 연관관계 설정

        developmentEnvironmentRepository.save(devEnvEntity);
        return devEnvEntity.getId();
    }

    /**
     * 2. 수정 (Update)
     */
    @Transactional
    public Long update(Long devEnvId, DevelopmentEnvironmentRequestDto requestDto) {
        DevelopmentEnvironmentEntity devEnvEntity = developmentEnvironmentRepository.findById(devEnvId)
                .orElseThrow(() -> new IllegalArgumentException("해당 개발 환경 정보가 존재하지 않습니다. id=" + devEnvId));

        // setter를 이용해 필드 값 변경
        devEnvEntity.setOperatingSystem(requestDto.getOperatingSystem());
        devEnvEntity.setIde(requestDto.getIde());
        devEnvEntity.setDevLanguage(requestDto.getDevLanguage());
        devEnvEntity.setFramework(requestDto.getFramework());
        devEnvEntity.setVersionControl(requestDto.getVersionControl());
        devEnvEntity.setDatabase(requestDto.getDatabase());
        devEnvEntity.setEtc(requestDto.getEtc());

        // 트랜잭션이 끝날 때 변경된 내용을 자동으로 데이터베이스에 반영 (JPA의 Dirty Checking)
        return devEnvEntity.getId();
    }

    /**
     * 3. 삭제 (Delete)
     */
    @Transactional
    public void delete(Long devEnvId) {
        DevelopmentEnvironmentEntity devEnvEntity = developmentEnvironmentRepository.findById(devEnvId)
                .orElseThrow(() -> new IllegalArgumentException("해당 개발 환경 정보가 존재하지 않습니다. id=" + devEnvId));
        developmentEnvironmentRepository.delete(devEnvEntity);
    }

    /**
     * 4-1. 개별 조회 (Read)
     */
    public DevelopmentEnvironmentResponseDto findById(Long devEnvId) {
        DevelopmentEnvironmentEntity entity = developmentEnvironmentRepository.findById(devEnvId)
                .orElseThrow(() -> new IllegalArgumentException("해당 개발 환경 정보가 존재하지 않습니다. id=" + devEnvId));
        return new DevelopmentEnvironmentResponseDto(entity);
    }

    /**
     * 4-2. 프로젝트별 전체 조회 (Read)
     */
    public List<DevelopmentEnvironmentResponseDto> findAllByProjectId(Long projectId) {
        List<DevelopmentEnvironmentEntity> entities = developmentEnvironmentRepository.findAllByProjectEntity_ProjectId(projectId);
        return entities.stream()
                .map(DevelopmentEnvironmentResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 5. 개발 언어로 검색 (Search)
     */
    public List<DevelopmentEnvironmentResponseDto> searchByDevLanguage(String keyword) {
        List<DevelopmentEnvironmentEntity> entities = developmentEnvironmentRepository.findByDevLanguageContainingIgnoreCase(keyword);
        return entities.stream()
                .map(DevelopmentEnvironmentResponseDto::new)
                .collect(Collectors.toList());
    }
}