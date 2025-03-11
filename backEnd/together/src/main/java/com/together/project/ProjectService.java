package com.together.project;

import com.together.project.Invitation.InvitationEntity;
import com.together.project.Invitation.InvitationRepository;
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
    public boolean inviteUserToProject(Long projectId, String userEmail) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        Optional<UserEntity> userOpt = userRepository.findByUserEmail(userEmail);

        if (projectOpt.isPresent() && userOpt.isPresent()) {
            ProjectEntity project = projectOpt.get();
            UserEntity user = userOpt.get();

            // 이미 초대된 경우 중복 저장 방지
            Optional<InvitationEntity> existingInvitation = invitationRepository.findByProjectAndUser(project, user);
            if (existingInvitation.isPresent()) {
                return false; // 이미 초대됨
            }

            // 초대 요청 저장
            InvitationEntity invitation = new InvitationEntity();
            invitation.setProject(project);
            invitation.setUser(user);
            invitationRepository.save(invitation);

            return true; // 초대 요청 완료
        }
        return false;
    }
//초대수락
    public boolean acceptInvitation(Long invitationId) {
        Optional<InvitationEntity> invitationOpt = invitationRepository.findById(invitationId);

        if (invitationOpt.isPresent()) {
            InvitationEntity invitation = invitationOpt.get();
            if (!invitation.isAccepted()) {
                invitation.acceptInvitation();
                invitationRepository.save(invitation);

                // 실제 프로젝트에 추가
                ProjectEntity project = invitation.getProject();
                UserEntity user = invitation.getUser();
                project.addUser(user);
                projectRepository.save(project);
                return true;
            }
        }
        return false;
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

