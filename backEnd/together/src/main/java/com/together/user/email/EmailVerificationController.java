package com.together.user.email;

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

    // 계정 생성 시 이메일 인증
    // 인증 코드 요청 API
    @PostMapping("/email/send")
    public ResponseEntity<String> sendVerificationEmail(@RequestParam String email) throws MessagingException {
        String code = emailService.generateVerificationCode();
        verificationCodeService.saveVerificationCode(email, code);
        emailService.sendVerificationEmail(email, code);
        return ResponseEntity.ok("이메일 인증 코드가 전송되었습니다.");
    }

    // 인증 코드 검증 API
    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String code) {
        if (verificationCodeService.verifyCode(email, code)) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        }
        return ResponseEntity.badRequest().body("인증 코드가 올바르지 않습니다.");
    }

}
