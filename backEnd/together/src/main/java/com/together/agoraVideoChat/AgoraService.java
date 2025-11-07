
package com.together.agoraVideoChat;

import io.agora.media.RtcTokenBuilder;
import io.agora.rtm.RtmTokenBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import static io.agora.media.RtcTokenBuilder.Role.Role_Publisher;


/**
 * Agora RTC 토큰 생성 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgoraService {

    // --- 화상채팅 키값 ---
    private final RestTemplate restTemplate;

    @Value("${agora.app.id}")
    private String appId;

    @Value("${agora.app.certificate}")
    private String appCertificate;

    // --- 참여자 목록 키값 ---
    @Value("${agora.api.customer-id}")
    private String customerId;

    @Value("${agora.api.customer-secret}")
    private String customerSecret;

    // 토큰의 유효 시간 (초)
    private static final int EXPIRATION_TIME_IN_SECONDS = 3600;


    /**
     * RTC 토큰을 생성합니다.
     *
     * @param channelName 화상 채팅이 이루어질 채널의 이름 (여기서는 projectId를 사용)
     * @param userId      채널에 참여하는 사용자의 고유 ID (Integer 타입)
     * @return 생성된 토큰 문자열
     */
    public String generateRtcToken(String channelName, Integer userId) {
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        // 현재 시간으로부터 1시간 뒤를 만료 시간으로 설정
        int timestamp = (int) (System.currentTimeMillis() / 1000 + EXPIRATION_TIME_IN_SECONDS);

        // 토큰 생성 (App ID, App Certificate, 채널명, 사용자 ID, 역할, 만료 시간)
        // Role_Publisher: 스트림을 발행할 수 있는 권한 (카메라, 마이크 사용 가능)
        // Role_Subscriber: 스트림을 구독만 할 수 있는 권한 (시청만 가능)
        String token = tokenBuilder.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                userId,         // Agora는 Integer 타입의 UID를 사용합니다.
                Role_Publisher,
                timestamp
        );
        return token;
    }

    /**
     * RTM (Real-Time Messaging) 토큰을 생성합니다.
     *
     * @param userId      채널에 참여하는 사용자의 고유 ID (RTM은 String 타입을 사용)
     * @return 생성된 RTM 토큰 문자열
     */
    public String generateRtmToken(String userId) {
        RtmTokenBuilder tokenBuilder = new RtmTokenBuilder();
        // 1시간 뒤 만료 시간 설정
        int timestamp = (int) (System.currentTimeMillis() / 1000 + EXPIRATION_TIME_IN_SECONDS);

        String token;
        try {
            // RTM 토큰 생성 (App ID, App Certificate, 사용자 ID(String), 만료 시간)
            // RTM 토큰은 채널명이 아닌, 사용자 ID를 기준으로 발급됩니다.
            token = tokenBuilder.buildToken(
                    appId,
                    appCertificate,
                    userId,
                    RtmTokenBuilder.Role.Rtm_User,
                    timestamp
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // RTM 토큰 생성 실패 시 런타임 예외 발생
            log.error("Agora RTM 토큰 생성 실패: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("RTM 토큰 생성에 실패했습니다.", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    /**
     * Agora REST API를 호출하여 채널의 참여자 목록을 가져옵니다.
     * @param channelName 조회할 채널 이름 (projectId)
     * @return Agora API의 응답 (JSON을 Map으로 임시 변환)
     */
    public Map<String, Object> getChannelParticipants(String channelName) {
        // 1. Agora REST API 엔드포인트 URL 구성
        // (주의: URL에 appId가 들어갑니다)
        String apiUrl = String.format("https://api.agora.io/dev/v1/channel/user/%s/%s", appId, channelName);

        // 2. Basic Authentication 헤더 생성
        String authCredentials = customerId + ":" + customerSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(authCredentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 3. REST API 호출
            log.info("Agora REST API 호출: {}", apiUrl);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class // 우선 간단히 Map으로 응답을 받습니다.
            );

            log.info("Agora API 응답 수신: {}", response.getBody());
            return response.getBody();

        } catch (Exception e) {
            log.error("Agora REST API 호출 실패: {}", e.getMessage());
            // 예외 처리를 적절히 추가해야 합니다 (예: 빈 Map 반환 또는 커스텀 예외)
            return Map.of("error", true, "message", e.getMessage());
        }
    }


}
