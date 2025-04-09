package com.together.user.email;

import com.together.user.UserEntity;
import com.together.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService { //이메일 전송 서비스

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    // 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6자리 랜덤 숫자
        return String.valueOf(code);
    }

    // 이메일 전송 (HTML 형식)
    public void sendVerificationEmail(String to, String code) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        String htmlContent = "<h2>이메일 인증 코드</h2>"
                + "<p>아래 인증 코드를 입력해 주세요.</p>"
                + "<h3 style='color:blue;'>" + code + "</h3>";

        helper.setTo(to);
        helper.setSubject("회원가입 이메일 인증 코드");
        helper.setText(htmlContent, true); // true: HTML 사용

        mailSender.send(message);
    }

}
