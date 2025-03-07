package com.together.project;

import com.together.user.UserEntity;
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
    public ResponseEntity<ProjectEntity> createProject(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");

            // 로그로 데이터를 출력하여 확인
            System.out.println("Title: " + title);
            System.out.println("Start Date: " + startDateStr);
            System.out.println("End Date: " + endDateStr);

            if (title == null || startDateStr == null || endDateStr == null) {
                return ResponseEntity.badRequest().body(null);
            }

            // String 형식을 Date로 변환
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // 수정된 날짜 변환
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            // 로그 출력
            System.out.println("Parsed Start Date: " + startDate);
            System.out.println("Parsed End Date: " + endDate);

            ProjectEntity project = projectService.createProject(title, startDate, endDate);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            // 예외가 발생하면 에러 메시지 출력
            System.err.println("Error creating project: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }



}
    // 이메일로 사용자 검색
    @GetMapping("/search")
    public ResponseEntity<List<UserEntity>> searchUser(@RequestParam String email) {
        List<UserEntity> users = projectService.searchUserByEmail(email);  // 서비스 호출
        return ResponseEntity.ok(users);
    }

    // 팀원 초대
    @PostMapping("/{projectId}/invite")
    public ResponseEntity<String> inviteUser(@PathVariable Long projectId, @RequestParam String email) {
        boolean success = projectService.inviteUserToProject(projectId, email);
        if (success) {
            return ResponseEntity.ok("팀원이 성공적으로 초대되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("팀원 초대 실패: 사용자를 찾을 수 없거나 이미 프로젝트에 추가됨.");
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
