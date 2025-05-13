package com.together.user.profile.dto;

import com.together.user.UserEntity.ThemeOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 조회 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {
    private String userName;         // 이름
    private String userEmail;        // 이메일 (변경 불가)
    private String bio;              // 자기소개
    private String profileImageUrl;  // 프로필 이미지
    private ThemeOption theme;       // 테마 (LIGHT / DARK)
}
