package com.together.project;

import com.together.project.Invitation.InvitationEntity;
import com.together.project.Invitation.InvitationRepository;
import com.together.project.Invitation.dto.InvitationResponseDto;
import com.together.project.Invitation.dto.TeamMemberDto;
import com.together.project.ProjectDto.InviteResponseDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.project.ProjectDto.ProjectTitleUpdateRequestDto;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    //프로젝트 생성
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            ProjectResponseDto project = projectService.createProject(title);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            System.err.println("Error creating project: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    //프로젝트 불러오기
    @GetMapping("/my")
    public ResponseEntity<ProjectResponseDto> getMyProject() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId = auth.getName();

        UserEntity user = userRepository.findByUserLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ProjectEntity project = user.getProject(); // 유저가 속한 프로젝트 하나
        if (project == null) {
            return ResponseEntity.status(404).body(null); // 프로젝트 없음
        }

        return ResponseEntity.ok(new ProjectResponseDto(
                project.getProjectId(),
                project.getTitle()
        ));
    }

    //프로젝트 제목 수정
    @PutMapping("/{projectId}/update-title")
    public ResponseEntity<ProjectResponseDto> updateProjectTitle(
            @PathVariable Long projectId,
            @RequestBody ProjectTitleUpdateRequestDto requestDto) {

        try {
            ProjectResponseDto updatedProject = projectService.updateProjectTitle(projectId, requestDto.getNewTitle());
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
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

    // ✅ 팀원 초대 API
    @PostMapping("/invite")
    public ResponseEntity<String> inviteUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String email
    ) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        boolean success = projectService.inviteUserToProject(projectId, email);

        if (success) {
            return ResponseEntity.ok("초대 요청을 보냈습니다.");
        } else {
            return ResponseEntity.badRequest().body("이미 초대 중이거나 실패했습니다.");
        }
    }
        //초대확인
        @GetMapping("/invitations")
        public ResponseEntity<List<InvitationResponseDto>> getMyInvitations(
                @AuthenticationPrincipal UserDetailsImpl userDetails
        ) {
            List<InvitationResponseDto> responses = projectService.getUserInvitations(userDetails.getUser().getUserId());
            return ResponseEntity.ok(responses);
        }
        //초대수락
    @PostMapping("/invite/{invitationId}/accept/")
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

    // ✅ 프로젝트 팀원 조회
    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberDto>> getProjectMembers(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long projectId = userDetails.getUser().getProject().getProjectId();
        List<TeamMemberDto> members = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(members);
    }

    // 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok("프로젝트가 삭제되었습니다.");
    }

}
