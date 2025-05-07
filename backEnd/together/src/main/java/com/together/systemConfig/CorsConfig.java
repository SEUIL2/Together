package com.together.systemConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
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
    }

}
