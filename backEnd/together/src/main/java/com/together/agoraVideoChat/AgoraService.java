
package com.together.agoraVideoChat;

import io.agora.media.RtcTokenBuilder;
import io.agora.media.RtcTokenBuilder.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * Agora RTC 토큰 생성 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class AgoraService {

    @Value("${agora.app.id}")
    private String appId;

    @Value("${agora.app.certificate}")
    private String appCertificate;

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
                Role.Role_Publisher,
                timestamp
        );
        return token;
    }
}
