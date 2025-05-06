package com.together.ai;

import com.together.ai.dto.KeywordsRequestDto;
import com.together.ai.service.AiKeywordGenerator;
import com.together.ai.service.AiTopicGenerator;
import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiKeywordGenerator keywordGenerator;
    private final AiTopicGenerator topicGenerator;

    // 1. 키워드 추천 요청
    @GetMapping("/keywords")
    public ResponseEntity<List<String>> getKeywords(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<String> keywords = keywordGenerator.generateKeywords(userDetails.getUser());
        return ResponseEntity.ok(keywords);
    }

    // 2. 키워드 기반 주제 추천 요청
    @PostMapping("/topics")
    public ResponseEntity<List<String>> getTopics(@RequestBody KeywordsRequestDto request) {
        List<String> topics = topicGenerator.generateTopics(request.getKeywords());
        return ResponseEntity.ok(topics);
    }

}
