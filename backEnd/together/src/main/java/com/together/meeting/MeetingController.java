package com.together.meeting;

import com.together.systemConfig.UserDetailsImpl;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/meeting")
public class MeetingController {

    private final MeetingService meetingService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    /**
     *   회의 생성 API (POST /meetings)
     * - 로그인한 사용자가 새로운 회의를 생성함.
     * - `meetingDto`에는 회의 제목, 내용, 날짜 정보가 포함됨.
     * - `userDetails`를 통해 현재 로그인한 사용자의 정보를 가져옴.
     *
     * @param meetingDto 회의 생성 요청 데이터
     * @param userDetails 현재 로그인한 사용자 정보 (Spring Security에서 자동 제공)
     * @return 생성된 회의 정보
     */
    @PostMapping("/create")
    public ResponseEntity<MeetingEntity> createMeeting(
            @RequestParam(required = false) Long projectId, //AOP 를 통해 교수일경우 불러오는값을 사용, 학생일 경우 자동 설정
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody MeetingDto meetingDto) {

        Long user = userDetails.getUser().getUserId();

        // 회의 생성
        MeetingEntity createdMeeting = meetingService.createMeeting(meetingDto, user, projectId);

        return ResponseEntity.ok(createdMeeting);
    }

    /**
     *   모든 회의 목록 조회 API (GET /meetings)
     * - 저장된 모든 회의를 불러옴.
     *
     * @return 모든 회의 목록
     * ---
     * 수정
     * 1. userName 을 출력하며 순환참조를 방지하기위해 MeetingResponseDto 로 convertToDto 후 출력
     * 2. 프론트에서 불러올때 projectId를 입력하게끔 수정 -> 해당 프로젝트에있는 회의만 출력됨
     */
    @GetMapping("/all-author")
    public ResponseEntity<List<MeetingResponseDto>> getAllMeetings(
            @RequestParam(required = false) Long projectId, //AOP 를 통해 교수일경우 불러오는값을 사용, 학생일 경우 자동 설정
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return ResponseEntity.ok(meetingService.getAllMeetings(projectId));
    }

    /**
     *   특정 회의 조회 API (GET /meetings/{meetingId})
     * - 특정 ID의 회의 정보를 조회함.
     *
     * @param meetingId 조회할 회의 ID (URL 경로 변수)
     * @return 해당 ID의 회의 정보
     * 수정
     * 1. userName 을 출력하며 순환참조를 방지하기위해 MeetingResponseDto 로 convertToDto 후 출력
     */
    @GetMapping("/author/{meetingId}")
    public ResponseEntity<MeetingResponseDto> getMeetingById(@PathVariable Long meetingId) {
        return ResponseEntity.ok(meetingService.getMeetingById(meetingId));
    }

    /**
     *   회의 수정 API (PUT /meetings/{meetingId})
     * - 로그인한 사용자가 특정 회의 정보를 수정할 수 있음.
     * - 회의 생성자만 수정 가능하도록 `userId` 검증 포함.
     *
     * @param meetingId 수정할 회의 ID
     * @param meetingDto 수정할 회의 정보 (제목, 내용, 날짜 등)
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 수정된 회의 정보
     */
    @PutMapping("/update/{meetingId}")
    public ResponseEntity<MeetingEntity> updateMeeting(
            @PathVariable Long meetingId,
            @RequestBody MeetingDto meetingDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 로그인한 사용자의 정보를 가져와 UserEntity 조회
        UserEntity user = userRepository.findByUserLoginId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 회의 수정
        MeetingEntity updatedMeeting = meetingService.updateMeeting(meetingId, meetingDto, user.getUserId());

        return ResponseEntity.ok(updatedMeeting);
    }

    /**
     *   회의 삭제 API (DELETE /meetings/{meetingId})
     * - 로그인한 사용자가 특정 회의를 삭제할 수 있음.
     * - 회의 생성자만 삭제 가능하도록 `userId` 검증 포함.
     *
     * @param meetingId 삭제할 회의 ID
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("/delete/{meetingId}")
    public ResponseEntity<String> deleteMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 로그인한 사용자의 정보를 가져와 UserEntity 조회
        UserEntity user = userRepository.findByUserLoginId(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 회의 삭제
        meetingService.deleteMeeting(meetingId, user.getUserId());

        return ResponseEntity.ok("회의가 삭제되었습니다.");
    }
}
