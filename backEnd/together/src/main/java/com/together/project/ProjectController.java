package com.together.project;

import com.together.documentManger.GoogleDriveService;
import com.together.project.Invitation.InvitationRepository;
import com.together.project.Invitation.dto.InvitationResponseDto;
import com.together.project.Invitation.dto.TeamMemberDto;
import com.together.project.ProjectDto.ProjectMembersDto;
import com.together.project.ProjectDto.ProjectResponseDto;
import com.together.project.ProjectDto.ProjectSummaryWithMembersDto;
import com.together.project.ProjectDto.ProjectTitleUpdateRequestDto;
import com.together.user.professor.ProfessorResponseDto;
import com.together.user.student.StudentEntity;
import com.together.util.customAnnotation.CurrentProject;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.UserRepository;
import com.together.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.together.documentManger.GoogleDriveService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final GoogleDriveService googleDriveService;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;


    /**
     * 이미지 없이 프로젝트를 생성하는 API (title만 JSON으로 받음)
     */
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            ProjectResponseDto project = projectService.createProject(title);  // ✅ imageUrl 없이
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            log.error("프로젝트 생성 실패", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    //프로젝트 생성 이미지업로드
    @PostMapping(value = "/create-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectResponseDto> createProjectWithImage(
            @RequestParam("title") String title,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = googleDriveService.uploadImageToGoogleDrive(imageFile);
            }

            ProjectResponseDto project = projectService.createProject(title, imageUrl);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            log.error("프로젝트 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PutMapping("/image")
    public ResponseEntity<String> updateProjectImage(
            @CurrentProject(required = false) Long projectId,
            @RequestPart("image") MultipartFile imageFile) {

        if (projectId == null) {
            return ResponseEntity.badRequest().body("프로젝트 ID가 없습니다.");
        }

        try {
            String imageUrl = googleDriveService.uploadImageToGoogleDrive(imageFile);
            projectService.updateProjectImage(projectId, imageUrl);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이미지 업로드 실패");
        }
    }

    /**
     * 현재 로그인한 사용자의 프로젝트에서 이미지 삭제
     * - 프로젝트의 imageUrl을 null로 초기화
     * - Google Drive에서도 실제 파일 삭제
     */
    @DeleteMapping("/image")
    public ResponseEntity<String> deleteProjectImage(
            @CurrentProject(required = false) Long projectId
    ) {
        if (projectId == null) {
            return ResponseEntity.badRequest().body("프로젝트 ID가 없습니다.");
        }

        try {
            projectService.deleteProjectImage(projectId);
            return ResponseEntity.ok("이미지 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이미지 삭제 실패: " + e.getMessage());
        }
    }
    //프로젝트 불러오기
    @GetMapping("/my")
    public ResponseEntity<ProjectResponseDto> getMyProject(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (projectId == null) {
            log.info("getMyProject: projectId가 Null 이다 ");
            return ResponseEntity.status(400).body(null);
        }

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다.")); // 유저 타입에 따라 프로젝트 가져오기

        return ResponseEntity.ok(new ProjectResponseDto(
                project.getProjectId(),
                project.getTitle(),
                project.getImageUrl()
        ));
    }


    /**
     * [GET] 교수의 프로젝트 목록을 생성일 기준으로 정렬하여 반환
     * 팀원 정보(학생 + 교수)까지 포함
     */
    // ✅ [교수 전용] 내가 만든 프로젝트를 생성일 기준으로 최신순 조회
    @GetMapping("/my-projects/sorted-by-created")
    public ResponseEntity<List<ProjectSummaryWithMembersDto>> getMyProjectsSortedWithMembers(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<ProjectSummaryWithMembersDto> projects =
                projectService.getProfessorProjectsSorted(userDetails.getUser().getUserId());

        return ResponseEntity.ok(projects);
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
            @CurrentProject(required = false) Long projectId, //커스템 어노테이션 사용
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String email
    ) {
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
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<InvitationResponseDto> responses = projectService.getUserInvitations(userDetails.getUser().getUserId());
        return ResponseEntity.ok(responses);
    }

    //초대수락
    @PostMapping("/invite/{invitationId}/accept")
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
    // 학생이 학생 조회 가능 , 교수가 학생 조회가능 , 학생이 교수 조회기능은 getProfessorsByProject() 에서 해야됌
    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberDto>> getProjectMembers(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<TeamMemberDto> members = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(members);
    }

    // 해당 팀의 교수 조회
    @GetMapping("/members/{projectId}")
    public ResponseEntity<List<ProfessorResponseDto>> getProfessorsByProject(
            @PathVariable Long projectId) {
        List<ProfessorResponseDto> professors = projectService.getProfessorsByProject(projectId);
        return ResponseEntity.ok(professors);
    }

    //프로젝트 나가기
    @DeleteMapping("/leave")
    public ResponseEntity<String> leaveProject(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (projectId == null) {
            return ResponseEntity.badRequest().body("프로젝트 ID가 없습니다.");
        }

        projectService.leaveProject(userDetails.getUser().getUserId(), projectId);
        return ResponseEntity.ok("프로젝트에서 성공적으로 나갔습니다.");
    }
    //교수 프로젝트 나가기
    @DeleteMapping("/{projectId}/leave")
    public ResponseEntity<String> leaveProjectAsProfessor(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            projectService.professorLeaveProject(userDetails.getUser().getUserId(), projectId);
            return ResponseEntity.ok("프로젝트에서 성공적으로 나갔습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("나가기 실패: " + e.getMessage());
        }
    }

    // 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok("프로젝트가 삭제되었습니다.");
    }

    //색상지정
    @PutMapping("/members/{userId}/color")
    public ResponseEntity<String> updateUserColor(
            @PathVariable Long userId,
            @RequestParam String colorHex) {

        projectService.updateUserColor(userId, colorHex);
        return ResponseEntity.ok("색상 업데이트 완료");
    }
    @GetMapping("/{projectId}") //무영 수정
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long projectId) {
        ProjectResponseDto project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }


    /**
     * 변경할 유저의 아이디값을 넣으면 됌
    {
        "newLeaderId": 1
    }
     */
    @PutMapping("/change-leader")
    public ResponseEntity<?> changeLeader(
            @CurrentProject Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Long> body
    ) {
        Long currentUserId = userDetails.getUser().getUserId();
        Long newLeaderId = body.get("newLeaderId");

        try {
            projectService.changeLeader(projectId, currentUserId, newLeaderId);
            return ResponseEntity.ok("팀장이 변경되었습니다.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //팀원 역할 확인 메서드
    @GetMapping("/members/role")
    public ResponseEntity<List<ProjectMembersDto>> getProjectMembers(
            @CurrentProject Long projectId
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트가 존재하지 않습니다."));

        Long leaderId = project.getLeader() != null ? project.getLeader().getUserId() : null;

        // 학생 리스트 변환
        List<ProjectMembersDto> studentDtos = project.getStudents().stream()
                .map(user -> {
                    String studentNumber = null;
                    if (user instanceof StudentEntity) {
                        studentNumber = user.getStudentNumber();
                    }

                    return new ProjectMembersDto(
                            user.getUserId(),
                            user.getUserName(),
                            studentNumber,
                            user.getRole().name(),
                            user.getUserId().equals(leaderId) // ✅ isLeader 체크
                    );
                })
                .toList();

        // 교수 리스트 변환
        List<ProjectMembersDto> professorDtos = project.getProfessors().stream()
                .map(professor -> new ProjectMembersDto(
                        professor.getUserId(),
                        professor.getUserName(),
                        null, // 교수는 학번 없음
                        professor.getRole().name(),
                        professor.getUserId().equals(leaderId) // ✅ isLeader 체크

                ))
                .toList();

        // 학생 + 교수 통합
        List<ProjectMembersDto> allMembers = Stream.concat(studentDtos.stream(), professorDtos.stream())
                .toList();

        return ResponseEntity.ok(allMembers);
    }

}
