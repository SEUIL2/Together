package com.together.project;

import com.together.documentManger.GoogleDriveService;
import com.together.notification.NotificationService;
import com.together.project.Invitation.InvitationEntity;
import com.together.project.Invitation.InvitationRepository;
import com.together.project.Invitation.dto.InvitationResponseDto;
import com.together.project.Invitation.dto.TeamMemberDto;
import com.together.project.ProjectDto.InviteResponseDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.dto.UserResponseDto;
import com.together.user.professor.ProfessorEntity;
import com.together.user.professor.ProfessorResponseDto;
import com.together.user.student.StudentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final NotificationService notificationService;
    private final GoogleDriveService googleDriveService;


    /**
     * 이미지 없이 프로젝트 생성 (title만 받음)
     */
    @Transactional
    public ProjectResponseDto createProject(String title) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();

        UserEntity user = userRepository.findByUserLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));

        if (user instanceof StudentEntity student && student.getMainProject() != null) {
            throw new RuntimeException("학생은 이미 하나의 프로젝트만 가질 수 있습니다.");
        }

        ProjectEntity project = new ProjectEntity();
        project.setTitle(title);
        project.setImageUrl(null); // ✅ 이미지 없이 생성
        project.addUser(user);
        project.setLeader(user); //프로젝트 리더로 설정

        ProjectEntity savedProject = projectRepository.save(project);

        if (user instanceof StudentEntity) {
            ((StudentEntity) user).setMainProject(savedProject);
        } else if (user instanceof ProfessorEntity) {
            ((ProfessorEntity) user).getProjects().add(savedProject);
        }

        return new ProjectResponseDto(
                savedProject.getProjectId(),
                savedProject.getTitle(),
                null  // imageUrl 없이 반환
        );
    }

    // 이미지 포함한 프로젝트 생성 (오버로딩된 메서드)
    @Transactional
    public ProjectResponseDto createProject(String title, String imageUrl) {
        // 현재 로그인 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();

        UserEntity user = userRepository.findByUserLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));

        // 학생이 이미 프로젝트를 보유하고 있다면 예외 처리
        if (user instanceof StudentEntity) {
            StudentEntity student = (StudentEntity) user;
            if (student.getMainProject() != null) {
                throw new RuntimeException("학생은 이미 하나의 프로젝트만 가질 수 있습니다.");
            }
        }

        // 프로젝트 생성 및 유저 연결
        ProjectEntity project = new ProjectEntity();
        project.setTitle(title);
        project.setImageUrl(imageUrl); // ✅ 이미지 URL 설정
        project.addUser(user);
        project.setLeader(user); //프로젝트 리더로 설정

        ProjectEntity savedProject = projectRepository.save(project);

        // 학생 또는 교수 연관 설정
        if (user instanceof StudentEntity student) {
            student.setMainProject(savedProject);
        } else if (user instanceof ProfessorEntity professor) {
            professor.getProjects().add(savedProject);
        }

        // DTO 반환
        return new ProjectResponseDto(
                savedProject.getProjectId(),
                savedProject.getTitle(),
                savedProject.getImageUrl() // ✅ DTO에 포함
        );
    }
    //제목수정
    @Transactional
    public ProjectResponseDto updateProjectTitle(Long projectId, String newTitle) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        project.setTitle(newTitle);
        ProjectEntity updatedProject = projectRepository.save(project);

        return new ProjectResponseDto(
                updatedProject.getProjectId(),
                updatedProject.getTitle(),
                updatedProject.getImageUrl()
        );
    }

    /**
     * 프로젝트 이미지 업로드 후 imageUrl 저장
     *
     * @param projectId 대상 프로젝트 ID
     * @param imageUrl Google Drive에 업로드된 이미지 URL
     */
    @Transactional
    public void updateProjectImage(Long projectId, String imageUrl) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        project.setImageUrl(imageUrl); // ✅ URL 업데이트
        projectRepository.save(project);
    }
    /**
     * 프로젝트에 등록된 이미지 URL을 제거하고
     * 해당 이미지 파일을 Google Drive에서도 삭제한다
     *
     * @param projectId 삭제할 이미지가 속한 프로젝트의 ID
     */
    @Transactional
    public void deleteProjectImage(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        String imageUrl = project.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fileId = googleDriveService.extractDriveFileId(imageUrl);

            try {
                googleDriveService.deleteFile(fileId);  // ✅ 예외 처리
            } catch (IOException e) {
                throw new RuntimeException("Google Drive 이미지 삭제 중 오류 발생", e);
            }
        }

        project.setImageUrl(null);
        projectRepository.save(project);
    }
    // 사용자 검색
    public List<UserResponseDto> searchUserByEmail(String email) {
        List<UserEntity> users = userRepository.findByUserEmailContaining(email);

        if (users == null || users.isEmpty()) {
            throw new RuntimeException("해당 이메일을 가진 사용자를 찾을 수 없습니다.");
        }

        return users.stream()
                .map(user -> new UserResponseDto(
                        user.getUserId(),
                        user.getUserName(),
                        user.getUserEmail(),
                        user.getRole() != null ? user.getRole().name() : "UNKNOWN"
                ))
                .toList();
    }

    // ✅ 팀원 초대
    @Transactional
    public boolean inviteUserToProject(Long projectId, String email) {
        // 프로젝트와 유저 조회
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트 없음"));
        UserEntity user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 1️⃣ 학생인 경우, 이미 mainProject가 설정되어 있으면 초대할 수 없음
        if (user instanceof StudentEntity) {
            StudentEntity student = (StudentEntity) user;
            if (student.getMainProject() != null) {
                throw new RuntimeException("학생은 이미 하나의 프로젝트에만 속할 수 있습니다.");
            }
        }

        // 2️⃣ 중복 초대 체크
        Optional<InvitationEntity> existing = invitationRepository.findByProjectAndUser(project, user);
        if (existing.isPresent()) {
            String status = existing.get().getStatus();
            if ("ACCEPTED".equals(status)) {
                throw new RuntimeException("이미 참여 중");
            } else if ("REJECTED".equals(status)) {
                existing.get().setStatus("PENDING");
                invitationRepository.save(existing.get());
                return true;
            }
            return false;
        }

        // 3️⃣ 새로운 초대 생성
        InvitationEntity invitation = new InvitationEntity();
        invitation.setProject(project);
        invitation.setUser(user);
        invitation.setStatus("PENDING");
        invitationRepository.save(invitation);

        // ⭐ 알림 전송 코드 추가
        String message = String.format("%s님이 사용자를 %s 프로젝트에 초대하였습니다.",
                project.getStudents().get(0).getUserName(), project.getTitle());
        notificationService.sendNotification(
                user.getUserId(),
                message,
                "/projects");

        return true;
    }

    // ✅ 나의 초대 목록
    public List<InvitationResponseDto> getUserInvitations(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        List<InvitationEntity> list = invitationRepository.findByUser(user);

        return list.stream()
                .map(inv -> new InvitationResponseDto(
                        inv.getId(),
                        inv.getProject().getTitle(),
                        inv.getUser().getUserName(),
                        inv.getStatus()
                )).toList();
    }


    // ✅ 초대 수락
    @Transactional
    public boolean acceptInvitation(Long invitationId) {
        InvitationEntity invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 없음"));

        invitation.setStatus("ACCEPTED");
        invitation.setAccepted(true);

        ProjectEntity project = invitation.getProject();
        UserEntity user = invitation.getUser();

        if (user instanceof StudentEntity student) {
            if (student.getMainProject() == null || !student.getMainProject().equals(project)) {
                student.setMainProject(project); // 메인 프로젝트 지정
            }
            // optional: 양방향 연관관계 유지 (선택)
            project.addUser(student);
            projectRepository.save(project);

        } else if (user instanceof ProfessorEntity) {// 교수인 경우: ManyToMany 관계로 주입
            ProfessorEntity professor = (ProfessorEntity) user;
            if (!project.getProfessors().contains(professor)) {
                project.addProfessor(professor);
                projectRepository.save(project);
            }
        }

        invitationRepository.save(invitation);
        return true;
    }


    // ✅ 초대 거절
    @Transactional
    public String rejectInvitation(Long invitationId) {
        InvitationEntity invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 없음"));

        invitation.setStatus("REJECTED");
        invitationRepository.save(invitation);
        return "초대를 거절했습니다.";
    }

    // ✅ 팀원 조회
    public List<TeamMemberDto> getProjectMembers(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        return project.getStudents().stream()
                .map(user -> new TeamMemberDto(
                        user.getUserId(),
                        user.getUserName(),
                        user.getUserEmail(),
                        user.getRole().name(),
                        user.getUserColor()
                ))
                .toList();
    }

    // 교수 조회
    public List<ProfessorResponseDto> getProfessorsByProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        return project.getProfessors().stream()
                .map(professor -> new ProfessorResponseDto(
                        professor.getUserId(),
                        professor.getUserName(),
                        professor.getUserEmail()
                ))
                .collect(Collectors.toList());
    }

    // 프로젝트 삭제
    @Transactional
    public void deleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }

    //프로젝트 나가기
    @Transactional
    public void leaveProject(Long userId, Long projectId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        if (!(user instanceof StudentEntity student)) {
            throw new RuntimeException("학생만 프로젝트를 나갈 수 있습니다.");
        }

        if (!project.equals(student.getMainProject())) {
            throw new RuntimeException("해당 프로젝트에 속해 있지 않습니다.");
        }

        student.setMainProject(null);
        student.setUserColor(null);
        userRepository.save(student);
    }
    public ProjectResponseDto getProjectById(Long projectId) { //무영수정
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        return new ProjectResponseDto(
                project.getProjectId(),
                project.getTitle(),
                project.getImageUrl()
        );
    }


    //유저 색상지정
    @Transactional
    public void updateUserColor(Long userId, String colorHex) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!(user instanceof StudentEntity student)) {
            throw new RuntimeException("학생만 색상을 지정할 수 있습니다.");
        }

        if (student.getMainProject() == null) {
            throw new RuntimeException("이 학생은 현재 프로젝트에 속해있지 않습니다.");
        }

        student.setUserColor(colorHex);
        userRepository.save(student);
    }

    //리더 변경 기능
    public boolean changeLeader(Long projectId, Long currentUserId, Long newLeaderId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        // 현재 유저가 팀장인지 검증
        if (!project.getLeader().getUserId().equals(currentUserId)) {
            throw new SecurityException("팀장만 팀장을 넘길 수 있습니다.");
        }

        // 새 팀장 유저 조회
        UserEntity newLeader = userRepository.findById(newLeaderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 프로젝트 팀원인지 검증
        boolean isMember = project.getStudents().stream()
                .anyMatch(u -> u.getUserId().equals(newLeaderId));

        if (!isMember) {
            throw new IllegalArgumentException("해당 유저는 팀원이 아닙니다.");
        }

        // 리더 변경
        project.setLeader(newLeader);
        projectRepository.save(project);
        return true;
    }
}

