package com.together.ai.service;

import com.together.ai.dto.ChatMessage;
import com.together.ai.dto.ChatRequest;
import com.together.ai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

}
