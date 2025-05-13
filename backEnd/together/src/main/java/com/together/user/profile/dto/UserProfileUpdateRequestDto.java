package com.together.user.profile.dto;

import com.together.user.UserEntity.ThemeOption;
import lombok.Data;

@Data
public class UserProfileUpdateRequestDto {
    private String userName;           // 이름
    private String bio;                // 자기소개
    private String profileImageUrl;    // 이미지 URL (Google Drive 링크)
    private ThemeOption theme;         // 테마 설정 (LIGHT or DARK)
}
