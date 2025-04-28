package com.together.project;

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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final NotificationService notificationService;

    // 프로젝트 생성
    @Transactional
    public ProjectResponseDto createProject(String title) {
        // 1️⃣ 현재 로그인한 사용자 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();

        UserEntity user = userRepository.findByUserLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));



        // 2️⃣ 프로젝트 생성 및 유저 연결
        ProjectEntity project = new ProjectEntity();
        project.setTitle(title);
        project.addUser(user); // ✅ 자동 연결

        // 3️⃣ 저장
        ProjectEntity savedProject = projectRepository.save(project);

        // 4️⃣ 응답 반환
        return new ProjectResponseDto(
                savedProject.getProjectId(),
                savedProject.getTitle()
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
                updatedProject.getTitle()
        );
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
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트 없음"));
        UserEntity user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 중복 초대 체크
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

        // 새로운 초대
        InvitationEntity invitation = new InvitationEntity();
        invitation.setProject(project);
        invitation.setUser(user);
        invitation.setStatus("PENDING");
        invitationRepository.save(invitation);

        // 알림 전송
        String message = String.format("%s님이 사용자를 %s 프로젝트에 초대하였습니다.",
                project.getUsers().get(0).getUserName(), project.getTitle());
        notificationService.sendNotification(user.getUserId(), message, "/projects");

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

        // 프로젝트에 사용자 추가
        if (!project.getUsers().contains(user)) {
            project.addUser(user);
            projectRepository.save(project);
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

        return project.getUsers().stream()
                .map(user -> new TeamMemberDto(
                        user.getUserId(),
                        user.getUserName(),
                        user.getUserEmail(),
                        user.getRole().name()
                ))
                .toList();
    }
    // 프로젝트 삭제
    @Transactional
    public void deleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }
}

