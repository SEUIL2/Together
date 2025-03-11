package com.together.project;

import com.together.project.Invitation.InvitationEntity;
import com.together.project.Invitation.InvitationRepository;
import com.together.project.ProjectDto.InviteResponseDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    // 프로젝트 생성
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");

            System.out.println("Title: " + title);
            System.out.println("Start Date: " + startDateStr);
            System.out.println("End Date: " + endDateStr);

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = (startDateStr != null && !startDateStr.isEmpty()) ? dateFormat.parse(startDateStr) : null;
            Date endDate = (endDateStr != null && !endDateStr.isEmpty()) ? dateFormat.parse(endDateStr) : null;

            ProjectResponseDto project = projectService.createProject(title, startDate, endDate);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            System.err.println("Error creating project: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    // 이메일로 사용자 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchAndInviteUser(
            @RequestParam String email,
            @RequestParam(required = false) Long projectId) {
        try {
            List<UserResponseDto> users = projectService.searchUserByEmail(email);

            // 초대 기능 추가 (선택적)
            if (projectId != null && !users.isEmpty()) {
                projectService.inviteUserToProject(projectId, users.get(0).getUserEmail());
                return ResponseEntity.ok("사용자를 찾았으며, 초대 요청이 전송되었습니다.");
            }

            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // 팀원 초대
    @PostMapping("/{projectId}/invite")
    public ResponseEntity<String> inviteUser(@PathVariable Long projectId, @RequestParam String email) {
        try {
            boolean success = projectService.inviteUserToProject(projectId, email);

            if (success) {
                return ResponseEntity.ok("팀원이 성공적으로 초대되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("초대에 실패했습니다.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
        //초대확인
    @GetMapping("/invitations/{userId}")
    public ResponseEntity<List<InvitationEntity>> getUserInvitations(@PathVariable Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        List<InvitationEntity> invitations = invitationRepository.findByUser(userOpt.get());

        return ResponseEntity.ok(invitations);
    }
        //초대수락
    @PostMapping("/invite/accept/{invitationId}")
    public ResponseEntity<String> acceptInvitation(@PathVariable Long invitationId) {
        boolean success = projectService.acceptInvitation(invitationId);
        if (success) {
            return ResponseEntity.ok("초대를 수락하였습니다.");
        } else {
            return ResponseEntity.badRequest().body("초대 수락 실패: 초대 정보를 찾을 수 없습니다.");
        }
    }

    // 초대 거절 API
    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<String> rejectInvitation(@PathVariable Long invitationId) {
        try {
            String response = projectService.rejectInvitation(invitationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
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
