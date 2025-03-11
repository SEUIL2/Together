package com.together.project;

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

    // 프로젝트 생성
    @Transactional
    public ProjectResponseDto createProject(String title, Date startDate, Date endDate) {
        ProjectEntity project = new ProjectEntity();
        project.setTitle(title);

        // ✅ startDate와 endDate가 null인 경우 그대로 null 값 저장
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
    public InviteResponseDto inviteUserToProject(Long projectId, String userEmail) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        Optional<UserEntity> userOpt = userRepository.findByUserEmail(userEmail);

        if (projectOpt.isEmpty()) {
            throw new RuntimeException("해당 프로젝트를 찾을 수 없습니다.");
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("해당 이메일을 가진 사용자를 찾을 수 없습니다.");
        }

        ProjectEntity project = projectOpt.get();
        UserEntity user = userOpt.get();

        // 이미 프로젝트에 속한지 확인
        if (project.getUsers().contains(user)) {
            throw new RuntimeException("해당 사용자는 이미 프로젝트에 참여 중입니다.");
        }

        // 팀원 추가
        project.addUser(user);
        projectRepository.save(project);

        return new InviteResponseDto(
                project.getProjectId(),
                user.getUserEmail(),
                "팀원이 성공적으로 초대되었습니다."
        );
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

