package com.together.project.worktask;

import com.together.project.worktask.dto.ScheduleUpdateDto;
import com.together.project.worktask.dto.WorkTaskRequestDto;
import com.together.project.worktask.dto.WorkTaskResponseDto;
import com.together.project.worktask.WorkTaskService;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WorkTask 관련 HTTP 요청을 처리하는 컨트롤러 클래스
 */
@RestController
@RequestMapping("/work-tasks")
@RequiredArgsConstructor
public class WorkTaskController {

    private final WorkTaskService service;

    /**
     * 새 작업을 생성합니다.
     * @param dto        작업 생성에 필요한 데이터
     * @param principal  인증된 사용자 정보 (UserDetailsImpl)
     * @return           생성된 작업 정보
     */
    @PostMapping
    public ResponseEntity<WorkTaskResponseDto> create(
            @RequestBody WorkTaskRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        // 인증된 UserEntity를 꺼냅니다.
        UserEntity user = principal.getUser();
        // 서비스 호출 후 생성된 WorkTaskResponseDto를 반환합니다.
        return ResponseEntity.ok(service.createWorkTask(dto, user));
    }

    /**
     * 현재 사용자의 프로젝트에 속한 최상위 작업(부모가 없는 작업) 목록을 조회합니다.
     * @param principal  인증된 사용자 정보
     * @return           작업 목록
     */
    @GetMapping("/project")
    public ResponseEntity<List<WorkTaskResponseDto>> list(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        UserEntity user = principal.getUser();
        // 프로젝트별 최상위 작업 리스트를 반환합니다.
        return ResponseEntity.ok(service.getTasks(user));
    }

    /**
     * 특정 작업을 전체 수정합니다.
     * 제목, 설명, 기간, 담당자, 상태, 상위 작업 등을 업데이트할 수 있습니다.
     * @param id         수정할 작업의 ID
     * @param dto        수정할 데이터
     * @param principal  인증된 사용자 정보
     * @return           수정된 작업 정보
     */
    @PatchMapping("/{id}")
    public ResponseEntity<WorkTaskResponseDto> update(
            @PathVariable Long id,
            @RequestBody WorkTaskRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        UserEntity user = principal.getUser();
        // 서비스에서 업데이트 후 반환된 DTO를 응답합니다.
        return ResponseEntity.ok(service.updateWorkTask(id, dto, user));
    }

    /**
     * 그래프 드래그 등으로 기간(startDate, endDate)만 수정할 때 호출합니다.
     * @param id         수정할 작업의 ID
     * @param dto        변경된 기간 정보
     * @param principal  인증된 사용자 정보
     * @return           수정된 작업 정보
     */
    @PatchMapping("/{id}/schedule")
    public ResponseEntity<WorkTaskResponseDto> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleUpdateDto dto,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        UserEntity user = principal.getUser();
        return ResponseEntity.ok(service.updateSchedule(id, dto, user));
    }

    /**
     * 특정 작업을 삭제합니다.
     * @param id         삭제할 작업의 ID
     * @param principal  인증된 사용자 정보
     * @return           응답 본문 없이 204 상태코드 반환
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        UserEntity user = principal.getUser();
        service.deleteWorkTask(id, user);
        return ResponseEntity.noContent().build();
    }
}
