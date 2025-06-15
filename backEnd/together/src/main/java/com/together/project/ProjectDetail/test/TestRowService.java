package com.together.project.ProjectDetail.test;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.project.ProjectDetail.test.dto.TestRowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestRowService {

    private final TestRowRepository testRowRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /** 테스트 행 등록(저장) */
    @Transactional
    public TestRowDto createTestRow(Long projectId, TestTableType tableType, String itemName, String description, Long authorId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다."));

        TestRowEntity row = TestRowEntity.builder()
                .project(project)
                .tableType(tableType)
                .itemName(itemName)
                .description(description)
                .author(author)
                .createdDate(LocalDate.now())
                .completed(false)
                .build();

        testRowRepository.save(row);
        return toDto(row);
    }

    /** 테스트 행 수정 */
    @Transactional
    public TestRowDto updateTestRow(Long rowId, String itemName, String description, Boolean completed) {
        TestRowEntity row = testRowRepository.findById(rowId)
                .orElseThrow(() -> new RuntimeException("테스트 행을 찾을 수 없습니다."));

        if (itemName != null) row.setItemName(itemName);
        if (description != null) row.setDescription(description);
        if (completed != null) row.setCompleted(completed);

        return toDto(row);
    }

    /** 테스트 행 삭제 */
    @Transactional
    public void deleteTestRow(Long rowId) {
        TestRowEntity row = testRowRepository.findById(rowId)
                .orElseThrow(() -> new RuntimeException("테스트 행을 찾을 수 없습니다."));
        testRowRepository.delete(row);
    }

    /** 완료여부(체크박스) 토글 */
    @Transactional
    public TestRowDto toggleCompleted(Long rowId) {
        TestRowEntity row = testRowRepository.findById(rowId)
                .orElseThrow(() -> new RuntimeException("테스트 행을 찾을 수 없습니다."));
        row.setCompleted(!row.isCompleted());
        return toDto(row);
    }

    /** 프로젝트 + 유형별(단위/통합) 전체 조회 */
    @Transactional(readOnly = true)
    public List<TestRowDto> getRowsByProjectAndType(Long projectId, TestTableType tableType) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        return testRowRepository.findByProjectAndTableType(project, tableType)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** 단일 행 조회 */
    @Transactional(readOnly = true)
    public TestRowDto getTestRowById(Long rowId) {
        TestRowEntity row = testRowRepository.findById(rowId)
                .orElseThrow(() -> new RuntimeException("테스트 행을 찾을 수 없습니다."));
        return toDto(row);
    }

    /** 엔티티→DTO 변환 */
    private TestRowDto toDto(TestRowEntity row) {
        TestRowDto dto = new TestRowDto();
        dto.setId(row.getId());
        dto.setItemName(row.getItemName());
        dto.setDescription(row.getDescription());
        dto.setAuthorName(row.getAuthor() != null ? row.getAuthor().getUserName() : null);
        dto.setCreatedDate(row.getCreatedDate());
        dto.setCompleted(row.isCompleted());
        return dto;
    }
}
