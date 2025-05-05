// src/main/java/com/together/project/worktask/service/WorkTaskService.java
package com.together.project.worktask;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.project.worktask.WorkTaskEntity;
import com.together.project.worktask.WorkTaskStatus;
import com.together.project.worktask.dto.ScheduleUpdateDto;
import com.together.project.worktask.dto.WorkTaskRequestDto;
import com.together.project.worktask.dto.WorkTaskResponseDto;
import com.together.project.worktask.WorkTaskRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkTaskService {
    private final WorkTaskRepository workTaskRepo;
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;

    /** 생성 */
    @Transactional
    public WorkTaskResponseDto createWorkTask(WorkTaskRequestDto dto, UserEntity user) {
        ProjectEntity proj = user.getProject();
        if (proj == null) throw new EntityNotFoundException("프로젝트가 없습니다.");

        WorkTaskEntity task = new WorkTaskEntity();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setStatus(dto.getStatus() != null
                ? WorkTaskStatus.valueOf(dto.getStatus().toUpperCase())
                : null);
        task.setProject(proj);

        // 담당자 검증 및 설정
        if (dto.getAssignedUserId() != null) {
            UserEntity assignee = userRepo.findById(dto.getAssignedUserId())
                    .orElseThrow(() -> new EntityNotFoundException("지정한 사용자를 찾을 수 없습니다."));
            if (!proj.getProjectId().equals(assignee.getProject().getProjectId())) {
                throw new IllegalArgumentException("같은 프로젝트에 속한 사용자만 할당할 수 있습니다.");
            }
            task.setAssignedUser(assignee);
        }

        // 상위 작업 설정
        if (dto.getParentTaskId() != null) {
            WorkTaskEntity parent = workTaskRepo.findById(dto.getParentTaskId())
                    .orElseThrow(() -> new EntityNotFoundException("상위 작업을 찾을 수 없습니다."));
            if (!proj.getProjectId().equals(parent.getProject().getProjectId())) {
                throw new IllegalArgumentException("같은 프로젝트의 작업만 상위 작업으로 지정할 수 있습니다.");
            }
            task.setParentTask(parent);
        }

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        return mapToDto(workTaskRepo.save(task));
    }


    /** 조회(프로젝트의 최상위 작업만) */
    @Transactional(readOnly = true)
    public List<WorkTaskResponseDto> getTasks(UserEntity user) {
        Long projId = user.getProject().getProjectId();
        return workTaskRepo.findByProject_ProjectId(projId).stream()
                .filter(t -> t.getParentTask() == null)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /** 전체 수정 */
    @Transactional
    public WorkTaskResponseDto updateWorkTask(Long id, WorkTaskRequestDto dto, UserEntity user) {
        Long projId = user.getProject().getProjectId();
        WorkTaskEntity task = workTaskRepo.findById(id)
                .filter(t -> t.getProject() != null && t.getProject().getProjectId().equals(projId))
                .orElseThrow(() -> new EntityNotFoundException("작업을 찾을 수 없습니다."));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) {
            task.setStatus(WorkTaskStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        // 담당자 수정
        if (dto.getAssignedUserId() != null) {
            userRepo.findById(dto.getAssignedUserId())
                    .filter(u -> u.getProject() != null && u.getProject().getProjectId().equals(projId))
                    .ifPresent(task::setAssignedUser);
        } else {
            task.setAssignedUser(null);
        }
        // 상위작업 수정
        if (dto.getParentTaskId() != null) {
            workTaskRepo.findById(dto.getParentTaskId())
                    .filter(t -> t.getProject() != null && t.getProject().getProjectId().equals(projId))
                    .ifPresent(task::setParentTask);
        } else {
            task.setParentTask(null);
        }

        task.setUpdatedAt(LocalDateTime.now());
        return mapToDto(task);
    }

    /** 기간만 수정 (드래그) */
    @Transactional
    public WorkTaskResponseDto updateSchedule(Long id, ScheduleUpdateDto dto, UserEntity user) {
        Long projId = user.getProject().getProjectId();
        WorkTaskEntity task = workTaskRepo.findById(id)
                .filter(t -> t.getProject() != null && t.getProject().getProjectId().equals(projId))
                .orElseThrow(() -> new EntityNotFoundException("작업을 찾을 수 없습니다."));

        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setUpdatedAt(LocalDateTime.now());
        return mapToDto(task);
    }

    /** 삭제 */
    @Transactional
    public void deleteWorkTask(Long id, UserEntity user) {
        Long projId = user.getProject().getProjectId();
        WorkTaskEntity task = workTaskRepo.findById(id)
                .filter(t -> t.getProject() != null && t.getProject().getProjectId().equals(projId))
                .orElseThrow(() -> new EntityNotFoundException("작업을 찾을 수 없습니다."));
        workTaskRepo.delete(task);
    }

    /** 재귀 매핑 */
    private WorkTaskResponseDto mapToDto(WorkTaskEntity t) {
        List<WorkTaskResponseDto> children = t.getChildTasks().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new WorkTaskResponseDto(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getStartDate(), t.getEndDate(),
                t.getAssignedUser() != null ? t.getAssignedUser().getUserId() : null,
                t.getAssignedUser() != null ? t.getAssignedUser().getUserName(): null,
                t.getStatus() != null ? t.getStatus().name(): null,
                t.getParentTask() != null ? t.getParentTask().getId(): null,
                children
        );
    }
}
