
package com.together.agoraVideoChat;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Agora 토큰 요청에 대한 응답 DTO
 */
@Getter
@AllArgsConstructor
public class AgoraTokenResponse {
    // 생성된 토큰 문자열
    private String token;
    private Integer userId;
}
