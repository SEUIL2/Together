package com.together.project;

import com.together.user.UserEntity;
import com.together.user.UserRepository;
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
    public ProjectEntity createProject(String title, Date startDate, Date endDate) {
        ProjectEntity project = new ProjectEntity();
        project.setTitle(title);
        project.setProjectStartDate(startDate);
        project.setProjectEndDate(endDate);

        // 프로젝트 저장
        ProjectEntity savedProject = projectRepository.save(project);

        // 저장된 프로젝트가 null이면 오류 발생
        if (savedProject == null) {
            throw new RuntimeException("Failed to save project");
        }




        return savedProject;
    }

    // 사용자 검색
    public List<UserEntity> searchUserByEmail(String email) {
        return userRepository.findByUserEmailContaining(email);  // 이메일로 사용자 검색
    }


    // 팀원 초대 (이메일로 검색 후 추가)
    @Transactional
    public boolean inviteUserToProject(Long projectId, String userEmail) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        Optional<UserEntity> userOpt = userRepository.findByUserEmail(userEmail);

        if (projectOpt.isPresent() && userOpt.isPresent()) {
            ProjectEntity project = projectOpt.get();
            UserEntity user = userOpt.get();

            // 이미 프로젝트에 속해있는지 확인
            if (!project.getUsers().contains(user)) {
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

