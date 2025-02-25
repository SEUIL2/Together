package com.together.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user/auth")
public class UserController {

    private final UserService userService;

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

        // 유효성 검사: STUDENT인 경우 학번 필수
        if ("STUDENT".equalsIgnoreCase(requestDto.getUserRole()) &&
                (requestDto.getStudentNumber() == null || requestDto.getStudentNumber().isEmpty())) {
            return ResponseEntity.badRequest().body("STUDENT의 경우 학번을 입력해야 합니다.");
        }

        // 회원 등록
        userService.registerUser(requestDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

}
