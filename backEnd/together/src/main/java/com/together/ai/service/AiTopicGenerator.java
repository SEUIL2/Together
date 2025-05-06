package com.together.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiTopicGenerator {

    private final OpenAiService openAiService;

    public List<String> generateTopics(List<String> keywords) {
        String prompt = String.format("""
            아래 키워드를 바탕으로 대학생이 할 수 있는 흥미로운 IT 프로젝트 주제를 5개 추천해줘.
            각 주제는 한 줄 설명으로 짧게 해줘.
            키워드: %s
        """, String.join(", ", keywords));

        String response = openAiService.ask(prompt);
        return Arrays.stream(response.split("\\n"))
                .map(s -> s.replaceAll("^\\d+[.\\)]\\s*", "")) // "1. 주제" -> "주제"
                .filter(s -> !s.isBlank())
                .toList();
    }

}
