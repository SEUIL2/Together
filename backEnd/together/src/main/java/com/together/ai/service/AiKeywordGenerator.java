package com.together.ai.service;

import com.together.ai.UserKeywordHistoryEntity;
import com.together.ai.UserKeywordHistoryRepository;
import com.together.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiKeywordGenerator {

    private final OpenAiService openAiService;

    private final UserKeywordHistoryRepository historyRepository;

    public List<String> generateKeywords(UserEntity user) {
        List<String> previousKeywords = historyRepository.findByUser(user).stream()
                .map(UserKeywordHistoryEntity::getKeyword)
                .collect(Collectors.toList());

        String prompt = buildPrompt(previousKeywords);
        String rawAnswer = openAiService.ask(prompt);

        // ✅ 응답 문자열 → 줄바꿈으로 나누고 → "1. " 같은 숫자 접두어 제거
        List<String> keywords = Arrays.stream(rawAnswer.split("\n"))
                .map(line -> line.replaceAll("^\\d+\\.\\s*", "").trim())
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        // 중복 제거 후 저장
        List<String> newKeywords = keywords.stream()
                .filter(k -> !previousKeywords.contains(k))
                .toList();

        List<UserKeywordHistoryEntity> saveList = newKeywords.stream()
                .map(k -> {
                    UserKeywordHistoryEntity history = new UserKeywordHistoryEntity();
                    history.setUser(user);
                    history.setKeyword(k);
                    return history;
                }).toList();
        historyRepository.saveAll(saveList);

        return newKeywords;
    }

    private String buildPrompt(List<String> exclude) {
        if (exclude.isEmpty()) {
            return "대학생들이 실제 팀 프로젝트로 구현할 수 있을 정도로 간단하고 실용적인 주제를 기반으로 키워드를 생성해줘. \n" +
                    "웹 또는 모바일 앱 형태로 만들 수 있는 프로젝트에 적합한 키워드만 포함해줘. \n" +
                    "예를 들어 커뮤니티 사이트, 축제 웹사이트, 일정 관리 앱, 반려동물 기록 앱 같은 쉬운 주제를 위한 키워드가 필요해. \n" +
                    "너무 자세한 예시는 불필요해, 키워드로 추천해줘, 예를 들어서 키워드 뒤에 '~앱' 이나 '~웹' 이 안붙게끔 해줘 \n" +
                    "총 8개의 키워드를 추천해줘. 이전에 제시한 키워드는 제외하고, 서로 다른 분야를 다루도록 중복 없이 생성해줘. ";
        }
        return String.format("다음 키워드는 이미 추천되었어: %s. 이걸 제외하고 대학생이 웹/앱으로 만들기 쉬운 키워드 8개만 추천해줘.",
                String.join(", ", exclude));
    }
}
