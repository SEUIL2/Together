package com.together.user.email;

import com.together.user.UserEntity;
import com.together.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;

    // 계정 생성 시 이메일 인증
    // 인증 코드 요청 API
    @PostMapping("/email/send")
    public ResponseEntity<String> sendVerificationEmail(@RequestParam String email) throws MessagingException {
        String code = emailService.generateVerificationCode();
        verificationCodeService.saveVerificationCode(email, code);
        emailService.sendVerificationEmail(email, code);
        return ResponseEntity.ok("이메일 인증 코드가 전송되었습니다.");
    }

    /**
     * 인증 코드 검증 API
     * @param email
     * @param code
     * @return
     *
     * 프론트에서 회원가입중 이메일 검증 시스템이 활성화되어야 하고
     * 이메일 인증이 완료되었을시 해당 회원가입 페이지를 벗어나기 전까지 회원가입시 입력되는 정보의 emailVerified 를 true로 설정
     */
    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String code) {
        //이메일 인증 성공했을 시 현재 이메일 검증 상태 변경 (UserEntity.emailVerified)
        if (verificationCodeService.verifyCode(email, code)) {
            UserEntity user = userRepository.findByUserEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            user.setEmailVerified(true); // 이메일 인증 상태를 true로 설정
            userRepository.save(user); // UserEntity 저장

            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        }
        return ResponseEntity.badRequest().body("인증 코드가 올바르지 않습니다.");
    }

}
