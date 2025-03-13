package com.together.project;

import com.together.project.Invitation.InvitationEntity;
import com.together.project.Invitation.InvitationRepository;
import com.together.project.Invitation.dto.InvitationResponseDto;
import com.together.project.ProjectDto.InviteResponseDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
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

    // 프로젝트 생성
    @Transactional
    public ProjectResponseDto createProject(String title, Date startDate, Date endDate) {
        ProjectEntity project = new ProjectEntity();
        project.setTitle(title);

        // ✅ startDate와 endDate가 null인 경우 그대로 저장
        project.setProjectStartDate(startDate);
        project.setProjectEndDate(endDate);

        ProjectEntity savedProject = projectRepository.save(project);

        return new ProjectResponseDto(
                savedProject.getProjectId(),
                savedProject.getTitle(),
                savedProject.getProjectStartDate(),
                savedProject.getProjectEndDate()
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

    // 팀원 초대 (이메일로 검색 후 추가)
    @Transactional
    public boolean inviteUserToProject(Long projectId, String email) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        Optional<UserEntity> userOpt = userRepository.findByUserEmail(email);

        if (projectOpt.isEmpty()) {
            throw new RuntimeException("해당 프로젝트를 찾을 수 없습니다.");
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("해당 사용자를 찾을 수 없습니다.");
        }

        ProjectEntity project = projectOpt.get();
        UserEntity user = userOpt.get();

        // 기존 초대 상태 확인
        Optional<InvitationEntity> existingInvitation = invitationRepository.findByProjectAndUser(project, user);

        if (existingInvitation.isPresent()) {
            InvitationEntity invitation = existingInvitation.get();

            if ("ACCEPTED".equals(invitation.getStatus())) {
                throw new RuntimeException("해당 사용자는 이미 프로젝트에 참여 중입니다.");
            }

            if ("REJECTED".equals(invitation.getStatus())) {
                invitation.setStatus("PENDING");
                invitationRepository.save(invitation);
                return true;
            }

            return false;
        }

        InvitationEntity invitation = new InvitationEntity();
        invitation.setProject(project);
        invitation.setUser(user);
        invitation.setStatus("PENDING");  // 초대 상태
        invitationRepository.save(invitation);

        return true;  // ✅ boolean 반환
    }

//초대 확인

    @Transactional
    public List<InvitationResponseDto> getUserInvitations(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("해당 사용자를 찾을 수 없습니다.");
        }

        List<InvitationEntity> invitations = invitationRepository.findByUser(userOpt.get());

        return invitations.stream()
                .map(invitation -> new InvitationResponseDto(
                        invitation.getId(),
                        invitation.getProject().getTitle(),
                        invitation.getUser().getUserName(),
                        invitation.getStatus()
                ))
                .toList();
    }

    //초대수락
    @Transactional
    public boolean acceptInvitation(Long invitationId) {
        InvitationEntity invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 정보를 찾을 수 없습니다."));

        invitation.setStatus("ACCEPTED");
        invitation.setAccepted(true);

        ProjectEntity project = invitation.getProject();
        UserEntity user = invitation.getUser();

        if (!project.getUsers().contains(user)) {
            project.addUser(user);
            projectRepository.save(project);
        }

        invitationRepository.save(invitation);

        return true;  // ✅ boolean 반환
    }


    //초대거절

    @Transactional
    public String rejectInvitation(Long invitationId) {
        InvitationEntity invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 정보를 찾을 수 없습니다."));

        invitation.setStatus("REJECTED");  // ❗ 초대 거절
        invitationRepository.save(invitation);

        return "초대가 거절되었습니다.";
    }
    // 프로젝트 팀원 목록 조회
    public List<UserEntity> getProjectMembers(Long projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectEntity::getUsers)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
    }

    // 프로젝트 삭제
    @Transactional
    public void deleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }
}

