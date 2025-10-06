package com.together.topicRecommendAI.service;

import com.together.topicRecommendAI.dto.ChatMessage;
import com.together.topicRecommendAI.dto.ChatRequest;
import com.together.topicRecommendAI.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    public String ask(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<ChatMessage> messages = List.of(
                new ChatMessage("system", "너는 대학생 프로젝트 주제를 추천하는 도우미야."),
                new ChatMessage("user", prompt)
        );

        ChatRequest request = new ChatRequest();
        request.setMessages(messages);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ChatResponse> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, ChatResponse.class);

        return response.getBody().getChoices().get(0).getMessage().getContent();
    }

    /**
     * [ ✨ 새로 추가된 메소드 ]
     * 범용적인 AI 응답을 처리하기 위한 메소드입니다.
     * 시스템 메시지 없이 사용자 프롬프트만 전달하여 유연하게 사용할 수 있습니다.
     * @param prompt AI에게 전달할 전체 프롬프트
     * @return AI의 응답 문자열
     */
    public String getChatResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 시스템 메시지 없이 사용자 프롬프트만으로 역할을 부여합니다.
        List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", prompt));

        ChatRequest request = new ChatRequest();
        request.setMessages(messages);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ChatResponse> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, ChatResponse.class);

        if (response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
            return response.getBody().getChoices().get(0).getMessage().getContent();
        } else {
            // OpenAI 응답이 비어있거나 문제가 있을 경우의 예외 처리
            return "{\"error\":\"AI로부터 응답을 받는데 실패했습니다.\"}";
        }
    }

}
