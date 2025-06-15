package com.together.user.profile;

import com.together.documentManger.GoogleDriveService;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.profile.dto.UserProfileUpdateRequestDto;
import com.together.user.profile.dto.UserProfileResponseDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 사용자 프로필 관련 서비스 로직
 */
@Service
@RequiredArgsConstructor
public class profileService {

    private final UserRepository userRepository;
    private final GoogleDriveService googleDriveService;

    /**
     * 사용자 프로필 수정
     * - 이름, 자기소개, 프로필 이미지 URL, 테마 설정 포함
     */
    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequestDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (dto.getUserName() != null) {
            user.setUserName(dto.getUserName());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(dto.getProfileImageUrl());
        }
        if (dto.getTheme() != null) {
            user.setTheme(dto.getTheme());
        }

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
                user.getUserId(),
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

        // ⭐️ 구글 드라이브 fileId 추출 → 미리보기 URL로 변환해서 저장!
        String fileId = googleDriveService.extractDriveFileId(imageUrl);
        String previewUrl = "https://drive.google.com/uc?id=" + fileId;

        user.setProfileImageUrl(previewUrl);
        userRepository.save(user);
    }


    //이미지 삭제

    @Transactional
    public void deleteUserProfileImage(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String imageUrl = user.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fileId = googleDriveService.extractDriveFileId(imageUrl);

            try {
                googleDriveService.deleteFile(fileId);  // ✅ IOException 처리
            } catch (IOException e) {
                throw new RuntimeException("Google Drive 이미지 삭제 실패", e);
            }
        }

        user.setProfileImageUrl(null);
        userRepository.save(user);
    }
}

