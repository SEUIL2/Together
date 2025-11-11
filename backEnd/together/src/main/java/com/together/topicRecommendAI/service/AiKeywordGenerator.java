//package com.together.topicRecommendAI.service;
//
//import com.together.topicRecommendAI.UserKeywordHistoryEntity;
//import com.together.topicRecommendAI.UserKeywordHistoryRepository;
//import com.together.user.UserEntity;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class AiKeywordGenerator {
//
//    private final OpenAiService openAiService;
//
//    private final UserKeywordHistoryRepository historyRepository;
//
//    public List<String> generateKeywords(UserEntity user) {
//        List<String> previousKeywords = historyRepository.findByUser(user).stream()
//                .map(UserKeywordHistoryEntity::getKeyword)
//                .collect(Collectors.toList());
//
//        String prompt = buildPrompt(previousKeywords);
//        String rawAnswer = openAiService.ask(prompt);
//
//        // ✅ 응답 문자열 → 줄바꿈으로 나누고 → "1. " 같은 숫자 접두어 제거
//        List<String> keywords = Arrays.stream(rawAnswer.split("\n"))
//                .map(line -> line.replaceAll("^\\d+\\.\\s*", "").trim())
//                .filter(line -> !line.isBlank())
//                .collect(Collectors.toList());
//
//        // 중복 제거 후 저장
//        List<String> newKeywords = keywords.stream()
//                .filter(k -> !previousKeywords.contains(k))
//                .toList();
//
//        List<UserKeywordHistoryEntity> saveList = newKeywords.stream()
//                .map(k -> {
//                    UserKeywordHistoryEntity history = new UserKeywordHistoryEntity();
//                    history.setUser(user);
//                    history.setKeyword(k);
//                    return history;
//                }).toList();
//        historyRepository.saveAll(saveList);
//
//        return newKeywords;
//    }
//
//    private String buildPrompt(List<String> exclude) {
//        if (exclude.isEmpty()) {
//            return "대학생들이 실제 팀 프로젝트로 구현할 수 있을 정도로 간단하고 실용적인 주제를 기반으로 키워드를 생성해줘. \n" +
//                    "웹 또는 모바일 앱 형태로 만들 수 있는 프로젝트에 적합한 키워드만 포함해줘. \n" +
//                    "예를 들어 커뮤니티 사이트, 축제 웹사이트, 일정 관리 앱, 반려동물 기록 앱 같은 쉬운 주제를 위한 키워드가 필요해. \n" +
//                    "너무 자세한 예시는 불필요해, 키워드로 추천해줘, 예를 들어서 키워드 뒤에 '~앱' 이나 '~웹' 이 안붙게끔 해줘 \n" +
//                    "총 8개의 키워드를 추천해줘. 이전에 제시한 키워드는 제외하고, 서로 다른 분야를 다루도록 중복 없이 생성해줘. ";
//        }
//        return String.format("다음 키워드는 이미 추천되었어: %s. 이걸 제외하고 대학생이 웹/앱으로 만들기 쉬운 키워드 8개만 추천해줘.",
//                String.join(", ", exclude));
//    }
//}

// backEnd/together/src/main/java/com/together/topicRecommendAI/service/AiKeywordGenerator.java

package com.together.topicRecommendAI.service;

import com.together.topicRecommendAI.UserKeywordHistoryEntity;
import com.together.topicRecommendAI.UserKeywordHistoryRepository;
import com.together.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiKeywordGenerator {

    private final OpenAiService openAiService;
    private final UserKeywordHistoryRepository historyRepository;
    private final UserKeywordHistoryService userKeywordHistoryService;

    /*public List<String> generateKeywords(UserEntity user) {
        List<String> previousKeywords = getPreviousKeywords(user);
        String prompt = buildPrompt(previousKeywords, null);
        return getAndSaveNewKeywords(user, previousKeywords, prompt);
    }

    public List<String> generateKeywordsFromUserInput(UserEntity user, List<String> userInputKeywords) {
        List<String> previousKeywords = getPreviousKeywords(user);
        String prompt = buildPrompt(previousKeywords, userInputKeywords);
        return getAndSaveNewKeywords(user, previousKeywords, prompt);
    }

    private List<String> getPreviousKeywords(UserEntity user) {
        return historyRepository.findByUser(user).stream()
                .map(UserKeywordHistoryEntity::getKeyword)
                .collect(Collectors.toList());
    }

    private List<String> getAndSaveNewKeywords(UserEntity user, List<String> previousKeywords, String prompt) {
        String rawAnswer = openAiService.ask(prompt);

        List<String> keywords = Arrays.stream(rawAnswer.split("\n"))
                .map(line -> line.replaceAll("^\\d+\\.\\s*", "").trim())
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

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
    }*/

    public List<String> generateKeywords(UserEntity user) {
        // [수정] 서비스의 메소드 사용
        List<String> previousKeywords = userKeywordHistoryService.getPreviousKeywords(user);
        String prompt = buildPrompt(previousKeywords, null);
        return getAndSaveNewKeywords(user, previousKeywords, prompt);
    }

    public List<String> generateKeywordsFromUserInput(UserEntity user, List<String> userInputKeywords) {
        // [수정] 서비스의 메소드 사용
        List<String> previousKeywords = userKeywordHistoryService.getPreviousKeywords(user);
        String prompt = buildPrompt(previousKeywords, userInputKeywords);
        return getAndSaveNewKeywords(user, previousKeywords, prompt);
    }

    /*private List<String> getPreviousKeywords(UserEntity user) {
        // [기존]
        // return historyRepository.findByUser(user).stream()
        //         .map(UserKeywordHistoryEntity::getKeyword)
        //         .collect(Collectors.toList());

        // [수정] 이 메소드는 이제 userKeywordHistoryService.getPreviousKeywords로 대체되어 사용되지 않거나,
        //      위의 generateKeywords, generateKeywordsFromUserInput 메소드처럼 직접 호출로 변경합니다.
        //      (위 코드에서는 이미 변경했습니다.)
        //      이 메소드(getPreviousKeywords)는 삭제해도 무방합니다.

        // 만약 남겨둔다면, 내부 구현을 서비스 호출로 변경합니다.
        return userKeywordHistoryService.getPreviousKeywords(user);
    }*/

    private List<String> getAndSaveNewKeywords(UserEntity user, List<String> previousKeywords, String prompt) {
        String rawAnswer = openAiService.ask(prompt);

        List<String> keywords = Arrays.stream(rawAnswer.split("\n"))
                .map(line -> line.replaceAll("^\\d+\\.\\s*", "").trim())
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        List<String> newKeywords = keywords.stream()
                .filter(k -> !previousKeywords.contains(k))
                .toList();

        // [기존]
        // List<UserKeywordHistoryEntity> saveList = newKeywords.stream()
        //         .map(k -> {
        //             UserKeywordHistoryEntity history = new UserKeywordHistoryEntity();
        //             history.setUser(user);
        //             history.setKeyword(k);
        //             return history;
        //         }).toList();
        // historyRepository.saveAll(saveList);

        // [수정] UserKeywordHistoryService를 통해 각 키워드를 저장합니다.
        // 이렇게 하면 키워드가 하나씩 저장될 때마다 50개 제한 로직이 동작합니다.
        newKeywords.forEach(keyword ->
                userKeywordHistoryService.addKeywordHistory(user, keyword)
        );

        return newKeywords;
    }

    private String buildPrompt(List<String> exclude, List<String> userInputKeywords) {
        // 사용자 키워드가 입력된 경우
        if (userInputKeywords != null && !userInputKeywords.isEmpty()) {
            String userInputPrompt = String.join(", ", userInputKeywords);

            String basePrompt = String.format(
                    "너는 대학생의 프로젝트 주제 추천을 돕는 AI야. " +
                            "사용자가 제시한 특정 키워드와 '직접 관련된' 프로젝트 아이디어를 키워드 형태로 8개 제안해야 해. \n\n" +
                            "## 입력 키워드:\n" +
                            "'%s'\n\n" +
                            "## 필수 조건:\n" +
                            "1. 모든 추천 키워드는 반드시 '입력 키워드'와 직접적으로 관련되어야 한다.\n" +
                            "2. '대학생', '팀 프로젝트' 같은 일반적인 주제가 아닌, 오직 '입력 키워드'에만 집중해야 한다.\n" +
                            "3. 각 키워드는 명사 형태로 간결하게 제시한다. ('~앱', '~ 웹사이트' 같은 접미사는 절대 붙이지 않는다.)\n" +
                            "4. 예시: 입력 키워드가 '음식'이라면, '맛집 지도', '레시피 공유', '식단 관리', '푸드 커뮤니티' 처럼 응답한다.\n\n" +
                            "위 조건에 맞는 키워드 8개를 추천해줘.",
                    userInputPrompt
            );

            if (exclude.isEmpty()) {
                return basePrompt;
            } else {
                // 제외 키워드 조건을 간결하게 추가
                return basePrompt + String.format("\n\n## 제외할 키워드:\n%s", String.join(", ", exclude));
            }
        }
        else {
            if (exclude.isEmpty()) {
                return "대학생들이 실제 팀 프로젝트로 구현할 수 있을 정도로 간단하고 실용적인 주제를 기반으로 키워드를 생성해줘. \n" +
                        "웹 또는 모바일 앱 형태로 만들 수 있는 프로젝트에 적합한 키워드만 포함해줘. \n" +
                        "예를 들어 커뮤니티 사이트, 축제 웹사이트, 일정 관리 앱, 반려동물 기록 앱 같은 쉬운 주제를 위한 키워드가 필요해. \n" +
                        "너무 자세한 예시는 불필요해, 키워드로 추천해줘, 예를 들어서 키워드 뒤에 '~앱' 이나 '~웹' 이 안붙게끔 해줘 \n" +
                        "응답되는 키워드 앞에 '대학생을 위한~' 은 안붙게끔 해줘 \n" +
                        "총 8개의 키워드를 추천해줘. 이전에 제시한 키워드는 제외하고, 서로 다른 분야를 다루도록 중복 없이 생성해줘. ";
            }
            return String.format("다음 키워드는 이미 추천되었어: %s. 이걸 제외하고 대학생이 웹/앱으로 만들기 쉬운 키워드 8개만 추천해줘.",
                    String.join(", ", exclude));
        }
    }
}
