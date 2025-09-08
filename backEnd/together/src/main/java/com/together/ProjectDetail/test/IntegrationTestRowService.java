package com.together.ProjectDetail.test;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.ProjectDetail.test.dto.IntegrationTestRowRequestDto;
import com.together.ProjectDetail.test.dto.IntegrationTestRowResponseDto;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통합 테스트(Integration) 행 비즈니스 로직
 * - 생성/수정/삭제/조회/토글
 */
@Service
@RequiredArgsConstructor
public class IntegrationTestRowService {

    private final IntegrationTestRowRepository repository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 행 생성
    @Transactional
    public IntegrationTestRowResponseDto create(Long projectId, Long authorId, IntegrationTestRowRequestDto dto) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        IntegrationTestRowEntity row = IntegrationTestRowEntity.builder()
                .testId(dto.getTestId())
                .moduleName(dto.getModuleName())
                .scenario(dto.getScenario())
                .expected(dto.getExpected())
                .result(dto.getResult())
                .note(dto.getNote())
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
    public IntegrationTestRowResponseDto get(Long rowId) {
        IntegrationTestRowEntity row = repository.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("행을 찾을 수 없습니다."));
        return toDto(row);
    }

    // 행 수정(부분 업데이트)
    @Transactional
    public IntegrationTestRowResponseDto update(Long rowId, IntegrationTestRowRequestDto dto) {
        IntegrationTestRowEntity row = repository.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("행을 찾을 수 없습니다."));

        if (dto.getTestId() != null) row.setTestId(dto.getTestId());
        if (dto.getModuleName() != null) row.setModuleName(dto.getModuleName());
        if (dto.getScenario() != null) row.setScenario(dto.getScenario());
        if (dto.getExpected() != null) row.setExpected(dto.getExpected());
        if (dto.getResult() != null) row.setResult(dto.getResult());
        if (dto.getNote() != null) row.setNote(dto.getNote());
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
    public List<IntegrationTestRowResponseDto> listByProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));
        return repository.findByProject(project).stream().map(this::toDto).toList();
    }

    // 완료 여부 토글
    @Transactional
    public IntegrationTestRowResponseDto toggleCompleted(Long rowId) {
        IntegrationTestRowEntity row = repository.findById(rowId)
                .orElseThrow(() -> new IllegalArgumentException("행을 찾을 수 없습니다."));
        row.setCompleted(!row.isCompleted()); // true ↔ false
        row.setUpdatedAt(LocalDateTime.now());
        return toDto(row);
    }

    // 엔티티 → DTO 매핑
    private IntegrationTestRowResponseDto toDto(IntegrationTestRowEntity r) {
        return new IntegrationTestRowResponseDto(
                r.getId(), r.getTestId(), r.getModuleName(), r.getScenario(),
                r.getExpected(), r.getResult(), r.getNote(),
                r.isCompleted(), r.getAuthor().getUserName(),
                r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
