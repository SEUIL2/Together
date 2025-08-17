package com.together.worktask;

import com.together.notification.NotificationService;
import com.together.project.ProjectEntity;
import com.together.worktask.dto.ScheduleUpdateDto;
import com.together.worktask.dto.WorkTaskRequestDto;
import com.together.worktask.dto.WorkTaskResponseDto;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.professor.ProfessorEntity;
import com.together.user.student.StudentEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkTaskService {

    private final WorkTaskRepository workTaskRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    /** 작업 생성 */
    @Transactional
    public WorkTaskResponseDto createWorkTask(WorkTaskRequestDto dto, ProjectEntity project) {
        if (project == null) {
            throw new EntityNotFoundException("프로젝트가 없습니다.");
        }

        var task = new WorkTaskEntity();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                task.setStatus(WorkTaskStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("잘못된 상태 값입니다: " + dto.getStatus());
            }
        }
        task.setProject(project);

        // 담당자 검증 & 설정
        if (dto.getAssignedUserId() != null) {
            UserEntity assignee = userRepo.findById(dto.getAssignedUserId())
                    .orElseThrow(() -> new EntityNotFoundException("지정한 사용자를 찾을 수 없습니다."));

            boolean belongs;
            if (assignee instanceof StudentEntity) {
                var mainProj = ((StudentEntity) assignee).getMainProject();
                belongs = mainProj != null
                        && mainProj.getProjectId().equals(project.getProjectId());
            } else if (assignee instanceof ProfessorEntity) {
                belongs = ((ProfessorEntity) assignee).getProjects().stream()
                        .anyMatch(p -> p.getProjectId().equals(project.getProjectId()));
            } else {
                belongs = false;
            }

            if (!belongs) {
                throw new IllegalArgumentException("같은 프로젝트에 속한 사용자만 할당할 수 있습니다.");
            }
            task.setAssignedUser(assignee);
        }

        // 상위 작업 검증 & 설정
        if (dto.getParentTaskId() != null) {
            var parent = workTaskRepo.findById(dto.getParentTaskId())
                    .orElseThrow(() -> new EntityNotFoundException("상위 작업을 찾을 수 없습니다."));
            if (!parent.getProject().getProjectId().equals(project.getProjectId())) {
                throw new IllegalArgumentException("같은 프로젝트의 작업만 상위 작업으로 지정할 수 있습니다.");
            }
            task.setParentTask(parent);
        }

        var now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);


        // ⭐ 알림 전송 코드 추가
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder // SecurityContextHolder : 로그인한 사용자 정보를 보관하는 Spring Security 의 전역 컨텍스트
                .getContext()
                .getAuthentication()
                .getPrincipal(); // 현재 로그인한 사용자의 인증 객체를 가져옴
        UserEntity currentUser = userDetails.getUser(); // 현재 로그인된 사용자의 UserEntity 객체를 가져옴, 알림 보낼 때 "본인 제외" 처리를 위해 사용됨
        List<StudentEntity> projectStudents = project.getStudents(); // 작업이 속한 ProjectEntity 에서 연결된 학생 리스트 (List<StudentEntity>)를 가져옴

        if (projectStudents != null) {
            for (UserEntity notificationUser : projectStudents) { // 프로젝트에 소속된 모든 학생들에 대해 반복문 시작
                if (!notificationUser.getUserId().equals(currentUser.getUserId())) { // 현재 로그인한 사용자와 동일하지 않은 경우에만 아래 알림을 보냄
                    notificationService.sendNotification(
                            notificationUser.getUserId(),
                            "작업 생성",
                            task.getTitle() + " 작업이 생성되었습니다."
                    );
                }
            }
        }

        return mapToDto(workTaskRepo.save(task));
    }

    /** 프로젝트 최상위 작업 조회 */
    @Transactional(readOnly = true)
    public List<WorkTaskResponseDto> getTasks(ProjectEntity project) {
        if (project == null) {
            throw new EntityNotFoundException("프로젝트가 없습니다.");
        }
        return workTaskRepo.findByProject_ProjectId(project.getProjectId()).stream()
                .filter(t -> t.getParentTask() == null)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /** 작업 전체 수정 */
    @Transactional
    public WorkTaskResponseDto updateWorkTask(Long id, WorkTaskRequestDto dto, ProjectEntity project) {
        if (project == null) {
            throw new EntityNotFoundException("프로젝트가 없습니다.");
        }

        var task = workTaskRepo.findById(id)
                .filter(t -> t.getProject() != null
                        && t.getProject().getProjectId().equals(project.getProjectId()))
                .orElseThrow(() -> new EntityNotFoundException("작업을 찾을 수 없습니다."));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                task.setStatus(WorkTaskStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("잘못된 상태 값입니다: " + dto.getStatus());
            }
        }

        // 담당자 수정
        if (dto.getAssignedUserId() != null) {
            UserEntity assignee = userRepo.findById(dto.getAssignedUserId())
                    .orElseThrow(() -> new EntityNotFoundException("지정한 사용자를 찾을 수 없습니다."));
            // (위와 같은 검증 로직 재사용 가능)
            task.setAssignedUser(assignee);
        } else {
            task.setAssignedUser(null);
        }

        // 상위 작업 수정
        if (dto.getParentTaskId() != null) {
            var parent = workTaskRepo.findById(dto.getParentTaskId())
                    .orElseThrow(() -> new EntityNotFoundException("상위 작업을 찾을 수 없습니다."));
            task.setParentTask(parent);
        } else {
            task.setParentTask(null);
        }

        task.setUpdatedAt(LocalDateTime.now());
        return mapToDto(task);
    }

    /** 기간만 수정 */
    @Transactional
    public WorkTaskResponseDto updateSchedule(Long id, ScheduleUpdateDto dto, ProjectEntity project) {
        if (project == null) {
            throw new EntityNotFoundException("프로젝트가 없습니다.");
        }

        var task = workTaskRepo.findById(id)
                .filter(t -> t.getProject() != null
                        && t.getProject().getProjectId().equals(project.getProjectId()))
                .orElseThrow(() -> new EntityNotFoundException("작업을 찾을 수 없습니다."));

        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setUpdatedAt(LocalDateTime.now());
        return mapToDto(task);
    }

    /** 작업 삭제 */
    @Transactional
    public void deleteWorkTask(Long id, ProjectEntity project) {
        if (project == null) {
            throw new EntityNotFoundException("프로젝트가 없습니다.");
        }

        var task = workTaskRepo.findById(id)
                .filter(t -> t.getProject() != null
                        && t.getProject().getProjectId().equals(project.getProjectId()))
                .orElseThrow(() -> new EntityNotFoundException("작업을 찾을 수 없습니다."));
        workTaskRepo.delete(task);
    }

    /** DTO 변환 (재귀) */
    private WorkTaskResponseDto mapToDto(WorkTaskEntity t) {
        var children = t.getChildTasks().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new WorkTaskResponseDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStartDate(),
                t.getEndDate(),
                t.getAssignedUser() != null ? t.getAssignedUser().getUserId() : null,
                t.getAssignedUser() != null ? t.getAssignedUser().getUserName() : null,
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getParentTask() != null ? t.getParentTask().getId() : null,
                children
        );
    }
}
