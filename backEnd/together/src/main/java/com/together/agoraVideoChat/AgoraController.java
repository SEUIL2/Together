package com.together.agoraVideoChat;

import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * Agora 화상 채팅 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/agora") // API 경로를 /api/agora로 설정
@RequiredArgsConstructor      // final 필드에 대한 생성자를 자동으로 생성
public class AgoraController {

    private final AgoraService agoraService;


    /**
     * 화상 채팅방 입장을 위한 토큰을 발급하는 API
     * @param projectId 현재 프로젝트 ID (@CurrentProject 어노테이션으로 자동 주입)
     * @param userDetails 현재 로그인된 사용자 정보 (@AuthenticationPrincipal 어노테이션으로 주입)
     * @return 생성된 토큰이 담긴 응답 객체
     */
    @GetMapping("/token")
    public ResponseEntity<AgoraTokenResponse> generateToken(
            @CurrentProject Long projectId, // 요청 경로의 projectId를 자동으로 가져옵니다.
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 화상 채팅 채널 이름으로 projectId를 사용 (문자열로 변환)
        String channelName = String.valueOf(projectId);

        // 현재 로그인한 사용자의 ID (Long)를 Agora에서 요구하는 UID (Integer) 타입으로 변환
        Long user = userDetails.getUser().getUserId();
        Integer userId = Math.toIntExact(user);

        log.info("화상채팅 토큰 발급 요청: projectId={}, userId={}", projectId, userId);

        // AgoraService를 통해 토큰 생성
        String token = agoraService.generateRtcToken(channelName, userId);

        // 생성된 토큰을 DTO에 담아 성공(200 OK) 응답 반환
        return ResponseEntity.ok(new AgoraTokenResponse(token, userId));
    }

    /**
     * 채팅(RTM)을 위한 RTM 토큰을 발급하는 API
     * @param userDetails 현재 로그인된 사용자 정보 (@AuthenticationPrincipal 어노테이션으로 주입)
     * @return 생성된 RTM 토큰과 RTM용 사용자 ID(String)
     */
    @GetMapping("/rtm-token")
    public ResponseEntity<Map<String, String>> generateRtmToken(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // RTM SDK는 사용자 ID로 Integer가 아닌 String을 요구합니다.
        String userId = String.valueOf(userDetails.getUser().getUserId());

        log.info("RTM 토큰 발급 요청: userId={}", userId);

        // AgoraService를 통해 RTM 토큰 생성
        String token = agoraService.generateRtmToken(userId);

        // 프론트에서 RTM 로그인 시 String 타입의 userId도 필요하므로 함께 반환합니다.
        return ResponseEntity.ok(Map.of("token", token, "userId", userId));
    }

    /**
     * 현재 화상 채팅방(채널)의 참여자 목록을 조회하는 API
     * @param projectId 현재 프로젝트 ID (@CurrentProject 어노테이션으로 자동 주입)
     * @return 채널 참여자 정보가 담긴 응답 객체
     * ---응답 예시---
     *{
     *     "success": true,
     *     "data": {
     *         "channel_exist": true, //접속을 성공했음
     *         "mode": 1,
     *         "total": 1, //총 1명 접속상태
     *         "users": [ 2 ] //접속한 유저의 아이디는 2이다
     *     }
     * }
     */
    @GetMapping("/participants")
    public ResponseEntity<Map<String, Object>> getParticipants(
            @CurrentProject Long projectId // @CurrentProject로 현재 프로젝트 ID를 안전하게 받음 (규칙 4)
    ) {
        String channelName = String.valueOf(projectId);
        log.info("참여자 목록 조회 요청: projectId={}", projectId);

        // AgoraService를 통해 참여자 목록 조회
        Map<String, Object> participantsData = agoraService.getChannelParticipants(channelName);

        // 조회된 데이터를 그대로 반환
        return ResponseEntity.ok(participantsData);
    }
}
