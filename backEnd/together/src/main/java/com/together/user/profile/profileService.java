package com.together.user.profile;

import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.profile.dto.UserProfileUpdateRequestDto;
import com.together.user.profile.dto.UserProfileResponseDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 사용자 프로필 관련 서비스 로직
 */
@Service
@RequiredArgsConstructor
public class profileService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 수정
     * - 이름, 자기소개, 프로필 이미지 URL, 테마 설정 포함
     */
    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequestDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setUserName(dto.getUserName());
        user.setBio(dto.getBio());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setTheme(dto.getTheme());

        userRepository.save(user);
    }

    /**
     * 사용자 프로필 조회
     * - 현재 로그인된 사용자 기준
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return new UserProfileResponseDto(
                user.getUserName(),
                user.getUserEmail(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getTheme()
        );
    }

    /**
     * 사용자 프로필 이미지 수정
     * - Google Drive에서 업로드된 이미지 URL을 저장
     */
    @Transactional
    public void updateUserProfileImage(Long userId, String imageUrl) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
    }
}

