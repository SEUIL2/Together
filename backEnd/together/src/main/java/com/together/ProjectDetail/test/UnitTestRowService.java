package com.together.ProjectDetail.test;

import com.together.ProjectDetail.test.dto.UnitTestRowRequestDto;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.ProjectDetail.test.dto.UnitTestRowRequestDto;
import com.together.ProjectDetail.test.dto.UnitTestRowResponseDto;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 단위 테스트(Unit) 행 비즈니스 로직
 * - 생성/수정/삭제/조회/토글
 */
@Service
@RequiredArgsConstructor
public class UnitTestRowService {

    private final UnitTestRowRepository repository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 행 생성
    @Transactional
    public UnitTestRowResponseDto create(Long projectId, Long authorId, UnitTestRowRequestDto dto) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UnitTestRowEntity row = UnitTestRowEntity.builder()
                .testId(dto.getTestId())
                .methodName(dto.getMethodName())
                .caseDesc(dto.getCaseDesc())
                .expectedResult(dto.getExpectedResult())
                .actualResult(dto.getActualResult())
                .inputs(dto.getInputs())
                .caseType(dto.getCaseType())
                .linkedIntegrationId(dto.getLinkedIntegrationId())
                .project(project)
                .author(author)
                .completed(Boolean.TRUE.equals(dto.getCompleted()))
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(row);
        return toDto(row);
    }

    // 행 단건 조회(옵션)
    @Transactional(readOnly = true)
    public UnitTestRowResponseDto get(Long rowId) {
        UnitTestRowEntity row = repository.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("행을 찾을 수 없습니다."));
        return toDto(row);
    }

    // 행 수정(부분 업데이트)
    @Transactional
    public UnitTestRowResponseDto update(Long rowId, UnitTestRowRequestDto dto) {
        UnitTestRowEntity row = repository.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("행을 찾을 수 없습니다."));

        if (dto.getTestId() != null) row.setTestId(dto.getTestId());
        if (dto.getMethodName() != null) row.setMethodName(dto.getMethodName());
        if (dto.getCaseDesc() != null) row.setCaseDesc(dto.getCaseDesc());
        if (dto.getExpectedResult() != null) row.setExpectedResult(dto.getExpectedResult());
        if (dto.getActualResult() != null) row.setActualResult(dto.getActualResult());
        if (dto.getInputs() != null) row.setInputs(dto.getInputs());
        if (dto.getCaseType() != null) row.setCaseType(dto.getCaseType());
        if (dto.getLinkedIntegrationId() != null) row.setLinkedIntegrationId(dto.getLinkedIntegrationId());
        if (dto.getCompleted() != null) row.setCompleted(dto.getCompleted());

        row.setUpdatedAt(LocalDateTime.now());
        return toDto(row);
    }

    // 행 삭제 (요청하신 '삭제 기능' 명확히 포함)
    @Transactional
    public void delete(Long rowId) {
        if (!repository.existsById(rowId)) {
            throw new IllegalArgumentException("삭제할 행을 찾을 수 없습니다.");
        }
        repository.deleteById(rowId);
    }

    // 프로젝트별 목록
    @Transactional(readOnly = true)
    public List<UnitTestRowResponseDto> listByProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));
        return repository.findByProject(project).stream().map(this::toDto).toList();
    }

    // 완료 여부 토글
    @Transactional
    public UnitTestRowResponseDto toggleCompleted(Long rowId) {
        UnitTestRowEntity row = repository.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("행을 찾을 수 없습니다."));
        row.setCompleted(!row.isCompleted()); // true ↔ false
        row.setUpdatedAt(LocalDateTime.now());
        return toDto(row);
    }

    // 엔티티 → DTO 매핑
    private UnitTestRowResponseDto toDto(UnitTestRowEntity r) {
        return new UnitTestRowResponseDto(
                r.getId(), r.getTestId(), r.getMethodName(), r.getCaseDesc(),
                r.getExpectedResult(), r.getActualResult(), r.getInputs(),
                r.getCaseType(), r.getLinkedIntegrationId(), r.isCompleted(),
                r.getAuthor().getUserName(), r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
