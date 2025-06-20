package com.together.user.profile;

import com.together.ImagePreview.ImgurUploadService;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.UserService;
import com.together.user.profile.dto.UserProfileResponseDto;
import com.together.user.profile.dto.UserProfileUpdateRequestDto;
import com.together.documentManger.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 프로필 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class profileController {

    private final ImgurUploadService imgurUploadService;
    private final profileService profileService;
    private final GoogleDriveService googleDriveService;

    /**
     * 프로필 수정 API (이름, 소개글, 이미지 URL, 테마)
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody UserProfileUpdateRequestDto requestDto) {

        profileService.updateUserProfile(userDetails.getUser().getUserId(), requestDto);
        return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.");
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserProfileResponseDto profile =  profileService.getUserProfile(userDetails.getUser().getUserId());
        return ResponseEntity.ok(profile);
    }

    /**
     * 사용자 프로필 이미지를 업로드하고 URL 저장
     */
    @PutMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart("image") MultipartFile imageFile) {

        try {
            // 1. Imgur(또는 S3/Firebase 등)에 이미지 업로드 → 퍼블릭 URL 반환
            String imageUrl = imgurUploadService.uploadImage(imageFile);

            // 2. profileService에서 URL로 DB에 저장
            profileService.updateUserProfileImage(userDetails.getUser().getUserId(), imageUrl);

            // 3. 응답도 URL 그대로 반환
            return ResponseEntity.ok(imageUrl); // ⭐️ 퍼블릭 URL 반환!
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패", e);
            return ResponseEntity.status(500).body("이미지 업로드 실패: " + e.getMessage());
        }
    }

    //이미지삭제
    @DeleteMapping("/profile/image")
    public ResponseEntity<String> deleteProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            profileService.deleteUserProfileImage(userDetails.getUser().getUserId());
            return ResponseEntity.ok("프로필 이미지가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이미지 삭제 실패: " + e.getMessage());
        }
    }
}
