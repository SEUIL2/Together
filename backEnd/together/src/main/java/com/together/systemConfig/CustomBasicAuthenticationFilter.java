package com.together.systemConfig;

import com.together.user.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * CustomBasicAuthenticationFilter
 * - 프론트에서 전달된 Authorization 헤더 (Basic Auth)를 수동으로 처리하는 커스텀 필터
 * - UsernamePasswordAuthenticationToken을 생성하여 AuthenticationManager로 인증 시도
 */
@RequiredArgsConstructor
public class CustomBasicAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더 추출
        String authHeader = request.getHeader("Authorization");

        // Basic Auth 인증 헤더가 존재할 경우 처리
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                // 1️⃣ "Basic dGVzdDE6cGFzc3dvcmQ=" → Base64 디코딩
                String base64Credentials = authHeader.substring("Basic ".length());
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                String[] values = credentials.split(":", 2);

                // 2️⃣ username, password 분리
                String username = values[0];
                String password = values[1];

                // 3️⃣ 인증 요청 객체 생성
                UsernamePasswordAuthenticationToken authRequest =
                        new UsernamePasswordAuthenticationToken(username, password);

                // 4️⃣ AuthenticationManager를 사용하여 인증 시도
                Authentication authResult = authenticationManager.authenticate(authRequest);

                // 5️⃣ 인증 성공 시 SecurityContext 에 저장
                SecurityContextHolder.getContext().setAuthentication(authResult);
            } catch (Exception ex) {
                // ❌ 인증 실패 시 401 응답
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Basic Auth credentials");
                return;
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
