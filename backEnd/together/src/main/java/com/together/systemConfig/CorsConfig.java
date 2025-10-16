package com.together.systemConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 특정 패턴의 출처를 허용합니다.
        configuration.addAllowedOriginPattern("http://localhost:5173");
        configuration.addAllowedOriginPattern("http://52.79.180.110:5173");
        // 만약 https를 사용한다면 https 패턴도 추가
//         configuration.addAllowedOriginPattern("https://3.38.204.194");

        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 쿠키 및 자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }

    /*@Bean
    public WebMvcConfigurer corsConfigurer() { //무영 수정 : PATCH , OPTIONS 추가
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 API 허용
                        .allowedOrigins("http://localhost:5173") // Vue 서버 주소
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // PATCH, OPTIONS 추가
                        .allowedHeaders("*") // 요청 헤더 모두 허용
                        .allowCredentials(true);
            }
        };
    }*/

}
