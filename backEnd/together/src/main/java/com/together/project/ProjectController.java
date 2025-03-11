package com.together.project;

import com.together.project.ProjectDto.InviteResponseDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.user.UserEntity;
import com.together.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 프로젝트 생성
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");

            if (title == null) {
                return ResponseEntity.badRequest().body(new ProjectResponseDto(null, null, null, null));
            }

            // 날짜 파싱 로직 개선
            Date startDate = null;
            Date endDate = null;

            if (startDateStr != null && !startDateStr.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                startDate = dateFormat.parse(startDateStr);
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                endDate = dateFormat.parse(endDateStr);
            }

            ProjectResponseDto project = projectService.createProject(title, startDate, endDate);

            return ResponseEntity.ok(project);
        } catch (Exception e) {
            System.err.println("Error creating project: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    // 이메일로 사용자 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchUser(@RequestParam String email) {
        try {
            List<UserResponseDto> users = projectService.searchUserByEmail(email);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // 팀원 초대
    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteUser(@PathVariable Long projectId, @RequestParam String email) {
        try {
            InviteResponseDto response = projectService.inviteUserToProject(projectId, email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    // 프로젝트 팀원 목록 조회
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<UserEntity>> getProjectMembers(@PathVariable Long projectId) {
        List<UserEntity> members = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(members);
    }

    // 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok("프로젝트가 삭제되었습니다.");
    }
}
