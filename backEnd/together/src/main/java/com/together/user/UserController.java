package com.together.user;

import com.together.systemConfig.UserDetailsImpl;
import com.together.user.dto.UserSignUpRequestDto;
import com.together.user.email.EmailService;
import com.together.user.email.VerificationCodeService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;

    /**
     *  로그인 (Spring Security에서 자동 처리됨)
     *
     *  예시 :
     *  {
     *      "userLoginId": "chulsu123",
     *      "password": "password123"
     *  }
     *
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String userLoginId) {
        UserEntity user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이메일 인증 여부 확인
        if (!user.isEmailVerified()) {
            // 이메일 인증되지 않은 경우 로그인 실패 처리
            return ResponseEntity.status(403).body("이메일 인증이 완료되지 않았습니다.");
        }

        log.info("login 성공");
        return ResponseEntity.ok("로그인이 성공적으로 처리되었습니다.");
    }

    /**
     * 세션과 쿠키를 제거하고 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignUpRequestDto requestDto) {
        // 아이디 중복 확인
        if (userService.findUserByUsername(requestDto.getUserLoginId()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        // 이메일 중복 확인
        if (userService.findUserByEmail(requestDto.getUserEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 등록된 이메일입니다.");
        }

        /*// 이메일 인증 확인
        if (!emailService.isEmailVerified(requestDto.getUserEmail())) {
            log.info("이메일 인증이 완료되지 않았습니다.");
            return ResponseEntity.badRequest().body("이메일 인증이 완료되지 않았습니다.");
        }*/

        // 유효성 검사: STUDENT인 경우 학번 필수
        if ("STUDENT".equalsIgnoreCase(requestDto.getUserRole()) &&
                (requestDto.getStudentNumber() == null || requestDto.getStudentNumber().isEmpty())) {
            return ResponseEntity.badRequest().body("STUDENT의 경우 학번을 입력해야 합니다.");
        }

        // 회원 등록
        userService.registerUser(requestDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    /** 현재 사용자 정보 추출
     * 예시
     * {
     *     "roles": [
     *         {
     *             "authority": "ROLE_STUDENT"
     *         }
     *     ],
     *     "username": "test01"
     * }
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }
        log.info("현재 사용자: {}", userDetails.getUsername());
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("roles", userDetails.getAuthorities());

        return ResponseEntity.ok(userInfo);
    }

    //회원가입할때 로그인아이디 중복여부 확인 : true=이미 존재함 , false = 사용가능
    @GetMapping("check-id")
    public ResponseEntity<Boolean> checkLoginIdDuplicate(@RequestParam String loginId) {
        boolean isDuplicate = userService.isLoginIdDuplicated(loginId);
        return ResponseEntity.ok(isDuplicate);
    }

    // 아이디 찾기 요청 (이메일로 인증 코드 전송)
    @PostMapping("/find-id")
    public ResponseEntity<String> findUserId(@RequestParam String email) throws MessagingException {
        Optional<UserEntity> user = userRepository.findByUserEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("해당 이메일로 가입된 계정이 없습니다.");
        }

        String code = emailService.generateVerificationCode();
        verificationCodeService.saveVerificationCode(email, code);
        emailService.sendVerificationEmail(email, code);
        return ResponseEntity.ok("이메일로 인증 코드가 전송되었습니다.");
    }

    // 인증 코드 확인 후 아이디 반환
    @PostMapping("/find-id/verify")
    public ResponseEntity<String> verifyAndReturnUserId(@RequestParam String email, @RequestParam String code) {
        if (!verificationCodeService.verifyCode(email, code)) {
            return ResponseEntity.badRequest().body("인증 코드가 올바르지 않습니다.");
        }

        UserEntity user = userRepository.findByUserEmail(email).orElseThrow();
        return ResponseEntity.ok("회원님의 아이디는 " + user.getUserLoginId() + " 입니다.");
    }

    // 비밀번호 찾기 요청 (이메일로 인증 코드 전송)
    @PostMapping("/find-password")
    public ResponseEntity<String> findUserPassword(@RequestParam String email) throws MessagingException {
        Optional<UserEntity> user = userRepository.findByUserEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("해당 이메일로 가입된 계정이 없습니다.");
        }

        String code = emailService.generateVerificationCode();
        verificationCodeService.saveVerificationCode(email, code);
        emailService.sendVerificationEmail(email, code);
        return ResponseEntity.ok("이메일로 인증 코드가 전송되었습니다.");
    }

    // 인증 코드 확인 후 비밀번호 변경
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String code, @RequestParam String newPassword) {
        if (!verificationCodeService.verifyCode(email, code)) {
            return ResponseEntity.badRequest().body("인증 코드가 올바르지 않습니다.");
        }

        UserEntity user = userRepository.findByUserEmail(email).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
