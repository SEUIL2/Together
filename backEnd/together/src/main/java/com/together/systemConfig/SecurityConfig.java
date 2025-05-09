package com.together.systemConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (API 용도로 사용할 경우)
                .cors(withDefaults()) // 🔥 CORS 설정 활성화 추가
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/email/send",  // 인증 없이 이메일 전송 가능하도록 설정
                                "/auth/email/verify",
                                "/auth/find-id",
                                "/auth/find-id/verify",
                                "/auth/find-password",
                                "/auth/reset-password",
                                "/auth/check-id"
                        ).permitAll()
                        //그 외 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // Basic Auth 사용
                .formLogin(form -> form.disable())
                .addFilterBefore(
                        new CustomBasicAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class))),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
