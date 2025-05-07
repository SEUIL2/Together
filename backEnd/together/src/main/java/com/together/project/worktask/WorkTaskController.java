package com.together.project.worktask;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.util.customAnnotation.CurrentProject;
import com.together.project.worktask.dto.ScheduleUpdateDto;
import com.together.project.worktask.dto.WorkTaskRequestDto;
import com.together.project.worktask.dto.WorkTaskResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

/**
 * WorkTask 관련 HTTP 요청을 처리하는 컨트롤러 클래스
 */
@Slf4j
@RestController
@RequestMapping("/work-tasks")
@RequiredArgsConstructor
public class WorkTaskController {

    private final WorkTaskService service;
    private final ProjectRepository projectRepository;

    /**
     * 새 작업을 생성합니다.
     * @param dto        작업 생성에 필요한 데이터
     * @param projectId  @CurrentProject로 주입된 프로젝트 ID
     * @return           생성된 작업 정보
     */
    @PostMapping
    public ResponseEntity<WorkTaskResponseDto> create(
            @RequestBody WorkTaskRequestDto dto,
            @CurrentProject Long projectId
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID=" + projectId));
        return ResponseEntity.ok(service.createWorkTask(dto, project));
    }

    /**
     * 현재 프로젝트의 최상위 작업(부모가 없는 작업) 목록을 조회합니다.
     * @param projectId  @CurrentProject로 주입된 프로젝트 ID
     * @return           작업 목록
     */
    @GetMapping("/project")
    public ResponseEntity<List<WorkTaskResponseDto>> list(
            @CurrentProject Long projectId
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID=" + projectId));
        return ResponseEntity.ok(service.getTasks(project));
    }

    /**
     * 특정 작업을 전체 수정합니다.
     * 제목, 설명, 기간, 담당자, 상태, 상위 작업 등을 업데이트할 수 있습니다.
     * @param id         수정할 작업의 ID
     * @param dto        수정할 데이터
     * @param projectId  @CurrentProject로 주입된 프로젝트 ID
     * @return           수정된 작업 정보
     */
    @PatchMapping("/{id}")
    public ResponseEntity<WorkTaskResponseDto> update(
            @PathVariable Long id,
            @RequestBody WorkTaskRequestDto dto,
            @CurrentProject Long projectId
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID=" + projectId));
        return ResponseEntity.ok(service.updateWorkTask(id, dto, project));
    }

    /**
     * 그래프 드래그 등으로 기간(startDate, endDate)만 수정할 때 호출합니다.
     * @param id         수정할 작업의 ID
     * @param dto        변경된 기간 정보
     * @param projectId  @CurrentProject로 주입된 프로젝트 ID
     * @return           수정된 작업 정보
     */
    @PatchMapping("/{id}/schedule")
    public ResponseEntity<WorkTaskResponseDto> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleUpdateDto dto,
            @CurrentProject Long projectId
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID=" + projectId));
        return ResponseEntity.ok(service.updateSchedule(id, dto, project));
    }

    /**
     * 특정 작업을 삭제합니다.
     * @param id         삭제할 작업의 ID
     * @param projectId  @CurrentProject로 주입된 프로젝트 ID
     * @return           응답 본문 없이 204 상태코드 반환
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @CurrentProject Long projectId
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. ID=" + projectId));
        service.deleteWorkTask(id, project);
        return ResponseEntity.noContent().build();
    }
}
