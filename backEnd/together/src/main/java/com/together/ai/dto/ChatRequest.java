package com.together.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String model = "gpt-3.5-turbo"; // or gpt-4
    private List<ChatMessage> messages;
    private double temperature = 0.8;
}
